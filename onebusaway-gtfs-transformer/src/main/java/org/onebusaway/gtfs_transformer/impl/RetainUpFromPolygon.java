package org.onebusaway.gtfs_transformer.impl;

import java.util.ArrayList;
import java.util.List;

import java.io.Serializable;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.geom.*;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.factory.EntityRetentionGraph;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class RetainUpFromPolygon implements GtfsTransformStrategy {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private final WKTReader wktReader = new WKTReader(GEOMETRY_FACTORY);

    @CsvField(optional = false)
    private String polygon;

    @CsvField(ignore = true)
    private Geometry polygonGeometry;

    public void setPolygon(String polygon) {
        this.polygon = polygon;
        this.polygonGeometry = buildPolygon(polygon);

        if (this.polygonGeometry == null || !this.polygonGeometry.isValid() || this.polygonGeometry.isEmpty()) {
            throw new IllegalArgumentException("The provided polygon is invalid or empty.");
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext transformContext, GtfsMutableRelationalDao gtfsMutableRelationalDao) {
        EntityRetentionGraph graph = new EntityRetentionGraph(gtfsMutableRelationalDao);
        graph.setRetainBlocks(false);
        // browse all stops and retain only those inside polygon/multipolygon
        for (Stop stop : gtfsMutableRelationalDao.getAllStops()) {
            if (insidePolygon(polygonGeometry,stop.getLon(),stop.getLat())){
                graph.retain(stop, true);
            }
        }

        // remove non retained objects
        for (Class<?> entityClass : GtfsEntitySchemaFactory.getEntityClasses()) {
            List<Object> objectsToRemove = new ArrayList<Object>();
            for (Object entity : gtfsMutableRelationalDao.getAllEntitiesForType(entityClass)) {
                if (!graph.isRetained(entity)){
                    objectsToRemove.add(entity);
                    }
                }
            for (Object toRemove : objectsToRemove){
                gtfsMutableRelationalDao.removeEntity((IdentityBean<Serializable>) toRemove);
                }
            }
        }   
    
    /*
     * Creates a Geometry object (polygon or multi-polygon) from the provided WKT string.
     * 
     * @param polygonWKT The WKT representation of the polygon.
     * @return The Geometry object.
     * @throws IllegalArgumentException if the WKT string is invalid or cannot be parsed.
     */
    private Geometry buildPolygon(String polygonWKT) {
        try{
            return wktReader.read(polygonWKT);
        } catch (ParseException e){
            throw new IllegalArgumentException(
                String.format("Error parsing WKT string: %s", e.getMessage()), e
            );
        }
    }
    /*
     * insidePolygon Checks whether a given point (specified by its longitude and latitude) is inside a given polygon or multipolygon.
     *
     * @param geometry The Geometry object representing the polygon or multipolygon.
     * @param lon the longitude of the point to check.
     * @param lat the latitude of the point to check.
     * @return true if the point is within the boundaries of the geometry; false otherwise.
     */
    private boolean insidePolygon(Geometry geometry, double lon, double lat) {
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(lon, lat));
        return geometry.contains(point);
    }

}
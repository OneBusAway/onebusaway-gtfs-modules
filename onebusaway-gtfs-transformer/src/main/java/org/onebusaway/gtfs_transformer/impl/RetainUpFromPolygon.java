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
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

public class RetainUpFromPolygon implements GtfsTransformStrategy {
    private final Logger log = LoggerFactory.getLogger(RetainUpFromPolygon.class);

    @CsvField(optional = false)
    private String polygon;

    public void setPolygon(String polygon) {
        this.polygon = polygon;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext transformContext, GtfsMutableRelationalDao gtfsMutableRelationalDao) {
        Geometry geometry = buildPolygon(polygon);
        EntityRetentionGraph graph = new EntityRetentionGraph(gtfsMutableRelationalDao);
        graph.setRetainBlocks(false);
        // browse all stops and retain only those inside polygon/multipolygon
        if (geometry.isValid() && !geometry.isEmpty()){
            for (Stop stop : gtfsMutableRelationalDao.getAllStops()) {
                if (insidePolygon(geometry,stop.getLon(),stop.getLat())){
                    graph.retain(stop, true);
                }
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
     * create polygon/multiPolygon from 'polygon' variable in json file
        * return Geometry variable
        * return null if an exception is encountered when parsing the wkt string
     */
    private Geometry buildPolygon(String polygonWKT) {
        WKTReader reader = new WKTReader();
        try{
            return  reader.read(polygonWKT);
        } catch (ParseException e){
            String message = String.format("Error parsing WKT string : %s", e.getMessage());
            log.error(message);
            return null;
        }
        
    }
    /*
     * insidePolygon returns boolean variable
        * true: if polygon contains point
        * false if point is outside polygon
     */
    private boolean insidePolygon(Geometry geometry, double lon, double lat) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
        return geometry.contains(point);
    }

}

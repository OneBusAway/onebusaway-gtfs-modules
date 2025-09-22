package org.onebusaway.gtfs_transformer.impl;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.onebusaway.collections.beans.PropertyPathExpression;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.match.PropertyValueEntityMatch;
import org.onebusaway.gtfs_transformer.match.SimpleValueMatcher;
import org.onebusaway.gtfs_transformer.match.TypedEntityMatch;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.updates.TrimTripTransformStrategy;
import org.onebusaway.gtfs_transformer.updates.TrimTripTransformStrategy.TrimOperation;

public class TrimTripFromPolygon implements GtfsTransformStrategy {
  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
  private final WKTReader wktReader = new WKTReader(GEOMETRY_FACTORY);

  private final TrimTripTransformStrategy strategy = new TrimTripTransformStrategy();
  private final TransformContext context = new TransformContext();

  @CsvField(optional = false)
  private String polygon;

  @CsvField(ignore = true)
  private Geometry polygonGeometry;

  public void setPolygon(String polygon) {
    this.polygon = polygon;
    this.polygonGeometry = buildPolygon(polygon);

    if (this.polygonGeometry == null
        || !this.polygonGeometry.isValid()
        || this.polygonGeometry.isEmpty()) {
      throw new IllegalArgumentException("The provided polygon is invalid or empty.");
    }
  }

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(
      TransformContext transformContext, GtfsMutableRelationalDao gtfsMutableRelationalDao) {

    for (Trip trip : gtfsMutableRelationalDao.getAllTrips()) {
      // retrieve the list of StopTimes for the current trip
      List<StopTime> stopTimes = gtfsMutableRelationalDao.getStopTimesForTrip(trip);
      List<StopTime> stopTimesInPolygon = new ArrayList<>();

      for (StopTime stopTime : stopTimes) {
        Stop stop = gtfsMutableRelationalDao.getStopForId(stopTime.getStop().getId());

        // add StopTime to the list if its Stop is within the polygon boundaries
        if (insidePolygon(polygonGeometry, stop.getLon(), stop.getLat())) {
          stopTimesInPolygon.add(stopTime);
        }
      }

      // if some stops are inside the polygon but not all, apply the TrimTrip operation
      if (!stopTimesInPolygon.isEmpty() && stopTimesInPolygon.size() < stopTimes.size()) {
        applyTrimOperation(gtfsMutableRelationalDao, trip, stopTimesInPolygon);
      }
    }
    // execute the TrimTrip transformation strategy
    strategy.run(context, gtfsMutableRelationalDao);
  }

  private void applyTrimOperation(
      GtfsMutableRelationalDao gtfs, Trip trip, List<StopTime> stopTimes) {
    // initialize a new TrimOperation object to define parameters
    TrimOperation operation = new TrimOperation();

    StopTime firstStopTime = stopTimes.getFirst();
    StopTime lastStopTime = stopTimes.getLast();

    // set 'ToStopId' of the trim operation if the first StopTime is not the first stopTime in the
    // trip
    if (firstStopTime.getStopSequence() > 0) {
      StopTime previousStop =
          gtfs.getStopTimesForTrip(trip).get(firstStopTime.getStopSequence() - 1);
      operation.setToStopId(previousStop.getStop().getId().getId());
    }

    // set 'FromStopId' of the trim operation if the last StopTime is not the last stopTime in the
    // trip
    if (lastStopTime.getStopSequence() < (gtfs.getStopTimesForTrip(trip).size() - 1)) {
      StopTime nextStop = gtfs.getStopTimesForTrip(trip).get(lastStopTime.getStopSequence() + 1);
      operation.setFromStopId(nextStop.getStop().getId().getId());
    }

    // define the matching criteria
    operation.setMatch(
        new TypedEntityMatch(
            Trip.class,
            new PropertyValueEntityMatch(
                new PropertyPathExpression("id"), new SimpleValueMatcher(trip.getId()))));

    // add the TrimOperation to the strategy for later execution
    strategy.addOperation(operation);
  }

  /*
   * Creates a Geometry object (polygon or multi-polygon) from the provided WKT string.
   *
   * @param polygonWKT The WKT representation of the polygon.
   * @return The Geometry object.
   * @throws IllegalArgumentException if the WKT string is invalid or cannot be parsed.
   */
  private Geometry buildPolygon(String polygonWKT) {
    try {
      return wktReader.read(polygonWKT);
    } catch (ParseException e) {
      throw new IllegalArgumentException(
          "Error parsing WKT string: %s".formatted(e.getMessage()), e);
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

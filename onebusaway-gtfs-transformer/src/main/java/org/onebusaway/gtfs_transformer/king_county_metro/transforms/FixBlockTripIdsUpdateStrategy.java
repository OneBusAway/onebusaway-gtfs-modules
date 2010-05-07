package org.onebusaway.gtfs_transformer.king_county_metro.transforms;

import java.util.HashMap;
import java.util.Map;

import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.king_county_metro.MetroKCDao;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCBlockTrip;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixBlockTripIdsUpdateStrategy implements GtfsTransformStrategy {

  private static Logger _log = LoggerFactory.getLogger(FixBlockTripIdsUpdateStrategy.class);

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    MetroKCDao metrokcDao = context.getMetroKCDao();
    
    Map<String, Trip> tripsById = getTripsById(dao);

    int hits = 0;
    int total = 0;

    for (MetroKCBlockTrip block : metrokcDao.getAllBlockTrips()) {
      String tripId = Integer.toString(block.getTripId());
      String blockId = Integer.toString(block.getBlockId());
      Trip trip = tripsById.get(tripId);
      if (trip == null)
        continue;
      total++;
      if (trip.getBlockId() == null || trip.getBlockId().equals(blockId))
        continue;
      hits++;
      trip.setBlockId(blockId);
    }

    _log.info("fix block ids: " + hits + "/" + total);
  }

  private Map<String, Trip> getTripsById(GtfsMutableRelationalDao dao) {
    Map<String, Trip> tripsById = new HashMap<String, Trip>();
    for (Trip trip : dao.getAllTrips())
      tripsById.put(trip.getId().getId(), trip);
    return tripsById;
  }
}

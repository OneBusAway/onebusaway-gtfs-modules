package org.onebusaway.gtfs_transformer.king_county_metro.transforms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.csv.CsvEntityContext;
import org.onebusaway.gtfs.csv.schema.AbstractFieldMapping;
import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.csv.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.beans.CsvEntityMappingBean;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.king_county_metro.MetroKCDao;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCStop;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.SchemaUpdateStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.updates.TripsByBlockInSortedOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeprecatedFieldsUpdaterStrategy implements GtfsTransformStrategy,
    SchemaUpdateStrategy {

  static Logger _log = LoggerFactory.getLogger(DeprecatedFieldsUpdaterStrategy.class);

  private Map<AgencyAndId, Double> _stopDirectionByStopId = new HashMap<AgencyAndId, Double>();

  private Map<AgencyAndId, Integer> _blockSequenceIdByTripId = new HashMap<AgencyAndId, Integer>();

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    
    MetroKCDao metrokcDao = context.getMetroKCDao();

    generateStopDirections(dao, metrokcDao);
    generateBlockSequenceIds(dao, metrokcDao);
  }

  private void generateStopDirections(GtfsMutableRelationalDao dao,
      MetroKCDao metrokcDao) {
    for (Stop stop : dao.getAllStops()) {
      try {
        int id = Integer.parseInt(stop.getId().getId());
        MetroKCStop mkcStop = metrokcDao.getStopForId(id);
        if (mkcStop == null)
          continue;
        double direction = getDirection(mkcStop);
        _stopDirectionByStopId.put(stop.getId(), direction);
      } catch (NumberFormatException ex) {

      }
    }
  }

  private void generateBlockSequenceIds(GtfsMutableRelationalDao dao,
      MetroKCDao metrokcDao) {

    Map<String, List<Trip>> tripsByBlockId = TripsByBlockInSortedOrder.getTripsByBlockInSortedOrder(dao);

    for (List<Trip> tripsInBlock : tripsByBlockId.values()) {
      int index = 0;
      for (Trip trip : tripsInBlock)
        _blockSequenceIdByTripId.put(trip.getId(), index++);
    }
  }

  private double getDirection(MetroKCStop stop) {

    double x = stop.getX();
    double y = stop.getY();

    double xStreet = stop.getStreetX();
    double yStreet = stop.getStreetY();

    double theta = Math.atan2(y - yStreet, x - xStreet);
    theta += Math.PI / 2;
    while (theta > Math.PI)
      theta -= Math.PI * 2;
    while (theta <= -Math.PI)
      theta += Math.PI * 2;
    return theta;
  }

  @Override
  public void updateSchema(DefaultEntitySchemaFactory factory) {

    CsvEntityMappingBean stopMappingBean = new CsvEntityMappingBean(Stop.class);
    stopMappingBean.addAdditionalFieldMapping(new StopFieldMapping());
    factory.addBean(stopMappingBean);

    CsvEntityMappingBean tripMappingBean = new CsvEntityMappingBean(Trip.class);
    tripMappingBean.addAdditionalFieldMapping(new BlockSequenceIdFieldMapping());
    factory.addBean(tripMappingBean);
  }

  private class StopFieldMapping extends AbstractFieldMapping {

    public StopFieldMapping() {
      super(Stop.class, "stop_direction", "stopDirection", false);
    }

    @Override
    public void translateFromCSVToObject(CsvEntityContext context,
        Map<String, Object> csvValues, BeanWrapper object) {
      // Nothing to do here
    }

    @Override
    public void translateFromObjectToCSV(CsvEntityContext context,
        BeanWrapper object, Map<String, Object> csvValues) {
      AgencyAndId stopId = (AgencyAndId) object.getPropertyValue("id");
      Double direction = _stopDirectionByStopId.get(stopId);
      if (direction != null)
        csvValues.put(_csvFieldName, direction);
    }
  }

  private class BlockSequenceIdFieldMapping extends AbstractFieldMapping {

    public BlockSequenceIdFieldMapping() {
      super(Trip.class, "block_sequence_id", "blockSequenceId", false);
    }

    @Override
    public void translateFromCSVToObject(CsvEntityContext context,
        Map<String, Object> csvValues, BeanWrapper object) {
      // Nothing to do here
    }

    @Override
    public void translateFromObjectToCSV(CsvEntityContext context,
        BeanWrapper object, Map<String, Object> csvValues) {

      AgencyAndId tripId = (AgencyAndId) object.getPropertyValue("id");
      Integer blockSequenceId = _blockSequenceIdByTripId.get(tripId);
      if (blockSequenceId != null)
        csvValues.put(_csvFieldName, blockSequenceId);
      else
        _log.warn("no block sequence found for trip: " + tripId);
    }
  }

}

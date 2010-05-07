package org.onebusaway.gtfs_transformer.king_county_metro.transforms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class DeduplicateStopsStrategy implements GtfsTransformStrategy {

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    Map<AgencyAndId, List<Stop>> stopsById = new FactoryMap<AgencyAndId, List<Stop>>(
        new ArrayList<Stop>());

    for (Stop stop : dao.getAllStops()) {
      
      AgencyAndId aid = stop.getId();
      String id = aid.getId();

      int index = id.indexOf('_');
      if (index == -1)
        continue;

      String stopCode = id.substring(0, index);
      AgencyAndId generalId = new AgencyAndId(aid.getAgencyId(), stopCode);
      stopsById.get(generalId).add(stop);
    }

    Map<Stop, List<StopTime>> stopTimesByStop = MappingLibrary.mapToValueList(
        dao.getAllStopTimes(), "stop", Stop.class);

    for (Map.Entry<AgencyAndId, List<Stop>> entry : stopsById.entrySet()) {

      AgencyAndId stopId = entry.getKey();
      List<Stop> stops = entry.getValue();

      // Remove the stop with the old id
      Stop stop = stops.get(0);
      dao.removeEntity(stop);

      // Add the stop with new id
      stop.setId(stopId);
      dao.saveEntity(stop);

      for (int i = 1; i < stops.size(); i++) {
        Stop duplicateStop = stops.get(i);
        dao.removeEntity(duplicateStop);
        List<StopTime> stopTimes = stopTimesByStop.get(duplicateStop);
        if (stopTimes == null)
          continue;
        for (StopTime stopTime : stopTimes)
          stopTime.setStop(stop);
      }
    }
  }
}

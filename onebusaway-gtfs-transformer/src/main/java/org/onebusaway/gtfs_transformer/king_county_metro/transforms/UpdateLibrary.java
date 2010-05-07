package org.onebusaway.gtfs_transformer.king_county_metro.transforms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.king_county_metro.MetroKCDao;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCChangeDate;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCTrip;
import org.onebusaway.gtfs_transformer.model.VersionedId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateLibrary {

  private static Logger _log = LoggerFactory.getLogger(UpdateLibrary.class);

  public static Map<Trip, List<MetroKCChangeDate>> getChangeDatesByTrip(
      GtfsMutableRelationalDao dao, MetroKCDao metrokcDao) {

    List<MetroKCChangeDate> allChangeDates = new ArrayList<MetroKCChangeDate>(
        metrokcDao.getAllChangeDates());
    Collections.sort(allChangeDates);

    Map<Trip, List<MetroKCChangeDate>> changeDatesByTrip = new HashMap<Trip, List<MetroKCChangeDate>>();

    for (Trip trip : dao.getAllTrips()) {

      AgencyAndId aid = trip.getId();
      String id = aid.getId();

      int index = id.indexOf('_');

      if (index == -1) {
        changeDatesByTrip.put(trip, allChangeDates);
      } else {
        String changeDateId = id.substring(0, index);
        MetroKCChangeDate changeDate = metrokcDao.getChangeDateForId(changeDateId);
        if (changeDate == null)
          throw new IllegalStateException("unknown change date: " + changeDate);
        changeDatesByTrip.put(trip, Arrays.asList(changeDate));
      }
    }

    return changeDatesByTrip;
  }

  public static Map<Trip, MetroKCTrip> getMetroKCTripsByGtfsTrip(GtfsDao dao,
      MetroKCDao metrokcDao, Map<Trip, MetroKCTrip> mapping) {

    List<MetroKCChangeDate> changeDates = new ArrayList<MetroKCChangeDate>(
        metrokcDao.getAllChangeDates());
    Collections.sort(changeDates);

    int misses = 0;
    int total = 0;

    for (Trip trip : dao.getAllTrips()) {

      AgencyAndId aid = trip.getId();
      String id = aid.getId();

      int index = id.indexOf('_');

      MetroKCTrip metroKCTrip = null;
      if (index == -1) {
        metroKCTrip = getMetroKCTripByUnversionedTripId(metrokcDao,
            changeDates, id);
      } else {
        String changeDate = id.substring(0, index);
        String tripId = id.substring(index + 1);
        VersionedId versionedId = new VersionedId(changeDate,
            Integer.parseInt(tripId));
        metroKCTrip = metrokcDao.getTripForId(versionedId);
      }

      if (metroKCTrip != null)
        mapping.put(trip, metroKCTrip);
      else
        misses++;

      total++;
    }

    if (misses > 0)
      _log.warn("gtfs trips without metrokc trips: " + misses + " / " + total);

    return mapping;
  }

  public static void clearDaoCache(GtfsMutableRelationalDao dao) {
    if (dao instanceof GtfsRelationalDaoImpl) {
      GtfsRelationalDaoImpl daoImpl = (GtfsRelationalDaoImpl) dao;
      daoImpl.clearAllCaches();
    }
  }

  private static MetroKCTrip getMetroKCTripByUnversionedTripId(
      MetroKCDao metrokcDao, List<MetroKCChangeDate> changeDates,
      String numericTripId) {

    int tripId = Integer.parseInt(numericTripId);

    for (MetroKCChangeDate changeDate : changeDates) {
      VersionedId id = new VersionedId(changeDate.getId(), tripId);
      MetroKCTrip metroKCTrip = metrokcDao.getTripForId(id);
      if (metroKCTrip != null)
        return metroKCTrip;
    }

    return null;
  }
}

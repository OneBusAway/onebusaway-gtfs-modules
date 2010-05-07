package org.onebusaway.gtfs_transformer.impl;

import java.util.Map;

import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class RemoveEntityUpdateStrategy extends
    AbstractEntityModificationStrategy {

  public RemoveEntityUpdateStrategy(Map<String, Object> propertyMatches) {
    super(propertyMatches);
  }

  @Override
  public void applyModification(TransformContext context, BeanWrapper wrapped,
      GtfsMutableRelationalDao dao) {

    if (!isModificationApplicable(wrapped))
      return;

    Object obj = wrapped.getWrappedInstance(Object.class);
    if (obj instanceof Agency) {
      removeAgency(dao, (Agency) obj);
    } else if (obj instanceof Route) {
      removeRoute(dao, (Route) obj);
    } else if (obj instanceof Stop) {
      removeStop(dao, (Stop) obj);
    } else if (obj instanceof Trip) {
      removeTrip(dao, (Trip) obj);
    } else if (obj instanceof StopTime) {
      removeStopTime(dao, (StopTime) obj);
    }

  }

  private void removeAgency(GtfsMutableRelationalDao dao, Agency agency) {
    for (Route route : dao.getRoutesForAgency(agency))
      removeRoute(dao, route);
    dao.removeEntity(agency);
  }

  private void removeRoute(GtfsMutableRelationalDao dao, Route route) {
    for (Trip trip : dao.getTripsForRoute(route))
      removeTrip(dao, trip);
    dao.removeEntity(route);
  }

  private void removeTrip(GtfsMutableRelationalDao dao, Trip trip) {
    for (StopTime stopTime : dao.getStopTimesForTrip(trip))
      removeStopTime(dao, stopTime);
    for (Frequency frequency : dao.getFrequenciesForTrip(trip))
      removeFrequency(dao, frequency);
    dao.removeEntity(trip);
  }

  private void removeFrequency(GtfsMutableRelationalDao dao, Frequency frequency) {
    dao.removeEntity(frequency);
  }

  private void removeStop(GtfsMutableRelationalDao dao, Stop stop) {
    for (StopTime stopTime : dao.getStopTimesForStop(stop))
      removeStopTime(dao, stopTime);
    dao.removeEntity(stop);
  }

  private void removeStopTime(GtfsMutableRelationalDao dao, StopTime stopTime) {
    dao.removeEntity(stopTime);
  }
}

package org.onebusaway.gtfs_transformer.king_county_metro.transforms;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableDao;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.king_county_metro.MetroKCDao;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCChangeDate;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCTrip;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.updates.TripScheduleModificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarUpdateStrategy implements GtfsTransformStrategy {

  private Logger _log = LoggerFactory.getLogger(CalendarUpdateStrategy.class);

  private List<TripScheduleModificationStrategy> _modifications = new ArrayList<TripScheduleModificationStrategy>();

  private Set<MetroKCServiceId> _serviceIds = new HashSet<MetroKCServiceId>();

  private String _defaultAgencyId;

  public void addModificationStrategy(TripScheduleModificationStrategy strategy) {
    _modifications.add(strategy);
  }

  public void setDefaultAgencyId(String defaultAgencyId) {
    _defaultAgencyId = defaultAgencyId;
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    
    MetroKCDao metrokcDao = context.getMetroKCDao();

    List<ServiceCalendar> existingCalendars = new ArrayList<ServiceCalendar>();
    existingCalendars.addAll(dao.getAllCalendars());

    List<ServiceCalendarDate> existingCalendarDates = new ArrayList<ServiceCalendarDate>();
    existingCalendarDates.addAll(dao.getAllCalendarDates());

    int misses = 0;
    int total = 0;

    Set<AgencyAndId> serviceIdsToKeep = new HashSet<AgencyAndId>();

    Map<Trip, List<MetroKCChangeDate>> changeDateMapping = UpdateLibrary.getChangeDatesByTrip(
        dao, metrokcDao);
    Map<Trip, MetroKCTrip> tripMapping = UpdateLibrary.getMetroKCTripsByGtfsTrip(
        dao, metrokcDao, new HashMap<Trip, MetroKCTrip>());

    for (Trip trip : dao.getAllTrips()) {

      MetroKCTrip mTrip = tripMapping.get(trip);
      List<MetroKCChangeDate> changeDates = changeDateMapping.get(trip);
      if (mTrip == null || changeDates == null) {
        serviceIdsToKeep.add(trip.getServiceId());
        misses++;
        continue;
      }

      total++;

      AgencyAndId serviceId = getServiceIdsForTrip(context, dao, metrokcDao,
          mTrip,changeDates);
      trip.setServiceId(serviceId);
    }

    _log.info("totalTrips=" + total + " withoutMetroKCTrip" + misses);

    // Remove any service dates still not in use
    for (ServiceCalendar serviceCalendar : existingCalendars) {
      if (!serviceIdsToKeep.contains(serviceCalendar.getServiceId()))
        dao.removeEntity(serviceCalendar);
    }

    for (ServiceCalendarDate serviceCalendarDate : existingCalendarDates) {
      if (!serviceIdsToKeep.contains(serviceCalendarDate.getServiceId()))
        dao.removeEntity(serviceCalendarDate);
    }
  }

  public AgencyAndId getServiceIdsForTrip(TransformContext context,
      GtfsMutableDao dao, MetroKCDao metrokcDao, MetroKCTrip mkcTrip, List<MetroKCChangeDate> changeDates) {

    String scheduleType = mkcTrip.getScheduleType();
    String exceptionCode = mkcTrip.getExceptionCode();

    MetroKCServiceId serviceId = new MetroKCServiceId(changeDates,
        scheduleType, exceptionCode);

    if (_serviceIds.add(serviceId)) {
      computeServiceIdsForTrip(dao, serviceId);
    }

    return new AgencyAndId(context.getDefaultAgencyId(),
        serviceId.getServiceId());
  }

  private void computeServiceIdsForTrip(GtfsMutableDao dao, MetroKCServiceId key) {

    List<MetroKCChangeDate> changeDates = key.getChangeDates();
    String scheduleType = key.getScheduleType();

    
    Date startDate = changeDates.get(0).getStartDate();
    Date endDate = changeDates.get(changeDates.size() - 1).getEndDate();

    ServiceDate startServiceDate = new ServiceDate(startDate);
    ServiceDate endServiceDate = new ServiceDate(endDate);

    ServiceCalendar c = new ServiceCalendar();

    AgencyAndId serviceId = new AgencyAndId(_defaultAgencyId,
        key.getServiceId());
    c.setServiceId(serviceId);
    c.setStartDate(startServiceDate);
    c.setEndDate(endServiceDate);

    int dayFrom = 0;
    int dayTo = 0;

    if (scheduleType.equals(MetroKCServiceId.SCHEDULE_TYPE_WEEKDAY)) {
      c.setMonday(1);
      c.setTuesday(1);
      c.setWednesday(1);
      c.setThursday(1);
      c.setFriday(1);
      dayFrom = Calendar.MONDAY;
      dayTo = Calendar.FRIDAY;
    } else if (scheduleType.equals(MetroKCServiceId.SCHEDULE_TYPE_SATURDAY)) {
      c.setSaturday(1);
      dayFrom = dayTo = Calendar.SATURDAY;
    } else if (scheduleType.equals(MetroKCServiceId.SCHEDULE_TYPE_SUNDAY)) {
      c.setSunday(1);
      dayFrom = dayTo = Calendar.SUNDAY;
    } else {
      throw new IllegalStateException("unknown schedule type: " + scheduleType);
    }

    dao.saveEntity(c);

    Calendar cal = Calendar.getInstance();
    cal.setTime(startDate);

    Set<Date> dates = new HashSet<Date>();
    
    while (cal.getTime().before(endDate)) {

      Date day = cal.getTime();
      int dow = cal.get(Calendar.DAY_OF_WEEK);
      cal.add(Calendar.DAY_OF_YEAR, 1);

      // Only consider days that we are already in service
      if (dow < dayFrom || dayTo < dow)
        continue;

      dates.add(day);
    }

    SortedMap<ServiceDate, ServiceCalendarDate> exceptionsByDate = new TreeMap<ServiceDate, ServiceCalendarDate>();

    for (TripScheduleModificationStrategy strategy : _modifications) {

      for (Date toAdd : strategy.getAdditions(key, dates)) {
        ServiceCalendarDate cd = new ServiceCalendarDate();
        cd.setServiceId(serviceId);
        cd.setDate(new ServiceDate(toAdd));
        cd.setExceptionType(ServiceCalendarDate.EXCEPTION_TYPE_ADD);
        addCalendarDateException(exceptionsByDate, cd);
      }

      for (Date toRemove : strategy.getCancellations(key, dates)) {
        ServiceCalendarDate cd = new ServiceCalendarDate();
        cd.setServiceId(serviceId);
        cd.setDate(new ServiceDate(toRemove));
        cd.setExceptionType(ServiceCalendarDate.EXCEPTION_TYPE_REMOVE);
        addCalendarDateException(exceptionsByDate, cd);
      }
    }

    for (ServiceCalendarDate cd : exceptionsByDate.values()) {
      dao.saveEntity(cd);
    }
  }

  private void addCalendarDateException(
      SortedMap<ServiceDate, ServiceCalendarDate> exceptionsByDate,
      ServiceCalendarDate cd) {

    ServiceCalendarDate existing = exceptionsByDate.put(new ServiceDate(cd.getDate()), cd);

    if (existing != null && compareTo(existing, cd) != 0)
      throw new IllegalStateException("CalendarDate collision: day="
          + cd.getDate() + " existing=" + existing + " replacement=" + cd);
  }

  private int compareTo(ServiceCalendarDate o1, ServiceCalendarDate o2) {

    int rc = 0;

    if ((rc = o1.getServiceId().compareTo(o2.getServiceId())) != 0)
      return rc;

    if ((rc = o1.getDate().compareTo(o2.getDate())) != 0)
      return rc;

    return o1.getExceptionType() == o2.getExceptionType() ? 0
        : (o1.getExceptionType() < o2.getExceptionType() ? -1 : 1);
  }
}

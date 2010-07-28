package org.onebusaway.gtfs.impl.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarServiceDataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * We perform initial date calculations in the timezone of the host jvm, which
 * may be different than the timezone of an agency with the specified service
 * id. To my knowledge, the calculation should work the same, which is to say I
 * can't immediately think of any cases where the service dates would be
 * computed incorrectly.
 * 
 * @author bdferris
 */
public class CalendarServiceDataFactoryImpl implements
    CalendarServiceDataFactory {

  private final Logger _log = LoggerFactory.getLogger(CalendarServiceDataFactoryImpl.class);

  private GtfsRelationalDao _dao;

  public CalendarServiceDataFactoryImpl() {

  }

  public CalendarServiceDataFactoryImpl(GtfsRelationalDao dao) {
    _dao = dao;
  }

  public void setGtfsDao(GtfsRelationalDao dao) {
    _dao = dao;
  }

  @Override
  public CalendarServiceData createData() {

    CalendarServiceData data = new CalendarServiceData();

    setTimeZonesForAgencies(data);

    Collection<ServiceCalendar> calendars = _dao.getAllCalendars();
    Collection<ServiceCalendarDate> calendarDates = _dao.getAllCalendarDates();

    Map<AgencyAndId, ServiceCalendar> calendarsByServiceId = getCalendarsByServiceId(calendars);
    Map<AgencyAndId, List<ServiceCalendarDate>> calendarDatesByServiceId = getCalendarDatesByServiceId(calendarDates);

    Set<AgencyAndId> serviceIds = new HashSet<AgencyAndId>();
    serviceIds.addAll(calendarsByServiceId.keySet());
    serviceIds.addAll(calendarDatesByServiceId.keySet());

    int index = 0;

    for (AgencyAndId serviceId : serviceIds) {

      index++;

      _log.info("serviceId=" + serviceId + " (" + index + "/"
          + serviceIds.size() + ")");

      Set<ServiceDate> activeDates = new HashSet<ServiceDate>();
      ServiceCalendar c = calendarsByServiceId.get(serviceId);

      if (c != null)
        addDatesFromCalendar(c, activeDates);
      if (calendarDatesByServiceId.containsKey(serviceId)) {
        for (ServiceCalendarDate cd : calendarDatesByServiceId.get(serviceId)) {
          addAndRemoveDatesFromCalendarDate(cd, activeDates);
        }
      }

      List<ServiceDate> serviceDates = new ArrayList<ServiceDate>(activeDates);
      Collections.sort(serviceDates);

      data.putServiceDatesForServiceId(serviceId, serviceDates);

      List<String> tripAgencyIds = _dao.getTripAgencyIdsReferencingServiceId(serviceId);

      Set<TimeZone> timeZones = new HashSet<TimeZone>();
      for (String tripAgencyId : tripAgencyIds) {
        TimeZone timeZone = data.getTimeZoneForAgencyId(tripAgencyId);
        timeZones.add(timeZone);
      }

      for (TimeZone timeZone : timeZones) {

        List<Date> dates = new ArrayList<Date>(serviceDates.size());
        for (ServiceDate serviceDate : serviceDates)
          dates.add(serviceDate.getAsDate(timeZone));

        LocalizedServiceId id = new LocalizedServiceId(serviceId, timeZone);
        data.putDatesForLocalizedServiceId(id, dates);
      }
    }

    return data;
  }

  private void setTimeZonesForAgencies(CalendarServiceData data) {
    for (Agency agency : _dao.getAllAgencies()) {
      TimeZone timeZone = TimeZone.getTimeZone(agency.getTimezone());
      if (timeZone == null)
        throw new UnknownAgencyTimezoneException(agency.getName(),
            agency.getTimezone());
      data.putTimeZoneForAgencyId(agency.getId(), timeZone);
    }
  }

  private Map<AgencyAndId, ServiceCalendar> getCalendarsByServiceId(
      Collection<ServiceCalendar> calendars) {
    Map<AgencyAndId, ServiceCalendar> calendarsByServiceId = new HashMap<AgencyAndId, ServiceCalendar>();
    for (ServiceCalendar c : calendars)
      calendarsByServiceId.put(c.getServiceId(), c);
    return calendarsByServiceId;
  }

  private Map<AgencyAndId, List<ServiceCalendarDate>> getCalendarDatesByServiceId(
      Collection<ServiceCalendarDate> calendarDates) {
    Map<AgencyAndId, List<ServiceCalendarDate>> calendarDatesByServiceId = new HashMap<AgencyAndId, List<ServiceCalendarDate>>();

    for (ServiceCalendarDate calendarDate : calendarDates) {
      List<ServiceCalendarDate> cds = calendarDatesByServiceId.get(calendarDate.getServiceId());
      if (cds == null) {
        cds = new ArrayList<ServiceCalendarDate>();
        calendarDatesByServiceId.put(calendarDate.getServiceId(), cds);
      }
      cds.add(calendarDate);
    }
    return calendarDatesByServiceId;
  }

  private void addDatesFromCalendar(ServiceCalendar calendar,
      Set<ServiceDate> activeDates) {

    Date startDate = calendar.getStartDate().getAsDate();
    Date endDate = calendar.getEndDate().getAsDate();

    java.util.Calendar c = java.util.Calendar.getInstance();
    c.setTime(startDate);

    while (true) {
      Date date = c.getTime();
      if (date.after(endDate))
        break;

      int day = c.get(java.util.Calendar.DAY_OF_WEEK);
      boolean active = false;

      switch (day) {
        case java.util.Calendar.MONDAY:
          active = calendar.getMonday() == 1;
          break;
        case java.util.Calendar.TUESDAY:
          active = calendar.getTuesday() == 1;
          break;
        case java.util.Calendar.WEDNESDAY:
          active = calendar.getWednesday() == 1;
          break;
        case java.util.Calendar.THURSDAY:
          active = calendar.getThursday() == 1;
          break;
        case java.util.Calendar.FRIDAY:
          active = calendar.getFriday() == 1;
          break;
        case java.util.Calendar.SATURDAY:
          active = calendar.getSaturday() == 1;
          break;
        case java.util.Calendar.SUNDAY:
          active = calendar.getSunday() == 1;
          break;
      }

      if (active) {
        activeDates.add(new ServiceDate(c));
      }

      c.add(java.util.Calendar.DAY_OF_YEAR, 1);
    }
  }

  private void addAndRemoveDatesFromCalendarDate(
      ServiceCalendarDate calendarDate, Set<ServiceDate> activeDates) {

    Date targetDate = calendarDate.getDate().getAsDate();
    Calendar c = Calendar.getInstance();
    c.setTime(targetDate);

    switch (calendarDate.getExceptionType()) {
      case ServiceCalendarDate.EXCEPTION_TYPE_ADD:
        activeDates.add(new ServiceDate(c));
        break;
      case ServiceCalendarDate.EXCEPTION_TYPE_REMOVE:
        activeDates.remove(new ServiceDate(c));
        break;
      default:
        _log.warn("unknown CalendarDate exception type: "
            + calendarDate.getExceptionType());
        break;
    }
  }

}

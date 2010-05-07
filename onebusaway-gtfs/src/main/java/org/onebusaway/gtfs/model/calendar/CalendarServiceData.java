package org.onebusaway.gtfs.model.calendar;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.onebusaway.gtfs.model.AgencyAndId;

public class CalendarServiceData implements Serializable {

  private static final long serialVersionUID = 1L;

  private Map<String, TimeZone> _timeZonesByAgencyId = new HashMap<String, TimeZone>();

  private Map<AgencyAndId, List<ServiceDate>> _serviceDatesByServiceId = new HashMap<AgencyAndId, List<ServiceDate>>();

  private Map<LocalizedServiceId, List<Date>> _datesByLocalizedServiceId = new HashMap<LocalizedServiceId, List<Date>>();

  private Map<ServiceDate, Set<AgencyAndId>> _serviceIdsByDate = new HashMap<ServiceDate, Set<AgencyAndId>>();

  public TimeZone getTimeZoneForAgencyId(String agencyId) {
    return _timeZonesByAgencyId.get(agencyId);
  }

  public void putTimeZoneForAgencyId(String agencyId, TimeZone timeZone) {
    _timeZonesByAgencyId.put(agencyId, timeZone);
  }

  public Set<AgencyAndId> getServiceIds() {
    return Collections.unmodifiableSet(_serviceDatesByServiceId.keySet());
  }

  public List<ServiceDate> getServiceDatesForServiceId(AgencyAndId serviceId) {
    return _serviceDatesByServiceId.get(serviceId);
  }

  public Set<AgencyAndId> getServiceIdsForDate(ServiceDate date) {
    Set<AgencyAndId> serviceIds = _serviceIdsByDate.get(date);
    if (serviceIds == null)
      serviceIds = new HashSet<AgencyAndId>();
    return serviceIds;
  }

  public void putServiceDatesForServiceId(AgencyAndId serviceId,
      List<ServiceDate> serviceDates) {
    Collections.sort(serviceDates);
    _serviceDatesByServiceId.put(serviceId, serviceDates);
    for (ServiceDate serviceDate : serviceDates) {
      Set<AgencyAndId> serviceIds = _serviceIdsByDate.get(serviceDate);
      if (serviceIds == null) {
        serviceIds = new HashSet<AgencyAndId>();
        _serviceIdsByDate.put(serviceDate, serviceIds);
      }
      serviceIds.add(serviceId);
    }
  }

  public List<Date> getDatesForLocalizedServiceId(LocalizedServiceId serviceId) {
    return _datesByLocalizedServiceId.get(serviceId);
  }

  public void putDatesForLocalizedServiceId(LocalizedServiceId serviceId,
      List<Date> dates) {
    _datesByLocalizedServiceId.put(serviceId, dates);
  }

}

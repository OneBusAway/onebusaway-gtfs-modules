package org.onebusaway.gtfs.model.calendar;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * A {@link Map} of {@link LocalizedServiceId} and {@link ServiceInterval}
 * objects, with convenience methods for adding additional service ids and
 * arrival-departure time intervals.
 * 
 * @author bdferris
 * 
 */
public class ServiceIdIntervals implements Serializable,
    Iterable<Map.Entry<LocalizedServiceId, ServiceInterval>> {

  private static final long serialVersionUID = 1L;

  private Map<LocalizedServiceId, ServiceInterval> _intervals = new HashMap<LocalizedServiceId, ServiceInterval>();

  public void addStopTime(LocalizedServiceId serviceId, int arrivalTime,
      int departureTime) {

    ServiceInterval interval = _intervals.get(serviceId);

    if (interval == null)
      interval = new ServiceInterval(arrivalTime, departureTime);
    else
      interval = interval.extend(arrivalTime, departureTime);

    _intervals.put(serviceId, interval);
  }

  public void addIntervals(ServiceIdIntervals intervals) {
    for (Map.Entry<LocalizedServiceId, ServiceInterval> entry : intervals) {
      LocalizedServiceId serviceId = entry.getKey();
      ServiceInterval interval = entry.getValue();
      addStopTime(serviceId, interval.getMinArrival(),
          interval.getMinDeparture());
      addStopTime(serviceId, interval.getMaxArrival(),
          interval.getMaxDeparture());
    }
  }

  public Set<LocalizedServiceId> getServiceIds() {
    return _intervals.keySet();
  }

  public ServiceInterval getIntervalForServiceId(LocalizedServiceId serviceId) {
    return _intervals.get(serviceId);
  }

  @Override
  public Iterator<Entry<LocalizedServiceId, ServiceInterval>> iterator() {
    return _intervals.entrySet().iterator();
  }

}

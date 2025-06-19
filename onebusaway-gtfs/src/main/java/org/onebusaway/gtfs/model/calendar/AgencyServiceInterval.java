/**
 * Copyright (C) 2024 Cambridge Systematics, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs.model.calendar;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represent a service interval (a period of defined transit both scheduled and dynamic) that exists
 * on a given service date and window relative to that date. The times may differ based on the
 * specified agency. The net effect is that the period of consideration can be smaller for more
 * frequent service (such as a subway/BRT provider) vs a traditional fixed bus service.
 */
public class AgencyServiceInterval implements Serializable {

  public static final int SECONDS_IN_DAY = 24 * 60 * 60;

  private final long _referenceTime;
  private final ServiceDate _serviceDate;

  /**
   * Map of overrides that have a different window of applicability of service relative to the
   * reference time. The default is the entire service date. Values should be AGENCY_ID,
   * MINUTES_AFTER_REFERENCE_TIME.
   */
  private final Map<String, Integer> _overridesByAgencyId = new HashMap<>();

  public AgencyServiceInterval(long referenceTime) {
    _referenceTime = referenceTime;
    _serviceDate = new ServiceDate(new Date(referenceTime));
  }

  public AgencyServiceInterval(ServiceDate serviceDate) {
    _referenceTime = serviceDate.getAsDate().getTime();
    _serviceDate = serviceDate;
  }

  public AgencyServiceInterval(long referenceTime, Map<String, Integer> agencyIdOverrides) {
    _referenceTime = referenceTime;
    _serviceDate = new ServiceDate(new Date(referenceTime));
    if (agencyIdOverrides != null) _overridesByAgencyId.putAll(agencyIdOverrides);
  }

  public ServiceDate getServiceDate() {
    return _serviceDate;
  }

  public ServiceInterval getServiceInterval(String agencyId) {

    if (_overridesByAgencyId.containsKey(agencyId)) {
      // override will be referenceTime, referenceTime+window (in minutes)
      ServiceDate serviceDate = new ServiceDate(new Date(_referenceTime));
      int startSecondsIntoDay =
          Math.toIntExact(_referenceTime - serviceDate.getAsDate().getTime()) / 1000;
      int endSecondsIntoDay = startSecondsIntoDay + (_overridesByAgencyId.get(agencyId) * 60);
      return new ServiceInterval(startSecondsIntoDay, endSecondsIntoDay);
    }
    // default will be 0, endOfDay (aka entire service day)
    return new ServiceInterval(0, SECONDS_IN_DAY);
  }

  public Date getFrom(String agencyId) {
    if (_overridesByAgencyId.containsKey(agencyId)) return new Date(_referenceTime);
    return new Date(_referenceTime);
  }

  public Date getTo(String agencyId) {
    if (_overridesByAgencyId.containsKey(agencyId))
      return new Date(_referenceTime + _overridesByAgencyId.get(agencyId) * 60 * 1000);
    return endOfDay(_serviceDate);
  }

  private Date endOfDay(ServiceDate serviceDate) {
    final Calendar cal = Calendar.getInstance();
    cal.setTime(serviceDate.getAsDate());
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 999);
    return cal.getTime();
  }

  private Date startOfDay(ServiceDate serviceDate) {
    final Calendar cal = Calendar.getInstance();
    cal.setTime(serviceDate.getAsDate());
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 000);
    return cal.getTime();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof AgencyServiceInterval)) {
      return false;
    }
    AgencyServiceInterval that = (AgencyServiceInterval) other;
    return that._referenceTime == _referenceTime;
  }

  @Override
  public int hashCode() {
    return new Long(_referenceTime).hashCode();
  }
}

/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.gtfs.services.calendar;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;

public interface CalendarServiceDataFactory {
  public CalendarServiceData createData();

  public CalendarServiceData updateData(
      Collection<Agency> allAgencies,
      Map<AgencyAndId, List<String>> tripAgencyIdsReferencingServiceId,
      Map<String, TimeZone> timeZoneMapByAgencyId);
}

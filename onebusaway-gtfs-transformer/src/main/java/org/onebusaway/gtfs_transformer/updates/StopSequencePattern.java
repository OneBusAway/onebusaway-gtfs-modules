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
/** */
package org.onebusaway.gtfs_transformer.updates;

import java.util.Arrays;
import java.util.List;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.StopTime;

public class StopSequencePattern {

  private final AgencyAndId[] _stopIds;
  private final int[] _arrivalTimes;
  private final int[] _departureTimes;

  public StopSequencePattern(AgencyAndId[] stopIds, int[] arrivalTimes, int[] departureTimes) {
    _stopIds = stopIds;
    _arrivalTimes = arrivalTimes;
    _departureTimes = departureTimes;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_arrivalTimes);
    result = prime * result + Arrays.hashCode(_departureTimes);
    result = prime * result + Arrays.hashCode(_stopIds);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    StopSequencePattern other = (StopSequencePattern) obj;
    if (!Arrays.equals(_arrivalTimes, other._arrivalTimes)) return false;
    if (!Arrays.equals(_departureTimes, other._departureTimes)) return false;
    if (!Arrays.equals(_stopIds, other._stopIds)) return false;
    return true;
  }

  public static StopSequencePattern getPatternForStopTimes(List<StopTime> stopTimes) {
    int n = stopTimes.size();
    AgencyAndId[] stopIds = new AgencyAndId[n];
    int[] arrivalTimes = new int[n];
    int[] departureTimes = new int[n];
    for (int i = 0; i < n; i++) {
      StopTime stopTime = stopTimes.get(i);
      stopIds[i] = stopTime.getStop().getId();
      arrivalTimes[i] = stopTime.getArrivalTime();
      departureTimes[i] = stopTime.getDepartureTime();
    }
    return new StopSequencePattern(stopIds, arrivalTimes, departureTimes);
  }
}

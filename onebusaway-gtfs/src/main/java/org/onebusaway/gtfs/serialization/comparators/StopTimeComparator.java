/**
 * 
 */
package org.onebusaway.gtfs.serialization.comparators;

import java.util.Comparator;

import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;

public class StopTimeComparator implements Comparator<StopTime>{

  @Override
  public int compare(StopTime o1, StopTime o2) {
    Trip trip1 = o1.getTrip();
    Trip trip2 = o2.getTrip();
    int c = trip1.getId().compareTo(trip2.getId());
    
    if( c == 0)
      c = o1.getStopSequence() - o2.getStopSequence();
    
    return c;
  }    
}
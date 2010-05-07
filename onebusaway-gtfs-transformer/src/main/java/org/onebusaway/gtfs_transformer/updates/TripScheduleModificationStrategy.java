package org.onebusaway.gtfs_transformer.updates;

import java.util.Date;
import java.util.Set;

import org.onebusaway.gtfs_transformer.king_county_metro.transforms.MetroKCServiceId;

public interface TripScheduleModificationStrategy {

  public Set<Date> getCancellations(MetroKCServiceId key, Set<Date> dates);

  public Set<Date> getAdditions(MetroKCServiceId key, Set<Date> dates);
}

package org.onebusaway.gtfs_transformer.updates;

import java.util.Date;
import java.util.Set;

//import org.onebusaway.king_county_metro_gtfs.model.MetroKCServiceId;
import sun.security.jca.ServiceId;

public interface TripScheduleModificationStrategy {

    public Set<Date> getCancellations(ServiceId key, Set<Date> dates);

    public Set<Date> getAdditions(ServiceId key, Set<Date> dates);
}

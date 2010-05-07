package org.onebusaway.gtfs_transformer.king_county_metro;

import java.util.Collection;

import org.onebusaway.gtfs.services.GenericMutableDao;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCBlockTrip;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCChangeDate;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCPatternPair;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCStop;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCStopTime;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCTrip;
import org.onebusaway.gtfs_transformer.model.VersionedId;

public interface MetroKCDao extends GenericMutableDao {

  public Collection<MetroKCChangeDate> getAllChangeDates();

  public MetroKCChangeDate getChangeDateForId(String changeDateId);

  public MetroKCStop getStopForId(int id);

  public Collection<MetroKCTrip> getAllTrips();

  public MetroKCTrip getTripForId(VersionedId id);

  public void removeTrip(MetroKCTrip trip);

  public Collection<MetroKCBlockTrip> getAllBlockTrips();

  public Collection<MetroKCStopTime> getAllStopTimes();

  public Collection<MetroKCPatternPair> getAllPatternPairs();
}

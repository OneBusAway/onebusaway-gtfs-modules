/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs.services;

import java.util.Collection;

import org.onebusaway.gtfs.model.*;

/**
 * Basic methods for accessing GTFS entities in bulk or by id.
 * 
 * @author bdferris
 */
public interface GtfsDao extends GenericDao {

  /****
   * Agency Methods
   ****/

  public Collection<Agency> getAllAgencies();

  public Agency getAgencyForId(String id);

  /****
   * {@link ServiceCalendar} Methods
   ****/

  public Collection<ServiceCalendar> getAllCalendars();

  public ServiceCalendar getCalendarForId(int id);

  /****
   * {@link ServiceCalendarDate} Methods
   ****/

  public Collection<ServiceCalendarDate> getAllCalendarDates();

  public ServiceCalendarDate getCalendarDateForId(int id);

  /****
   * {@link FareAttribute} Methods
   ****/

  public Collection<FareAttribute> getAllFareAttributes();

  public FareAttribute getFareAttributeForId(AgencyAndId id);

  /****
   * {@link FareRule} Methods
   ***/

  public Collection<FareRule> getAllFareRules();

  public FareRule getFareRuleForId(int id);

  /****
   * {@link FeedInfo} Methods
   ****/

  public Collection<FeedInfo> getAllFeedInfos();

  public FeedInfo getFeedInfoForId(String id);

  /****
   * {@link Frequency} Methods
   ****/

  public Collection<Frequency> getAllFrequencies();

  public Frequency getFrequencyForId(int id);

  /****
   * {@link Pathway} Methods
   ****/

  public Collection<Pathway> getAllPathways();

  public Pathway getPathwayForId(AgencyAndId id);

  public Collection<Level> getAllLevels();

  public Level getLevelForId(AgencyAndId id);

  /****
   * {@link Route} Methods
   ****/

  public Collection<Route> getAllRoutes();

  public Route getRouteForId(AgencyAndId id);

  /****
   * {@link ShapePoint} Methods
   ****/

  public Collection<ShapePoint> getAllShapePoints();

  public ShapePoint getShapePointForId(int id);

  /****
   * {@link Stop} Methods
   ****/

  public Collection<Stop> getAllStops();

  public Stop getStopForId(AgencyAndId id);

  /****
   * {@link StopTime} Methods
   ****/

  public Collection<StopTime> getAllStopTimes();

  public StopTime getStopTimeForId(int id);

  /****
   * {@link Transfer} Methods
   ****/

  public Collection<Transfer> getAllTransfers();

  public Transfer getTransferForId(int id);

  /****
   * {@link Trip} Methods
   ****/

  public Collection<Trip> getAllTrips();

  public Trip getTripForId(AgencyAndId id);

  public Collection<Block> getAllBlocks();

  public Block getBlockForId(int id);

  public Collection<Ridership> getAllRiderships();

  /****
   * {@link Area} Methods
   ****/

  public Collection<Area> getAllAreas();
}

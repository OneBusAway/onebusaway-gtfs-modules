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
package org.onebusaway.gtfs.services;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
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

  Collection<Agency> getAllAgencies();

  Agency getAgencyForId(String id);

  /****
   * {@link ServiceCalendar} Methods
   ****/

  Collection<ServiceCalendar> getAllCalendars();

  ServiceCalendar getCalendarForId(int id);

  /****
   * {@link ServiceCalendarDate} Methods
   ****/

  Collection<ServiceCalendarDate> getAllCalendarDates();

  ServiceCalendarDate getCalendarDateForId(int id);

  /****
   * {@link FareAttribute} Methods
   ****/

  Collection<FareAttribute> getAllFareAttributes();

  FareAttribute getFareAttributeForId(AgencyAndId id);

  /****
   * {@link FareLegRule } Methods
   ***/
  Collection<FareLegRule> getAllFareLegRules();

  /****
   * {@link FareProduct } Methods
   ***/

  Collection<FareProduct> getAllFareProducts();

  FareProduct getFareProductForId(AgencyAndId id);

  /****
   * {@link FareMedium } Methods
   ***/
  Collection<FareMedium> getAllFareMedia();

  /****
   * {@link RiderCategory} Methods
   ***/
  Collection<RiderCategory> getAllRiderCategories();

  /****
   * {@link FareRule} Methods
   ***/

  Collection<FareRule> getAllFareRules();

  FareRule getFareRuleForId(int id);

  /****
   * {@link FareTransferRule} Methods
   ***/

  Collection<FareTransferRule> getAllFareTransferRules();

  /****
   * {@link FeedInfo} Methods
   ****/

  Collection<FeedInfo> getAllFeedInfos();

  FeedInfo getFeedInfoForId(String id);

  /****
   * {@link Frequency} Methods
   ****/

  Collection<Frequency> getAllFrequencies();

  Frequency getFrequencyForId(int id);

  /****
   * {@link Pathway} Methods
   ****/

  Collection<Pathway> getAllPathways();

  Pathway getPathwayForId(AgencyAndId id);

  Collection<Level> getAllLevels();

  Level getLevelForId(AgencyAndId id);

  /****
   * {@link Route} Methods
   ****/

  Collection<Route> getAllRoutes();

  Collection<RouteStop> getAllRouteStops();

  Collection<RouteShape> getAllRouteShapes();

  Route getRouteForId(AgencyAndId id);

  /****
   * {@link ShapePoint} Methods
   ****/

  Collection<ShapePoint> getAllShapePoints();

  ShapePoint getShapePointForId(int id);

  /****
   * {@link Stop} Methods
   ****/

  Collection<Stop> getAllStops();

  Stop getStopForId(AgencyAndId id);

  /****
   * {@link StopTime} Methods
   ****/

  Collection<StopTime> getAllStopTimes();

  StopTime getStopTimeForId(int id);

  /****
   * {@link Transfer} Methods
   ****/

  Collection<Transfer> getAllTransfers();

  Transfer getTransferForId(int id);

  /****
   * {@link Trip} Methods
   ****/

  Collection<Trip> getAllTrips();

  Trip getTripForId(AgencyAndId id);

  Collection<Block> getAllBlocks();

  Block getBlockForId(int id);

  Collection<Ridership> getAllRiderships();

  /****
   * {@link Vehicle} Methods
   ****/
  Collection<Vehicle> getAllVehicles();

  /****
   * {@link Vehicle} Methods
   ****/
  Vehicle getVehicleForId(AgencyAndId id);

  /****
   * {@link Area} Methods
   ****/

  Collection<Area> getAllAreas();

  @Deprecated
  Collection<LocationGroupElement> getAllLocationGroupElements();

  Collection<LocationGroup> getAllLocationGroups();

  Collection<StopAreaElement> getAllStopAreaElements();

  Collection<Location> getAllLocations();

  Collection<BookingRule> getAllBookingRules();

  /****
   * {@link Translation} Methods
   ****/

  Collection<Translation> getAllTranslations();

  /****
   * {@link Network} Methods
   ****/

   Collection<Network> getAllNetworks();

  Collection<DirectionEntry> getAllDirectionEntries();

  default boolean hasFaresV1() {
    return Stream.of(getAllFareAttributes(), getAllFareRules())
        .flatMap(Collection::stream)
        .findAny()
        .isPresent();
  }

  default boolean hasFaresV2() {
    return Stream.of(getAllFareProducts(), getAllFareLegRules(), getAllFareTransferRules())
        .flatMap(Collection::stream)
        .findAny()
        .isPresent();
  }

  List<String> getOptionalMetadataFilenames();

  boolean hasMetadata(String filename);

  String getMetadata(String filename);

  void addMetadata(String filename, String content);
}

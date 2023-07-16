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
package org.onebusaway.gtfs.impl;

import java.io.Serializable;
import java.util.*;

import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.services.GenericMutableDao;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsMutableDao;

public class GtfsDaoImpl extends GenericDaoImpl implements GtfsMutableDao {

  public static final String[] OPTIONAL_FILE_NAMES = {"modifications.txt"};
  private StopTimeArray stopTimes = new StopTimeArray();

  private ShapePointArray shapePoints = new ShapePointArray();

  private boolean packStopTimes = false;

  private boolean packShapePoints = false;

  private List<String> _optionalMetadataFilenames = null;

  private Map<String, String> metadataByFilename = new HashMap<>();

  public GtfsDaoImpl() {
    _optionalMetadataFilenames = new ArrayList<>();
    if (OPTIONAL_FILE_NAMES != null) {
      for (String optionalFileName : OPTIONAL_FILE_NAMES) {
        _optionalMetadataFilenames.add(optionalFileName);
      }
    }
  }

  public boolean isPackStopTimes() {
    return packStopTimes;
  }

  public void setPackStopTimes(boolean packStopTimes) {
    this.packStopTimes = packStopTimes;
  }

  public boolean isPackShapePoints() {
    return packShapePoints;
  }

  public void setPackShapePoints(boolean packShapePoints) {
    this.packShapePoints = packShapePoints;
  }

  /***
   * {@link GtfsDao} Interface
   ****/

  public Agency getAgencyForId(String id) {
    return getEntityForId(Agency.class, id);
  }

  public Collection<Agency> getAllAgencies() {
    return getAllEntitiesForType(Agency.class);
  }
  
  public Collection<Block> getAllBlocks() {
    return getAllEntitiesForType(Block.class);
  }

  public Collection<ServiceCalendarDate> getAllCalendarDates() {
    return getAllEntitiesForType(ServiceCalendarDate.class);
  }

  public Collection<ServiceCalendar> getAllCalendars() {
    return getAllEntitiesForType(ServiceCalendar.class);
  }

  public Collection<FareAttribute> getAllFareAttributes() {
    return getAllEntitiesForType(FareAttribute.class);
  }

  public Collection<FareRule> getAllFareRules() {
    return getAllEntitiesForType(FareRule.class);
  }

  @Override
  public Collection<FeedInfo> getAllFeedInfos() {
    return getAllEntitiesForType(FeedInfo.class);
  }

  public Collection<Frequency> getAllFrequencies() {
    return getAllEntitiesForType(Frequency.class);
  }

  public Collection<Route> getAllRoutes() {
    return getAllEntitiesForType(Route.class);
  }

  public Collection<RouteStop> getAllRouteStops() {
    return getAllEntitiesForType(RouteStop.class);
  }

  public Collection<RouteShape> getAllRouteShapes() {
    return getAllEntitiesForType(RouteShape.class);
  }

  public Collection<ShapePoint> getAllShapePoints() {
    if (packShapePoints) {
      return shapePoints;
    }
    return getAllEntitiesForType(ShapePoint.class);
  }

  public Collection<StopTime> getAllStopTimes() {
    if (packStopTimes) {
      return stopTimes;
    }
    return getAllEntitiesForType(StopTime.class);
  }

  public Collection<Stop> getAllStops() {
    return getAllEntitiesForType(Stop.class);
  }

  public Collection<Transfer> getAllTransfers() {
    return getAllEntitiesForType(Transfer.class);
  }

  public Collection<Trip> getAllTrips() {
    return getAllEntitiesForType(Trip.class);
  }

  public Collection<Ridership> getAllRiderships() {
    return getAllEntitiesForType(Ridership.class);
  }

  public Collection<Vehicle> getAllVehicles() { return getAllEntitiesForType(Vehicle.class); }

  public Collection<Level> getAllLevels() {
    return getAllEntitiesForType(Level.class);
  }

  public Block getBlockForId(int id) {
    return getEntityForId(Block.class, id);
  }
  
  public ServiceCalendarDate getCalendarDateForId(int id) {
    return getEntityForId(ServiceCalendarDate.class, id);
  }

  public ServiceCalendar getCalendarForId(int id) {
    return getEntityForId(ServiceCalendar.class, id);
  }

  public FareAttribute getFareAttributeForId(AgencyAndId id) {
    return getEntityForId(FareAttribute.class, id);
  }

  @Override
  public Collection<FareLegRule> getAllFareLegRules() {
    return getAllEntitiesForType(FareLegRule.class);
  }

  @Override
  public Collection<FareProduct> getAllFareProducts() {
    return getAllEntitiesForType(FareProduct.class);
  }

  @Override
  public FareProduct getFareProductForId(AgencyAndId id) {
    return getEntityForId(FareProduct.class, id);
  }

  @Override
  public Collection<FareMedium> getAllFareMedia() {
    return getAllEntitiesForType(FareMedium.class);
  }

  @Override
  public Collection<RiderCategory> getAllRiderCategories() {
    return getAllEntitiesForType(RiderCategory.class);
  }

  public FareRule getFareRuleForId(int id) {
    return getEntityForId(FareRule.class, id);
  }

  @Override
  public Collection<FareTransferRule> getAllFareTransferRules() {
    return getAllEntitiesForType(FareTransferRule.class);
  }

  @Override
  public FeedInfo getFeedInfoForId(String id) {
    return getEntityForId(FeedInfo.class, id);
  }

  public Frequency getFrequencyForId(int id) {
    return getEntityForId(Frequency.class, id);
  }

  public Collection<Pathway> getAllPathways() {
    return getAllEntitiesForType(Pathway.class);
  }

  public Pathway getPathwayForId(AgencyAndId id) {
    return getEntityForId(Pathway.class, id);
  }

  public Route getRouteForId(AgencyAndId id) {
    return getEntityForId(Route.class, id);
  }

  public ShapePoint getShapePointForId(int id) {
    if (packShapePoints) {
      return shapePoints.get(id);
    }
    return getEntityForId(ShapePoint.class, id);
  }

  public Stop getStopForId(AgencyAndId id) {
    return getEntityForId(Stop.class, id);
  }

  public StopTime getStopTimeForId(int id) {
    if (packStopTimes) {
      return stopTimes.get(id);
    }
    return getEntityForId(StopTime.class, id);
  }

  public Transfer getTransferForId(int id) {
    return getEntityForId(Transfer.class, id);
  }

  public Trip getTripForId(AgencyAndId id) {
    return getEntityForId(Trip.class, id);
  }

  public Level getLevelForId(AgencyAndId id) {
    return getEntityForId(Level.class, id);
  }



  public Facility getFacilityForId(AgencyAndId id) { return getEntityForId(Facility.class, id);}
  public FacilityProperty getFacilityPropertiesForId(AgencyAndId id) { return getEntityForId(FacilityProperty.class, id);}
  public FacilityPropertyDefinition getFacilityPropertiesDefinitionsForId(AgencyAndId id) { return getEntityForId(FacilityPropertyDefinition.class, id);}
  public RouteNameException getRouteNameExceptionForId(AgencyAndId id) { return getEntityForId(RouteNameException.class, id);}
  public DirectionNameException getDirectionNameExceptionForId(AgencyAndId id) { return getEntityForId(DirectionNameException.class, id);}

  public Collection<DirectionEntry> getAllDirectionEntries() {
    return getAllEntitiesForType(DirectionEntry.class);
  }
  public Collection<Facility> getAllFacilities() {
    return getAllEntitiesForType(Facility.class);
  }
  public Collection<FacilityProperty> getAllFacilityProperties() {
    return getAllEntitiesForType(FacilityProperty.class);
  }
  public Collection<FacilityPropertyDefinition> getAllFacilityPropertyDefinitions() {
    return getAllEntitiesForType(FacilityPropertyDefinition.class);
  }
  public Collection<RouteNameException> getAllRouteNameExceptions() {
    return getAllEntitiesForType(RouteNameException.class);
  }
  public Collection<DirectionNameException> getAllDirectionNameExceptions() {
    return getAllEntitiesForType(DirectionNameException.class);
  }

  public Collection<WrongWayConcurrency> getAllWrongWayConcurrencies() {
    return getAllEntitiesForType(WrongWayConcurrency.class);
  }
  public Collection<Area> getAllAreas() {
    return getAllEntitiesForType(Area.class);
  }

  public Collection<LocationGroupElement> getAllLocationGroupElements() {
    return getAllEntitiesForType(LocationGroupElement.class);
  }

  public Collection<LocationGroup> getAllLocationGroups() {
    return getAllEntitiesForType(LocationGroup.class);
  }

  public Collection<Location> getAllLocations() {
    return getAllEntitiesForType(Location.class);
  }

  public Collection<BookingRule> getAllBookingRules() {
    return getAllEntitiesForType(BookingRule.class);
  }

  public Collection<Translation> getAllTranslations() {
    return getAllEntitiesForType(Translation.class);
  }

  @Override
  public Collection<StopArea> getAllStopAreas() {
    return getAllEntitiesForType(StopArea.class);
  }

  /****
   * {@link GenericMutableDao} Interface
   ****/

  @Override
  public <K, V> Map<K, V> getEntitiesByIdForEntityType(Class<K> keyType,
      Class<V> entityType) {
    noKeyCheck(keyType);
    return super.getEntitiesByIdForEntityType(keyType, entityType);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Collection<T> getAllEntitiesForType(Class<T> type) {
    if (packStopTimes && type.equals(StopTime.class)) {
      return (Collection<T>) stopTimes;
    } else if (packShapePoints && type.equals(ShapePoint.class)) {
      return (Collection<T>) shapePoints;
    }
    return super.getAllEntitiesForType(type);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getEntityForId(Class<T> type, Serializable id) {
    if (packStopTimes && type.equals(StopTime.class)) {
      return (T) stopTimes.get((Integer) id);
    } else if (packShapePoints && type.equals(ShapePoint.class)) {
      return (T) shapePoints.get((Integer) id);
    }
    return super.getEntityForId(type, id);
  }

  @Override
  public void saveEntity(Object entity) {
    if (packStopTimes && entity.getClass().equals(StopTime.class)) {
      stopTimes.add((StopTime) entity);
      return;
    } else if (packShapePoints && entity.getClass().equals(ShapePoint.class)) {
      shapePoints.add((ShapePoint) entity);
      return;
    }
    super.saveEntity(entity);
  }

  @Override
  public <T> void clearAllEntitiesForType(Class<T> type) {
    if (packStopTimes && type.equals(StopTime.class)) {
      stopTimes.clear();
      return;
    } else if (packShapePoints && type.equals(ShapePoint.class)) {
      shapePoints.clear();
      return;
    }
    super.clearAllEntitiesForType(type);
  }

  @Override
  public <K extends Serializable, T extends IdentityBean<K>> void removeEntity(
      T entity) {
    if (packStopTimes && entity.getClass().equals(StopTime.class)) {
      throw new UnsupportedOperationException();
    } else if (packShapePoints && entity.getClass().equals(ShapePoint.class)) {
      throw new UnsupportedOperationException();
    }
    super.removeEntity(entity);
  }

  @Override
  public void close() {
    if (packStopTimes) {
      stopTimes.trimToSize();
    }
    if (packShapePoints) {
      shapePoints.trimToSize();
    }
    super.close();
  }

  @Override
  public List<String> getOptionalMetadataFilenames() {
    return _optionalMetadataFilenames;
  }
  @Override
  public boolean hasMetadata(String filename) {
    return metadataByFilename.containsKey(filename);
  }
  @Override
  public String getMetadata(String filename) {
    return metadataByFilename.get(filename);
  }
  @Override
  public void addMetadata(String filename, String content) {
    metadataByFilename.put(filename, content);
    if (!_optionalMetadataFilenames.contains(filename))
      _optionalMetadataFilenames.add(filename);
  }


  /****
   * Private Methods
   ****/

  private <K> void noKeyCheck(Class<K> keyType) {
    if (packStopTimes && keyType.equals(StopTime.class)) {
      throw new UnsupportedOperationException();
    }
    if (packShapePoints && keyType.equals(ShapePoint.class)) {
      throw new UnsupportedOperationException();
    }
  }

}

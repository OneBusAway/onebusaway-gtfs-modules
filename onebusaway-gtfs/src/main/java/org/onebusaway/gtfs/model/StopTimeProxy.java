/**
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.gtfs.model;

/**
 * 
 * @author bdferris
 * 
 * @see StopTime#setProxy(StopTimeProxy)
 */
public interface StopTimeProxy {

  public Integer getId();

  public void setId(Integer id);

  public Trip getTrip();

  public Area getStartServiceArea();

  public void setStartServiceArea(Area area);

  public Area getEndServiceArea();

  public void setEndServiceArea(Area area);

  public void setTrip(Trip trip);

  public int getStopSequence();

  public void setStopSequence(int stopSequence);

  public StopLocation getStop();

  public StopLocation getLocation();
  public StopLocation getLocationGroup();

  public void setStop(StopLocation stop);

  public void setLocation(StopLocation stop);

  public void setLocationGroup(StopLocation stop);

  public boolean isArrivalTimeSet();

  public int getArrivalTime();

  public void setArrivalTime(int arrivalTime);

  public void clearArrivalTime();

  public boolean isDepartureTimeSet();

  public int getDepartureTime();

  public void setDepartureTime(int departureTime);

  public void clearDepartureTime();
  
  public boolean isTimepointSet();
  
  public int getTimepoint();
  
  public void setTimepoint(int timepoint);
  
  public void clearTimepoint();

  public String getStopHeadsign();

  public void setStopHeadsign(String headSign);

  public String getRouteShortName();

  public void setRouteShortName(String routeShortName);

  public int getPickupType();

  public void setPickupType(int pickupType);

  public int getDropOffType();

  public void setDropOffType(int dropOffType);

  public boolean isShapeDistTraveledSet();

  public double getShapeDistTraveled();

  public void setShapeDistTraveled(double shapeDistTraveled);

  public void clearShapeDistTraveled();

  public BookingRule getPickupBookingRule();

  public void setPickupBookingRule(BookingRule pickupBookingRule);

  public BookingRule getDropOffBookingRule();

  public void setDropOffBookingRule(BookingRule dropOffBookingRule);

  public double getMeanDurationFactor();
	
  public void setMeanDurationFactor(double meanDurationFactor);	
	
  public double getMeanDurationOffset();
	
  public void setMeanDurationOffset(double meanDurationOffset);
	
  public double getSafeDurationFactor();
	
  public void setSafeDurationFactor(double safeDurationFactor);

  public double getSafeDurationOffset();
	
  public void setSafeDurationOffset(double safeDurationOffset);

  public String getFreeRunningFlag();

  public void setFreeRunningFlag(String freeRunningFlag);

  public String getRawTripId();

  public void setRawTripId(String rawTripId);

  public String getRawStopId();

  public void setRawStopId(String rawStopId);
}

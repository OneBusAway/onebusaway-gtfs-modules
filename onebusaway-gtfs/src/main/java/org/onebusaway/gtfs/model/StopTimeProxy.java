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

  Integer getId();

  void setId(Integer id);

  Trip getTrip();

  void setTrip(Trip trip);

  int getStopSequence();

  void setStopSequence(int stopSequence);

  StopLocation getStop();

  StopLocation getLocation();
  StopLocation getLocationGroup();

  void setStop(StopLocation stop);

  void setLocation(StopLocation stop);

  void setLocationGroup(StopLocation stop);

  boolean isArrivalTimeSet();

  int getArrivalTime();

  void setArrivalTime(int arrivalTime);

  void clearArrivalTime();

  boolean isDepartureTimeSet();

  int getDepartureTime();

  void setDepartureTime(int departureTime);

  void clearDepartureTime();
  
  boolean isTimepointSet();
  
  int getTimepoint();
  
  void setTimepoint(int timepoint);
  
  void clearTimepoint();

  String getStopHeadsign();

  void setStopHeadsign(String headSign);

  String getRouteShortName();

  void setRouteShortName(String routeShortName);

  int getPickupType();

  void setPickupType(int pickupType);

  int getDropOffType();

  void setDropOffType(int dropOffType);

  boolean isShapeDistTraveledSet();

  double getShapeDistTraveled();

  void setShapeDistTraveled(double shapeDistTraveled);

  void clearShapeDistTraveled();

  BookingRule getPickupBookingRule();

  void setPickupBookingRule(BookingRule pickupBookingRule);

  BookingRule getDropOffBookingRule();

  void setDropOffBookingRule(BookingRule dropOffBookingRule);

  double getMeanDurationFactor();
	
  void setMeanDurationFactor(double meanDurationFactor);	
	
  double getMeanDurationOffset();
	
  void setMeanDurationOffset(double meanDurationOffset);
	
  double getSafeDurationFactor();
	
  void setSafeDurationFactor(double safeDurationFactor);

  double getSafeDurationOffset();
	
  void setSafeDurationOffset(double safeDurationOffset);

}

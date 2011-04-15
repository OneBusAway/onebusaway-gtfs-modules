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
package org.onebusaway.gtfs.model;

public final class Frequency extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  private int id;

  private Trip trip;

  private int startTime;

  private int endTime;

  private int headwaySecs;

  private int exactTimes = 0;

  public Frequency() {

  }

  public Frequency(Frequency f) {
    this.id = f.id;
    this.trip = f.trip;
    this.startTime = f.startTime;
    this.endTime = f.endTime;
    this.headwaySecs = f.headwaySecs;
    this.exactTimes = f.exactTimes;
  }

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  public Trip getTrip() {
    return trip;
  }

  public void setTrip(Trip trip) {
    this.trip = trip;
  }

  public int getStartTime() {
    return startTime;
  }

  public void setStartTime(int startTime) {
    this.startTime = startTime;
  }

  public int getEndTime() {
    return endTime;
  }

  public void setEndTime(int endTime) {
    this.endTime = endTime;
  }

  public int getHeadwaySecs() {
    return headwaySecs;
  }

  public void setHeadwaySecs(int headwaySecs) {
    this.headwaySecs = headwaySecs;
  }

  public int getExactTimes() {
    return exactTimes;
  }

  public void setExactTimes(int exactTimes) {
    this.exactTimes = exactTimes;
  }

  public String toString() {
    return "<Frequency " + getId() + ">";
  }
}

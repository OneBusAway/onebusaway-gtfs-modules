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

public final class Stop extends IdentityBean<AgencyAndId> {

  private static final long serialVersionUID = 1L;

  private AgencyAndId id;

  private String code;

  private String name;

  private String desc;

  private double lat;

  private double lon;

  private String zoneId;

  private String url;

  private int locationType;

  private String parentStation;

  private int wheelchairBoarding = 0;
  
  private String direction;

  public Stop() {

  }

  public Stop(Stop obj) {
    this.id = obj.id;
    this.code = obj.code;
    this.name = obj.name;
    this.desc = obj.desc;
    this.lat = obj.lat;
    this.lon = obj.lon;
    this.zoneId = obj.zoneId;
    this.url = obj.url;
    this.locationType = obj.locationType;
    this.parentStation = obj.parentStation;
    this.wheelchairBoarding = obj.wheelchairBoarding;
    this.direction = obj.direction;
  }

  public AgencyAndId getId() {
    return id;
  }

  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLon() {
    return lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  public String getZoneId() {
    return zoneId;
  }

  public void setZoneId(String zoneId) {
    this.zoneId = zoneId;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getLocationType() {
    return locationType;
  }

  public void setLocationType(int locationType) {
    this.locationType = locationType;
  }

  public String getParentStation() {
    return parentStation;
  }

  public void setParentStation(String parentStation) {
    this.parentStation = parentStation;
  }

  @Override
  public String toString() {
    return "<Stop " + this.id + ">";
  }

  public void setWheelchairBoarding(int wheelchairBoarding) {
    this.wheelchairBoarding = wheelchairBoarding;
  }

  public int getWheelchairBoarding() {
    return wheelchairBoarding;
  }

  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }
}

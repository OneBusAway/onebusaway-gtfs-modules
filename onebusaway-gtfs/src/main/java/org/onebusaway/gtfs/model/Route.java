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

public final class Route extends IdentityBean<AgencyAndId> {

  private static final long serialVersionUID = 1L;

  private AgencyAndId id;

  private Agency agency;

  private String shortName;

  private String longName;

  private String desc;

  private int type;

  private String url;

  private String color;

  private String textColor;

  private int bikesAllowed = 0;

  public Route() {

  }

  public Route(Route r) {
    this.id = r.id;
    this.agency = r.agency;
    this.shortName = r.shortName;
    this.longName = r.longName;
    this.desc = r.desc;
    this.type = r.type;
    this.url = r.url;
    this.color = r.color;
    this.textColor = r.textColor;
    this.bikesAllowed = r.bikesAllowed;
  }

  public AgencyAndId getId() {
    return id;
  }

  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public Agency getAgency() {
    return agency;
  }

  public void setAgency(Agency agency) {
    this.agency = agency;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public String getLongName() {
    return longName;
  }

  public void setLongName(String longName) {
    this.longName = longName;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getTextColor() {
    return textColor;
  }

  public void setTextColor(String textColor) {
    this.textColor = textColor;
  }

  public int getBikesAllowed() {
      return bikesAllowed;
  }

  public void setBikesAllowed(int bikesAllowed) {
      this.bikesAllowed = bikesAllowed;
  }

  @Override
  public String toString() {
    return "<Route " + id + " " + shortName + ">";
  }
}

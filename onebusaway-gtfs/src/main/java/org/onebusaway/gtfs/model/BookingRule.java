/**
 * Copyright (C) 2020 Victor Pavlushkov <victor@kyyti.com>
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

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.StopTimeFieldMappingFactory;

@CsvFields(filename = "booking_rules.txt", required = false)
public final class BookingRule extends IdentityBean<AgencyAndId> {

  @CsvField(name = "booking_rule_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId id;

  @CsvField(name = "booking_type")
  private int type;

  @CsvField(optional = true)
  private int priorNoticeDurationMin;

  @CsvField(optional = true)
  private int priorNoticeDurationMax;

  @CsvField(optional = true)
  private int priorNoticeLastDay;

  @CsvField(optional = true, mapping = StopTimeFieldMappingFactory.class)
  private int priorNoticeLastTime;

  @CsvField(optional = true)
  private int priorNoticeStartDay;

  @CsvField(optional = true, mapping = StopTimeFieldMappingFactory.class)
  private int priorNoticeStartTime;

  @CsvField(optional = true, mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId priorNoticeServiceId;

  @CsvField(optional = true)
  private String message;

  @CsvField(optional = true)
  private String pickupMessage;

  @CsvField(optional = true)
  private String dropOffMessage;

  @CsvField(optional = true)
  private String phoneNumber;

  @CsvField(optional = true)
  private String infoUrl;

  @CsvField(name = "booking_url", optional = true)
  private String url;

  public BookingRule() {

  }

  public BookingRule(BookingRule br) {
    this.id = br.id;
    this.type = br.type;
    this.priorNoticeDurationMin = br.priorNoticeDurationMin;
    this.priorNoticeDurationMax = br.priorNoticeDurationMax;
    this.priorNoticeLastDay = br.priorNoticeLastDay;
    this.priorNoticeLastTime = br.priorNoticeLastTime;
    this.priorNoticeStartDay = br.priorNoticeStartDay;
    this.priorNoticeStartTime = br.priorNoticeStartTime;
    this.priorNoticeServiceId = br.priorNoticeServiceId;
    this.message = br.message;
    this.pickupMessage = br.pickupMessage;
    this.dropOffMessage = br.dropOffMessage;
    this.phoneNumber = br.phoneNumber;
    this.infoUrl = br.infoUrl;
    this.url = br.url;
  }

  @Override
  public AgencyAndId getId() {
    return id;
  }

  @Override
  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public int getPriorNoticeDurationMin() {
    return priorNoticeDurationMin;
  }

  public void setPriorNoticeDurationMin(int priorNoticeDurationMin) {
    this.priorNoticeDurationMin = priorNoticeDurationMin;
  }

  public int getPriorNoticeDurationMax() {
    return priorNoticeDurationMax;
  }

  public void setPriorNoticeDurationMax(int priorNoticeDurationMax) {
    this.priorNoticeDurationMax = priorNoticeDurationMax;
  }

  public int getPriorNoticeLastDay() {
    return priorNoticeLastDay;
  }

  public void setPriorNoticeLastDay(int priorNoticeLastDay) {
    this.priorNoticeLastDay = priorNoticeLastDay;
  }

  public int getPriorNoticeLastTime() {
    return priorNoticeLastTime;
  }

  public void setPriorNoticeLastTime(int priorNoticeLastTime) {
    this.priorNoticeLastTime = priorNoticeLastTime;
  }

  public int getPriorNoticeStartDay() {
    return priorNoticeStartDay;
  }

  public void setPriorNoticeStartDay(int priorNoticeStartDay) {
    this.priorNoticeStartDay = priorNoticeStartDay;
  }

  public int getPriorNoticeStartTime() {
    return priorNoticeStartTime;
  }

  public void setPriorNoticeStartTime(int priorNoticeStartTime) {
    this.priorNoticeStartTime = priorNoticeStartTime;
  }

  public AgencyAndId getPriorNoticeServiceId() {
    return priorNoticeServiceId;
  }

  public void setPriorNoticeServiceId(AgencyAndId priorNoticeServiceId) {
    this.priorNoticeServiceId = priorNoticeServiceId;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getPickupMessage() {
    return pickupMessage;
  }

  public void setPickupMessage(String pickupMessage) {
    this.pickupMessage = pickupMessage;
  }

  public String getDropOffMessage() {
    return dropOffMessage;
  }

  public void setDropOffMessage(String dropOffMessage) {
    this.dropOffMessage = dropOffMessage;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getInfoUrl() {
    return infoUrl;
  }

  public void setInfoUrl(String infoUrl) {
    this.infoUrl = infoUrl;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Override
  public String toString() {
    return "<BookingRule " + this.id + ">";
  }
}
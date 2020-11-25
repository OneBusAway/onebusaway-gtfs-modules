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

@CsvFields(filename = "booking_rules.txt", required = false)
public final class BookingRule extends IdentityBean<AgencyAndId> {

  public static final int MISSING_VALUE = -999;

  @CsvField(name = "booking_rule_id", mapping = EntityFieldMappingFactory.class)
  private String id;

  /** booking_type
   (Enum, Required) Defines how much in advance the booking can be made. Value are:
   0: Real-time booking only
   1: Up to same-day booking, with advance notice
   2: Up to prior day(s) booking
   */
  @CsvField(name = "booking_type", mapping = EntityFieldMappingFactory.class)
  private int type;

  /**
   prior_notice_duration_min
   (Integer, Conditionally Required) Minimum number of minutes of advance time necessary before travel to make a booking request.

   Conditionally Required:
   (The timing must be provided by either a duration or a last time & day)
   Required for up-to-same-day booking (booking_type=1)..
   Forbidden otherwise.
   */
  @CsvField(name = "prior_notice_duration_min", optional = true, mapping = EntityFieldMappingFactory.class)
  private int priorNoticeDurationMin;

  /**
   prior_notice_duration_max
   (Integer, Conditionally Optional) Maximum number of minutes of advance time necessary before travel to make a booking request.

   Conditionally Optional:
   Optional for up-to-same-day booking (booking_type=1)
   Forbidden otherwise.
   */
  @CsvField(name = "prior_notice_duration_max", optional = true, mapping = EntityFieldMappingFactory.class)
  private int priorNoticeDurationMax;

  /**
   prior_notice_last_day
   (Integer, Conditionally Required) Latest day on which a booking can be made. Defined as an offset, so the number of service days in advance of the booking.

   Example: “Ride must be booked 1 day in advance before 5PM” will be encoded as prior_notice_last_day=1.

   Conditionally Required:
   (The timing must be provided by either a duration or a last time & day)
   Required for up-to-prior-day booking (booking_type=2).
   Forbidden otherwise.
   */
  @CsvField(name = "prior_notice_last_day", optional = true, mapping = EntityFieldMappingFactory.class)
  private int priorNoticeLastDay;

  /**
   prior_notice_last_time
   (Time, Conditionally Required) Latest time of day on the last day on which a booking can be made. The timezone used is the one defined by agency.agency_timezone.

   Example: “Ride must be booked 1 day in advance before 5PM” will be encoded as prior_notice_last_time=17:00:00.

   Conditionally Required:
   Required if prior_notice_last_day is defined.
   Forbidden otherwise.
   */
  @CsvField(name = "prior_notice_last_time", optional = true, mapping = EntityFieldMappingFactory.class)
  private int priorNoticeLastTime = MISSING_VALUE;

  /**
   prior_notice_start_day
   (Integer, Conditionally Optional) Earliest day on which a booking can be made. Defined as an offset, so the number of service days in advance of the booking.

   Example: “Ride can be booked at the earliest one week in advance at midnight” will be encoded as prior_notice_start_day=7.

   Conditionally Optional:
   Optional for up-to-prior-day booking (booking_type=2).
   Optional for up-to-same-day booking (booking_type=1) if prior_notice_duration_max is empty.
   Forbidden otherwise.
   */
  @CsvField(name = "prior_notice_start_day", optional = true, mapping = EntityFieldMappingFactory.class)
  private int priorNoticeStartDay;

  /**
   prior_notice_start_time
   (Time, Conditionally Required) Earliest time of the earliest day at which a booking can be made. The timezone used is the one defined by agency.agency_timezone.

   Example: “Ride can be booked at the earliest one week in advance at midnight” will be encoded as prior_notice_start_time=00:00:00.

   Conditionally Required:
   Required if prior_notice_start_day is defined.
   Forbidden otherwise.
   */
  @CsvField(name = "prior_notice_start_time", optional = true, mapping = EntityFieldMappingFactory.class)
  private int priorNoticeStartTime = MISSING_VALUE;

  /**
   prior_notice_service_id
   (ID from calendar.txt, Optional) When prior_notice_start_day is used, prior_notice_service_id defines a subset of days that count towards the prior_notice_start_day.

   Example: If empty, prior_notice_start_day=2 will be two calendar days in advance. If defined as a service_id containing only business days (weekdays without holidays), prior_notice_start_day=2 will be two business days in advance.
   */
  @CsvField(name = "prior_notice_service_id", optional = true, mapping = ???.class)
  private ??? priorNoticeService;

  /**
   message
   (Text, Optional) Message to passengers utilizing service at a stop_time with this boarding rule. This message appears when the rider is booking a demand responsive pickup and drop off.

   The message is meant to provide minimal information to be transmitted within a user interface about the action a rider must take in order to utilize the service. This text is expected to be a link within user interfaces, whether to a phone number, a url, or a deep link to an app.
   */
  @CsvField(name = "message", optional = true)
  private String message;

  /**
   pickup_message
   (Text, Optional) Identical to message but used when riders have a demand response pickup only.
   */
  @CsvField(name = "pickup_message", optional = true)
  private String pickupMessage;

  /**
   drop_off_message
   (Text, Optional) Identical to message but used when riders have a demand response drop off only.
   */
  @CsvField(name = "drop_off_message", optional = true)
  private String dropOffMessage;

  /**
   phone_number
   (Phone number, Optional) Phone number to make the booking. Must follow the  E.123 standard (e.g. “+1 503 238 7433” for TriMet).
   */
  @CsvField(name = "phone_number", optional = true)
  private String phoneNumber;

  /**
   info_url
   (URL, Optional) The info_url field contains a URL providing human readable information about the booking rule.
   */
  @CsvField(name = "infoUrl", optional = true)
  private String infoUrl;

  /**
   booking_url
   (URL, Optional) If a rider can book trips according to this booking rule through an online interface or app, the link to that reservation system or app download page is the booking_url.
   */
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
    this.priorNoticeService = br.priorNoticeService;
    this.message = br.message;
    this.pickupMessage = br.pickupMessage;
    this.dropOffMessage = br.dropOffMessage;
    this.phoneNumber = br.phoneNumber;
    this.infoUrl = br.infoUrl;
    this.url = br.url;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
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

  public ??? getPriorNoticeService() {
    return priorNoticeService;
  }

  pubblic void setPriorNoticeService(??? priorNoticeService) {
    this.priorNoticeService = priorNoticeService;
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
}
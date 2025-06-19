/**
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.mappings.ServiceDateFieldMappingFactory;

@CsvFields(filename = "feed_info.txt", required = false, prefix = "feed_")
public final class FeedInfo extends IdentityBean<String> {

  private static final long serialVersionUID = 2L;

  @CsvField(optional = true)
  private String id = "1";

  private String publisherName;

  private String publisherUrl;

  private String lang;

  @CsvField(optional = true, mapping = ServiceDateFieldMappingFactory.class)
  private ServiceDate startDate;

  @CsvField(optional = true, mapping = ServiceDateFieldMappingFactory.class)
  private ServiceDate endDate;

  @CsvField(optional = true)
  private String version;

  @CsvField(optional = true, name = "default_lang")
  private String defaultLang;

  @CsvField(optional = true)
  private String contactEmail;

  @CsvField(optional = true)
  private String contactUrl;

  public FeedInfo() {}

  public FeedInfo(FeedInfo fi) {
    this.id = fi.id;
    this.publisherName = fi.publisherName;
    this.publisherUrl = fi.publisherUrl;
    this.lang = fi.lang;
    this.startDate = fi.startDate;
    this.endDate = fi.endDate;
    this.version = fi.version;
    this.defaultLang = fi.defaultLang;
    this.contactEmail = fi.contactEmail;
    this.contactUrl = fi.contactUrl;
  }

  public String getPublisherName() {
    return publisherName;
  }

  public void setPublisherName(String publisherName) {
    this.publisherName = publisherName;
  }

  public String getPublisherUrl() {
    return publisherUrl;
  }

  public void setPublisherUrl(String publisherUrl) {
    this.publisherUrl = publisherUrl;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public ServiceDate getStartDate() {
    return startDate;
  }

  public void setStartDate(ServiceDate startDate) {
    this.startDate = startDate;
  }

  public ServiceDate getEndDate() {
    return endDate;
  }

  public void setEndDate(ServiceDate endDate) {
    this.endDate = endDate;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getDefaultLang() {
    return defaultLang;
  }

  public void setDefaultLang(String defaultLang) {
    this.defaultLang = defaultLang;
  }

  public String getContactEmail() {
    return contactEmail;
  }

  public void setContactEmail(String contactEmail) {
    this.contactEmail = contactEmail;
  }

  public String getContactUrl() {
    return contactUrl;
  }

  public void setContactUrl(String contactUrl) {
    this.contactUrl = contactUrl;
  }

  /****
   * {@link IdentityBean} Interface
   ****/

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }
}

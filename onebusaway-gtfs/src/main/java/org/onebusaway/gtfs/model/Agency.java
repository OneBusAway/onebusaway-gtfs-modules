/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2011 Google, Inc.
 * Copyright (C) 2015 University of South Florida (cagricetin@mail.usf.edu)
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

import java.io.Serial;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.AgencyIdTranslationFieldMappingFactory;

@CsvFields(filename = "agency.txt", prefix = "agency_")
public final class Agency extends IdentityBean<String> {

  @Serial private static final long serialVersionUID = 2L;

  @CsvField(optional = true, mapping = AgencyIdTranslationFieldMappingFactory.class)
  private String id;

  private String name;

  private String url;

  private String timezone;

  @CsvField(optional = true)
  private String lang;

  @CsvField(optional = true)
  private String phone;

  @CsvField(optional = true)
  private String fareUrl;

  @CsvField(optional = true)
  private String email;

  public Agency() {}

  public Agency(Agency a) {
    this.id = a.id;
    this.name = a.name;
    this.url = a.url;
    this.timezone = a.timezone;
    this.lang = a.lang;
    this.phone = a.phone;
    this.email = a.email;
    this.fareUrl = a.fareUrl;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getFareUrl() {
    return fareUrl;
  }

  public void setFareUrl(String fareUrl) {
    this.fareUrl = fareUrl;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String toString() {
    return "<Agency " + this.id + ">";
  }
}

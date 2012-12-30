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
package org.onebusaway.gtfs_transformer.services;

import java.util.HashMap;
import java.util.Map;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.serialization.GtfsReader;

public class TransformContext {

  private String _defaultAgencyId;

  private GtfsReader _reader;

  private Map<String, Object> _parameters = new HashMap<String, Object>();

  public void setDefaultAgencyId(String agencyId) {
    _defaultAgencyId = agencyId;
  }

  public String getDefaultAgencyId() {
    return _defaultAgencyId;
  }

  public AgencyAndId resolveId(Class<?> entityType, String rawId) {
    return new AgencyAndId(_reader.getDefaultAgencyId(), rawId);
  }

  public GtfsReader getReader() {
    return _reader;
  }

  public void setReader(GtfsReader reader) {
    _reader = reader;
  }

  public void putParameter(String key, Object value) {
    _parameters.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T getParameter(String key) {
    return (T) _parameters.get(key);
  }
}

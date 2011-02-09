package org.onebusaway.gtfs_transformer.services;

import java.util.HashMap;
import java.util.Map;

public class TransformContext {

  private String _defaultAgencyId;

  private Map<String, Object> _parameters = new HashMap<String, Object>();

  public void setDefaultAgencyId(String agencyId) {
    _defaultAgencyId = agencyId;
  }

  public String getDefaultAgencyId() {
    return _defaultAgencyId;
  }

  public void putParameter(String key, Object value) {
    _parameters.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T getParameter(String key) {
    return (T) _parameters.get(key);
  }
}

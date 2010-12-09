package org.onebusaway.gtfs.csv.schema;

import org.apache.commons.beanutils.Converter;

public class DefaultConverter implements Converter {

  @Override
  public Object convert(@SuppressWarnings("rawtypes") Class type, Object value) {
    if (value == null)
      return "";
    return value.toString();
  }
}

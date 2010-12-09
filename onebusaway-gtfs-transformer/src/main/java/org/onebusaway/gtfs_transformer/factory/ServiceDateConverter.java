package org.onebusaway.gtfs_transformer.factory;

import org.apache.commons.beanutils.Converter;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

public class ServiceDateConverter implements Converter {

  @Override
  public Object convert(@SuppressWarnings("rawtypes") Class type, Object value) {
    return ServiceDate.parseString((String) value);
  }
}

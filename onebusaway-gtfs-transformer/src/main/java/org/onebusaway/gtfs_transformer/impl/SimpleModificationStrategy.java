package org.onebusaway.gtfs_transformer.impl;

import java.io.Serializable;
import java.util.Map;

import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class SimpleModificationStrategy extends
    AbstractEntityModificationStrategy {

  private Map<String, Object> _propertyUpdates;

  public SimpleModificationStrategy(Map<String, Object> propertyMatches,
      Map<String, Object> propertyUpdates) {
    super(propertyMatches);
    _propertyUpdates = propertyUpdates;
  }

  public void run(TransformContext context, GtfsMutableRelationalDao dao,
      BeanWrapper entity) {

    if (!isModificationApplicable(entity))
      return;

    for (Map.Entry<String, Object> entry : _propertyUpdates.entrySet()) {
      String property = entry.getKey();
      Object value = entry.getValue();
      Class<?> propertyType = entity.getPropertyType(property);
      if (IdentityBean.class.isAssignableFrom(propertyType))
        value = dao.getEntityForId(propertyType, (Serializable) value);
      entity.setPropertyValue(property, value);
    }
  }
}

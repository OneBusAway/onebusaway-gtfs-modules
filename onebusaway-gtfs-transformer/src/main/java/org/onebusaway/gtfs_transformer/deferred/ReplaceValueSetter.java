package org.onebusaway.gtfs_transformer.deferred;

import org.onebusaway.csv_entities.schema.BeanWrapper;

/**
 * A {@link ValueSetter} that can do string-replacement operations on a bean
 * value.
 */
public class ReplaceValueSetter implements ValueSetter {

  private String matchRegex;
  private String replacementValue;

  public ReplaceValueSetter(String matchRegex, String replacementValue) {
    this.matchRegex = matchRegex;
    this.replacementValue = replacementValue;
  }

  @Override
  public void setValue(BeanWrapper bean, String propertyName) {
    Object value = bean.getPropertyValue(propertyName);
    if (value == null) {
      return;
    }
    String stringValue = value.toString();
    String updatedValue = stringValue.replaceAll(matchRegex, replacementValue);
    if (!stringValue.equals(updatedValue)) {
      bean.setPropertyValue(propertyName, updatedValue);
    }
  }
}

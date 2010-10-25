package org.onebusaway.gtfs.serialization;

import org.onebusaway.gtfs.csv.exceptions.CsvEntityException;

/**
 * Indicates that a GTFS entity has a reference to another entity that has not
 * been seen before in the feed and that we don't know how to specify a default
 * agency id for that entity as result.
 * 
 * @author bdferris
 * 
 */
public class EntityReferenceNotFoundException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  public EntityReferenceNotFoundException(Class<?> entityType, String entityId) {
    super(entityType, "entity reference not found: type="
        + entityType.getName() + " id=" + entityId);
  }
}

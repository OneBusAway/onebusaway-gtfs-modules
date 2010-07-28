package org.onebusaway.gtfs.serialization;

import org.onebusaway.gtfs.csv.exceptions.CsvEntityException;
import org.onebusaway.gtfs.model.AgencyAndId;

/**
 * Indicates that two entities with the same id were found in a GTFS feed as it
 * was being read.
 * 
 * @author bdferris
 * 
 */
public class DuplicateEntityException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  public DuplicateEntityException(Class<?> entityType, AgencyAndId id) {
    super(entityType, "duplicate entity id: type=" + entityType.getName()
        + " agencyId=" + id.getAgencyId() + " id=" + id.getId());
  }
}

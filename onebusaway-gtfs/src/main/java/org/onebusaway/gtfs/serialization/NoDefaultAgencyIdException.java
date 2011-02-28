package org.onebusaway.gtfs.serialization;

import org.onebusaway.csv_entities.exceptions.CsvEntityException;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;

/**
 * Indicates that no default agency id (see
 * {@link GtfsReaderContext#getDefaultAgencyId()} was specified for a particular
 * feed. Recall that {@link GtfsReader} will attempt to fill in the
 * {@link AgencyAndId#setAgencyId(String)} agencyId value for all entities
 * loaded by the reader so as to make entity ids unique across feeds. For
 * entities such as {@link Route} and {@link Trip}, the agency id will be
 * resolved automatically to any referenced {@link Agency} in the {@link Route}.
 * However, for all other entities, we need some agency id. By default, the
 * {@link Agency#getId()} of the first {@link Agency} loaded by the reader will
 * be used. However, you may also specify a default agency with a call to
 * {@link GtfsReader#setDefaultAgencyId(String)}.
 * 
 * @author bdferris
 * @see GtfsReaderContext#getDefaultAgencyId()
 * @see GtfsReader#setDefaultAgencyId(String)
 */
public class NoDefaultAgencyIdException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  public NoDefaultAgencyIdException() {
    super(Agency.class, "no default agency id was found for the gtfs feed");
  }
}

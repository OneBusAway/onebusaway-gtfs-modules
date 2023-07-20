package org.onebusaway.gtfs.serialization;

import java.io.File;
import java.io.IOException;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

public class BaseGtfsTest {

  /****
   * Private Methods
   ****/

  public static GtfsRelationalDao processFeed(
          File resourcePath, String agencyId,
          boolean internStrings
  ) throws IOException {

    GtfsReader reader = new GtfsReader();
    reader.setDefaultAgencyId(agencyId);
    reader.setInternStrings(internStrings);

    reader.setInputLocation(resourcePath);

    GtfsRelationalDaoImpl entityStore = new GtfsRelationalDaoImpl();
    entityStore.setGenerateIds(true);
    reader.setEntityStore(entityStore);

    reader.run();
    return entityStore;
  }
}

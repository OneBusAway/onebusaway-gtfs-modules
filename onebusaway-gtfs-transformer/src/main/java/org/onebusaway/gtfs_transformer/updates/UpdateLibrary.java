package org.onebusaway.gtfs_transformer.updates;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;

public class UpdateLibrary {
  public static void clearDaoCache(GtfsMutableRelationalDao dao) {
    if (dao instanceof GtfsRelationalDaoImpl) {
      GtfsRelationalDaoImpl daoImpl = (GtfsRelationalDaoImpl) dao;
      daoImpl.clearAllCaches();
    }
  }
}

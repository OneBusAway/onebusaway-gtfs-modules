package org.onebusaway.gtfs_transformer.services;

import org.onebusaway.gtfs_transformer.king_county_metro.MetroKCDao;


public class TransformContext {

  private String _defaultAgencyId;

  private MetroKCDao _metroKCDao;

  public void setDefaultAgencyId(String agencyId) {
    _defaultAgencyId = agencyId;
  }

  public String getDefaultAgencyId() {
    return _defaultAgencyId;
  }

  public void setMetroKCDao(MetroKCDao metroKCDao) {
    _metroKCDao = metroKCDao;
  }

  public MetroKCDao getMetroKCDao() {
    return _metroKCDao;
  }
}

package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;

@CsvFields(filename = "route_networks.txt")
public final class RouteNetworkAssignment extends IdentityBean<String> {

  @CsvField(name = "route_id")
  private String routeId;

  @CsvField(name = "network_id")
  private String networkId;

  private String getRouteId() {
    return routeId;
  }

  private void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  private String getNetworkId() {
    return networkId;
  }

  private void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getId() {
    return routeId + "|" + networkId;
  }

  @Override
  public void setId(String id) {}
}

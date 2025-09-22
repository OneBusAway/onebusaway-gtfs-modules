package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;

@CsvFields(filename = "route_networks.txt", required = false)
public final class RouteNetworkAssignment extends IdentityBean<String> {

  @CsvField(name = "route_id", mapping = EntityFieldMappingFactory.class)
  private Route route;

  @CsvField(name = "network_id")
  private String networkId;

  public Route getRoute() {
    return route;
  }

  public void setRoute(Route routeId) {
    this.route = routeId;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getId() {
    return route.getId() + "|" + networkId;
  }

  @Override
  public void setId(String id) {}
}

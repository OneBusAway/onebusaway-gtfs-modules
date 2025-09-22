package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;

@CsvFields(filename = "route_networks.txt")
public final class RouteNetworkAssignment extends IdentityBean<String> {

  @CsvField(name = "route_id", optional = true, mapping = EntityFieldMappingFactory.class)
  private Route route;

  @CsvField(name = "network_id")
  private String networkId;

  private Route getRouteId() {
    return route;
  }

  private void setRouteId(Route routeId) {
    this.route = routeId;
  }

  private String getNetworkId() {
    return networkId;
  }

  private void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getId() {
    return route.getId() + "|" + networkId;
  }

  @Override
  public void setId(String id) {}
}

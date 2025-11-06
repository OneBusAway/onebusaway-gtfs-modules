/**
 * Copyright (C) 2017 Cambridge Systematics, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_transformer.impl;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.csv_entities.CSVLibrary;
import org.onebusaway.csv_entities.CSVListener;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareAttribute;
import org.onebusaway.gtfs.model.FareRule;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopMatrixFareModificationStrategy implements GtfsTransformStrategy {

  private static final Logger _log =
      LoggerFactory.getLogger(StopMatrixFareModificationStrategy.class);

  private String routeId;

  private String csvUrl;

  private int initialFareId;

  @CsvField(optional = true)
  private String agencyId;

  @CsvField(optional = true)
  private String currencyType;

  @CsvField(optional = true)
  private int paymentMethod = -1;

  @CsvField(optional = true)
  private int transfers = -1;

  @CsvField(optional = true)
  private int transferDuration = -1;

  @CsvField(optional = true)
  private float youthPrice = -1;

  @CsvField(optional = true)
  private float seniorPrice = -1;

  @CsvField(ignore = true)
  private Route route;

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public void setCsvUrl(String csvUrl) {
    this.csvUrl = csvUrl;
  }

  public void setInitialFareId(int initialFareId) {
    this.initialFareId = initialFareId;
  }

  public void setCurrencyType(String currencyType) {
    this.currencyType = currencyType;
  }

  public void setPaymentMethod(int paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public void setTransfers(int transfers) {
    this.transfers = transfers;
  }

  public void setTransferDuration(int transferDuration) {
    this.transferDuration = transferDuration;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public void setYouthPrice(float youthPrice) {
    this.youthPrice = youthPrice;
  }

  public void setSeniorPrice(float seniorPrice) {
    this.seniorPrice = seniorPrice;
  }

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    // remove rules for route
    for (FareRule rule : new HashSet<FareRule>(dao.getAllFareRules())) {
      if (rule.getRoute() != null && rule.getRoute().getId().getId().equals(routeId)) {
        if (!isExemplarSet()) {
          setAttributesFromExemplar(rule.getFare());
        }
        route = rule.getRoute();
        dao.removeEntity(rule);
      }
    }
    for (FareAttribute attr : new HashSet<FareAttribute>(dao.getAllFareAttributes())) {
      if (dao.getFareRulesForFareAttribute(attr).isEmpty()) {
        dao.removeEntity(attr);
      }
    }

    // add new rules
    FareCreationListener listener = new FareCreationListener();
    listener.setDao(dao);
    try {
      URL url = new URL(csvUrl);
      try (InputStream is = url.openStream()) {
        new CSVLibrary().parse(is, listener);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    listener.flushNewFares();
  }

  private void setAttributesFromExemplar(FareAttribute attr) {
    agencyId = attr.getId().getAgencyId();
    currencyType = attr.getCurrencyType();
    paymentMethod = attr.getPaymentMethod();
    transfers = attr.getTransfers();
    if (attr.isTransferDurationSet()) {
      transferDuration = attr.getTransferDuration();
    }
  }

  private boolean isExemplarSet() {
    return agencyId != null || currencyType != null || paymentMethod != -1 || transfers != -1;
  }

  private class FareCreationListener implements CSVListener {

    private List<String> stopIds;

    private Map<Float, FareAttribute> newFareAttributes = new HashMap<>();

    private Map<Pair<String>, FareRule> newFareRules = new HashMap<>();

    int fareId = initialFareId;

    private GtfsMutableRelationalDao dao;

    @Override
    public void handleLine(List<String> list) throws Exception {
      if (stopIds == null) {
        stopIds = list;
        return;
      }
      String orig = list.getFirst();
      for (int i = 1; i < list.size(); i++) {
        String dest = stopIds.get(i);
        if (!orig.equals(dest)) {
          float price = Float.parseFloat(list.get(i));
          addNewFareRule(orig, dest, price);
        }
      }
    }

    public void flushNewFares() {
      for (FareAttribute attr : newFareAttributes.values()) {
        dao.saveEntity(attr);
      }
      for (FareRule rule : newFareRules.values()) {
        dao.saveEntity(rule);
      }
    }

    public void setDao(GtfsMutableRelationalDao dao) {
      this.dao = dao;
    }

    private void addNewFareRule(String orig, String dest, float price) {
      FareAttribute attr = getFareAttributeForCost(price);
      String origZone = getZoneForStopId(orig);
      String destZone = getZoneForStopId(dest);
      Pair<String> key = Tuples.pair(origZone, destZone);

      FareRule rule = newFareRules.get(key);
      if (rule != null) {
        if (rule.getFare().getId().equals(attr.getId())) {
          _log.debug("Fare rule already exists for stops {} -> {}, skipping", orig, dest);
        } else {
          throw new RuntimeException(
              "Invalid fare matrix: stops with the same zone given different prices (zone "
                  + origZone
                  + ", "
                  + destZone
                  + ")");
        }
      }

      rule = new FareRule();
      rule.setFare(attr);
      rule.setRoute(route);
      rule.setOriginId(origZone);
      rule.setDestinationId(destZone);
      newFareRules.put(key, rule);
    }

    private FareAttribute getFareAttributeForCost(float price) {
      if (newFareAttributes.get(price) != null) {
        return newFareAttributes.get(price);
      }
      FareAttribute attr = new FareAttribute();
      attr.setId(new AgencyAndId(agencyId, Integer.toString(fareId++)));
      attr.setCurrencyType(currencyType);
      attr.setPaymentMethod(paymentMethod);
      attr.setTransfers(transfers);
      attr.setTransferDuration(transferDuration);
      attr.setPrice(price);
      // these columns are non-standard, this piece may need to be refactored in the future
      if (youthPrice != -1) {
        attr.setYouthPrice(youthPrice);
      }
      if (seniorPrice != -1) {
        attr.setSeniorPrice(seniorPrice);
      }
      newFareAttributes.put(price, attr);
      return attr;
    }

    private String getZoneForStopId(String stopId) {
      Stop stop = dao.getStopForId(new AgencyAndId(agencyId, stopId));
      if (stop == null) {
        return null;
      }
      if (stop.getZoneId() == null || "".equals(stop.getZoneId())) {
        _log.info("Stop {} does not have zone, setting zone to stop_id", stop);
        stop.setZoneId(stop.getId().getId());
        dao.updateEntity(stop);
      }
      return stop.getZoneId();
    }
  }
}

/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.CloudContextService;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

import org.onebusaway.gtfs_transformer.util.CalendarFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Compare GTFS trips and report on gaps at a depot level.
 * The concept of a depot is inferred from the trip id and is
 * currently specific to the MTA.
 */
public class CompareToReferenceService implements GtfsTransformStrategy {

  private static final int MAX_MESSAGE_SIZE = 250 * 1024;
  private static final Logger _log = LoggerFactory.getLogger(CompareToReferenceService.class);
  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @CsvField(ignore = true)
  private String defaultAgencyId = "1";

  private String s3BasePath = null;
  public void setS3BasePath(String path) {
    s3BasePath = path;
  }

  @CsvField(ignore = true)
  private CalendarFunctions helper = new CalendarFunctions();
  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao gtfsDao) {
    try {

      defaultAgencyId = CloudContextService.getLikelyFeedName(gtfsDao);
      String summaryTopic = CloudContextService.getTopic() + "-atis-summary";
      String detailTopic = CloudContextService.getTopic() + "-atis-detail";

      String summaryHeader = "depot,unmatched_gtfs_trips,unmatched_reference_trips\n";
      String detailHeader = "depot,unmatched_gtfs_trip_ds,unmatched_reference_trip_ids\n";

      StringBuffer summaryReport = new StringBuffer();
      StringBuffer detailReport = new StringBuffer();

      GtfsMutableRelationalDao referenceDao = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();

      // get active trip ids for service date from base GTFS
      Map<String, List<AgencyAndId>> activeTripsByDepot = getTripsByDepot(context, gtfsDao);
      // get active trip ids for service date for reference GTFS
      Map<String, List<AgencyAndId>> referenceTripsByDepot = getTripsByDepot(context, referenceDao);
      String summaryFilename = System.getProperty("java.io.tmpdir") + File.separator + "summary.csv";
      FileWriter summaryFile = new FileWriter(summaryFilename);
      summaryFile.write(summaryHeader);
      String detailFilename = System.getProperty("java.io.tmpdir") + File.separator + "detail.csv";
      FileWriter detailFile = new FileWriter(detailFilename);
      detailFile.write(detailHeader);

      Set<String> allDepots = new HashSet<>();
      allDepots.addAll(activeTripsByDepot.keySet());
      allDepots.addAll(referenceTripsByDepot.keySet());

      for (String depot : allDepots) {
        HashSet<AgencyAndId> gtfsTripIds = new HashSet<>();
        HashSet<AgencyAndId> referenceTripIds = new HashSet<>();
        if (activeTripsByDepot.containsKey(depot)) {
          gtfsTripIds.addAll(activeTripsByDepot.get(depot));
        }
        if (referenceTripsByDepot.containsKey(depot)) {
          referenceTripIds.addAll(referenceTripsByDepot.get(depot));
        }

        // now do some set operations to determine the differences
        HashSet<AgencyAndId> unmatchedReferenceTrips = new HashSet<>(referenceTripIds);
        HashSet<AgencyAndId> unmatchedGtfsTrips = new HashSet<>(gtfsTripIds);

        unmatchedGtfsTrips.removeAll(referenceTripIds);

        unmatchedReferenceTrips.removeAll(gtfsTripIds);
        summaryReport.append(depot).append(",").append(unmatchedGtfsTrips.size()).append(",").append(unmatchedReferenceTrips.size()).append("\n");
        detailReport.append(depot).append(",\"").append(unmatchedGtfsTrips).append("\",\"").append(unmatchedReferenceTrips).append("\"\n");
//        es.publishMessage(detailTopic, truncate(detailReport.toString()));
        detailFile.write(detailReport.toString());
        detailReport = new StringBuffer();
      }
//      es.publishMessage(summaryTopic, summaryReport.toString());
      summaryFile.write(summaryReport.toString());

      summaryFile.close();
      detailFile.close();

      // now copy the reports to an S3 reports directory
      Calendar cal = Calendar.getInstance();
      String year = "" + cal.get(Calendar.YEAR);
      String month = lpad(cal.get(Calendar.MONTH)+1, 2);
      String day = lpad(cal.get(Calendar.DAY_OF_MONTH), 2);
      String time = lpad(cal.get(Calendar.HOUR_OF_DAY), 2) + ":" + lpad(cal.get(Calendar.MINUTE),2 );
      String baseurl = s3BasePath
              + "/" + year + "/" + month + "/" + day + "/" + time + "-";


      _log.error("{} Unmatched Summary", getName());
      _log.error(summaryReport.toString());
    } catch (Throwable t) {
      _log.error("{} failed: {}", getName(), t, t);
    }

  }

  private String lpad(int numberToFormat, int totalDigits) {
    String text = String.valueOf(numberToFormat);
    while (text.length() < totalDigits)
      text = "0" + text;
    return text;
  }

  private String truncate(String message) {
    if (message.length() > MAX_MESSAGE_SIZE)
      return message.substring(0, MAX_MESSAGE_SIZE);
    return message;
  }

  private String getDepot(String tripId) {
    String depot = null;
    if (tripId.indexOf("-") < 0) {
      depot = "MISSING";
    } else {
      depot = tripId.split("-")[1];
      if ("Weekday".equals(depot) || "Saturday".equals(depot) || "Sunday".equals(depot))
        depot = tripId.split("-")[0]; // it moves around!
    }
    if (depot.length() <= 1) {
      depot = "MISSING";
    }
    return depot;
  }


  private Map<String, List<AgencyAndId>> getTripsByDepot(TransformContext context, GtfsMutableRelationalDao dao) {
    Map<String, List<AgencyAndId>> tripsByDepot = new HashMap<>();
    for (Trip trip : dao.getAllTrips()) {
      if (trip.getServiceId() != null) {
        boolean isActive = helper.isTripActive(dao, new ServiceDate(), trip, true);
        if (isActive) {
          AgencyAndId tripId = trip.getId();
          if (trip.getMtaTripId() != null) {
            tripId = new AgencyAndId(trip.getId().getAgencyId(), trip.getMtaTripId());
          }
          String depot = getDepot(tripId.getId());
          if (!tripsByDepot.containsKey(depot))
            tripsByDepot.put(depot, new ArrayList<>());
          tripsByDepot.get(depot).add(sanitize(tripId));
        }
      }
    }
    return tripsByDepot;
  }



  private AgencyAndId sanitize(AgencyAndId tripId) {
    if (tripId.getId().contains("-SDon")) {
      return new AgencyAndId(defaultAgencyId, tripId.getId().replaceAll("-SDon", ""));
    }
    return new AgencyAndId(defaultAgencyId, tripId.getId());
  }
}

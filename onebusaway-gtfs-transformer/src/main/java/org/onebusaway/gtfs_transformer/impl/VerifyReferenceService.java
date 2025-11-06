/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import org.onebusaway.csv_entities.CSVLibrary;
import org.onebusaway.csv_entities.CSVListener;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.gtfs_transformer.services.CloudContextService;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* Checks the numbers of Routes running on two input GTFS files and compares them.
 * Used for comparing ATIS to Reference GTFS and reporting differences of Reference routes that are missing service
 * Looks at Routes running today through next 3 days
 */
public class VerifyReferenceService implements GtfsTransformStrategy {
  private final Logger _log = LoggerFactory.getLogger(VerifyReferenceService.class);

  @CsvField(optional = true)
  private String problemRoutesFile;

  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    GtfsMutableRelationalDao reference =
        (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();
    CalendarService refCalendarService = CalendarServiceDataFactoryImpl.createService(reference);
    String feed = CloudContextService.getLikelyFeedName(reference);

    Collection<String> problemRoutes;
    ProblemRouteListener listener = new ProblemRouteListener();

    try {
      if (problemRoutesFile != null) {
        InputStream is = new BufferedInputStream(new FileInputStream(problemRoutesFile));
        new CSVLibrary().parse(is, listener);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    problemRoutes = listener.returnRouteIds();

    for (String route : problemRoutes) {
      _log.info("ProblemRoutes includes: " + route);
    }

    int[] tripsToday;
    int[] tripsTomorrow;
    int[] tripsNextDay;
    int[] tripsDayAfterNext;
    Date today = removeTime(new Date());
    Date tomorrow = removeTime(addDays(new Date(), 1));
    Date nextDay = removeTime(addDays(new Date(), 2));
    Date dayAfterNext = removeTime(addDays(new Date(), 3));

    tripsToday = hasRouteServiceForDate(dao, reference, refCalendarService, today, problemRoutes);
    tripsTomorrow =
        hasRouteServiceForDate(dao, reference, refCalendarService, tomorrow, problemRoutes);
    tripsNextDay =
        hasRouteServiceForDate(dao, reference, refCalendarService, nextDay, problemRoutes);
    tripsDayAfterNext =
        hasRouteServiceForDate(dao, reference, refCalendarService, dayAfterNext, problemRoutes);

    _log.info("Feed for metrics: {}", feed);
    _log.info(
        "Active routes {}: {}, {}: {}, {}: {}, {}: {}",
        today,
        tripsToday,
        tomorrow,
        tripsTomorrow,
        nextDay,
        tripsNextDay,
        dayAfterNext,
        tripsDayAfterNext);
  }

  int[] hasRouteServiceForDate(
      GtfsMutableRelationalDao dao,
      GtfsMutableRelationalDao reference,
      CalendarService refCalendarService,
      Date testDate,
      Collection<String> problemRoutes) {
    AgencyAndId daoAgencyAndId = dao.getAllTrips().iterator().next().getId();

    int numRoutesWithTrips = 0;
    int activeRoutes = 0;
    int alarmingRoutes = 0;

    // check for route specific current service
    for (Route route : reference.getAllRoutes()) {
      numRoutesWithTrips = 0;

      ServiceDate sDate = createServiceDate(testDate);
      triploop:
      for (Trip trip : reference.getTripsForRoute(route)) {
        for (ServiceDate calDate :
            refCalendarService.getServiceDatesForServiceId(trip.getServiceId())) {
          if (calDate.equals(sDate)) {
            _log.info("Reference has service for route: {} on {}", route.getId().getId(), testDate);
            numRoutesWithTrips++;
            activeRoutes++;
            break triploop;
          }
        }
      }
      if (numRoutesWithTrips == 0) {
        _log.error("No service for {} on {}", route.getId().getId(), testDate);
        // if there is no current service, check that it should have service
        // there are certain routes that don't run on the weekend or won't have service
        Route atisRoute =
            dao.getRouteForId(new AgencyAndId(daoAgencyAndId.getAgencyId(), route.getId().getId()));
        if (atisRoute == null) {
          atisRoute =
              dao.getRouteForId(
                  new AgencyAndId(daoAgencyAndId.getAgencyId(), route.getShortName()));
        }
        reftriploop:
        for (Trip atisTrip : dao.getTripsForRoute(atisRoute)) {
          for (ServiceCalendarDate calDate :
              dao.getCalendarDatesForServiceId(atisTrip.getServiceId())) {
            Date date = constructDate(calDate.getDate());
            if (date.equals(testDate)) {
              if (calDate.getExceptionType() == 1) {
                if (problemRoutes.contains(route.getId().getId())) {
                  _log.info(
                      "On {} ATIS has service for this route but Reference has none: {}. Excused route, no further action",
                      testDate,
                      route.getId());
                  break reftriploop;
                } else {
                  _log.info(
                      "On {} ATIS has service for this route but Reference has none: {}",
                      testDate,
                      route.getId());
                  alarmingRoutes++;
                  break reftriploop;
                }
              }
            }
          }
        }
      }
    }
    return new int[] {activeRoutes, alarmingRoutes};
  }

  private Date constructDate(ServiceDate date) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, date.getYear());
    calendar.set(Calendar.MONTH, date.getMonth() - 1);
    calendar.set(Calendar.DATE, date.getDay());
    Date date1 = calendar.getTime();
    date1 = removeTime(date1);
    return date1;
  }

  private Date removeTime(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    date = calendar.getTime();
    return date;
  }

  private Date addDays(Date date, int daysToAdd) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.DATE, daysToAdd);
    return cal.getTime();
  }

  private ServiceDate createServiceDate(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return new ServiceDate(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH));
  }

  public void setProblemRoutesFile(String problemRoutesFile) {
    this.problemRoutesFile = problemRoutesFile;
  }

  private static class ProblemRouteListener implements CSVListener {

    private Collection<String> routeIds = new HashSet<>();

    @Override
    public void handleLine(List<String> list) {
      if (routeIds == null) {
        routeIds = list;
        return;
      }
      routeIds.add(list.getFirst());
    }

    private Collection<String> returnRouteIds() {
      return routeIds;
    }
  }
}

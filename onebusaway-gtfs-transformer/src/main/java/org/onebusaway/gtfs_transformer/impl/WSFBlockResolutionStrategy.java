/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.stream.StreamSource;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.calendar.CalendarServiceDataFactory;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.wa.wsdot.ferries.schedule.SchedResponse;
import gov.wa.wsdot.ferries.schedule.SchedTerminalCombo;
import gov.wa.wsdot.ferries.schedule.SchedTime;

public class WSFBlockResolutionStrategy implements GtfsTransformStrategy {

  private static final Logger _log = LoggerFactory.getLogger(
      WSFBlockResolutionStrategy.class);

  @CsvField(ignore=true)
  private GtfsMutableRelationalDao _dao;
  
  @CsvField(ignore=true)
  private String _agencyId;
  
  @CsvField(ignore=true)
  private TimeZone _agencyTimeZone;
  
  @CsvField(ignore=true)
  private WSFTripResolutionService _tripResolutionService;
  
  @CsvField(ignore=true)
  private WSFScheduleService _scheduleService;
  
  private String apiAccessCode = "";
  

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    _dao = dao;

    Agency agency = dao.getAllAgencies().iterator().next();
    _agencyId = agency.getId();
    _agencyTimeZone = TimeZone.getTimeZone(agency.getTimezone());

    _tripResolutionService = new WSFTripResolutionService(_dao, _agencyId,
        _agencyTimeZone);

    try {
      _scheduleService = new WSFScheduleService(apiAccessCode);
      setAllBlockIds();
    } catch (Exception e) {
      _log.error("Error initializing WSFBlockResolutionStrategy: " + e);
      return;
    }

  }

  public void setApiAccessCode(String apiAccessCode) {
    this.apiAccessCode = apiAccessCode;
  }

  private void setAllBlockIds() throws InterruptedException {
    Collection<Trip> trips = _dao.getAllTrips();

    Map<BlockTask, BlockTask> tripsMap = new HashMap<BlockTask, BlockTask>();
    for (Trip t : trips) {

      BlockTask task = new BlockTask(t);

      // No data for trips in past
      if (task.isInPast()) {
        continue;
      }

      if (!tripsMap.containsKey(task)) {
        tripsMap.put(task, task);
      }
      task = tripsMap.get(task);

      task.addTrip(t);
    }

    Set<BlockTask> jobs = tripsMap.keySet();

    // Oversubscribe cores since we are network-bound
    int nCores = Runtime.getRuntime().availableProcessors();
    ExecutorService executor = Executors.newFixedThreadPool(nCores * 2);

    executor.invokeAll(jobs);

  }

  private void setBlockIdsFromSchedResponse(SchedResponse resp) {
    List<SchedTerminalCombo> combos = resp.getTerminalCombos().getValue().getSchedTerminalCombo();
    for (SchedTerminalCombo stc : combos) {

      String depart = stc.getDepartingTerminalID().toString();
      String arrive = stc.getArrivingTerminalID().toString();

      for (SchedTime sched : schedTime(stc)) {
        long time = ts(sched.getDepartingTime());
        Trip trip = _tripResolutionService.resolve(depart, time, arrive);
        if (trip != null) {
          trip.setBlockId(sched.getVesselID().toString());
        } else {
          _log.warn("Skipping schedTime due to no matching trip {}",
              sched.toString());
        }
      }
    }
  }

  private String id(Stop st) {
    return st.getId().getId();
  }

  private Date date(ServiceCalendar cal) {
    return cal.getStartDate().getAsDate(_agencyTimeZone);
  }

  private List<SchedTime> schedTime(SchedTerminalCombo st) {
    return st.getTimes().getValue().getSchedTime();
  }

  // From WSFRealtimeProvider
  private long ts(XMLGregorianCalendar xgc) {
    GregorianCalendar gc = xgc.toGregorianCalendar(_agencyTimeZone, null, null);
    return (gc.getTimeInMillis() / 1000L);
  }

  /**
   * We need to query the API for each service date, for each stop pair (route).
   * This class allows us to construct an index of trips that have the same
   * service date and route. In addition, it is a Callable so that tasks can be
   * run asynchronously.
   *
   */
  class BlockTask implements Callable<Boolean> {
    Route route;
    ServiceCalendar cal;
    List<Trip> trips;

    BlockTask(Trip t) {
      this.route = t.getRoute();
      this.cal = _dao.getCalendarForServiceId(t.getServiceId());
    }

    @Override
    public Boolean call() {
      List<StopTime> stops = _dao.getStopTimesForTrip(trips.get(0));
      Stop orig = stops.get(0).getStop(), dest = stops.get(1).getStop();

      _log.info("Submitting WSF block task for {} ({}, {})", cal, orig, dest);
      SchedResponse resp = _scheduleService.getSchedule(date(cal), id(orig),
          id(dest));

      setBlockIdsFromSchedResponse(resp);

      return true;
    }

    void addTrip(Trip t) {
      if (trips == null) {
        trips = new ArrayList<Trip>();
      }
      trips.add(t);
    }

    boolean isInPast() {
      Calendar today = Calendar.getInstance();
      today.set(Calendar.HOUR_OF_DAY, 0);
      today.set(Calendar.MINUTE, 0);
      today.set(Calendar.SECOND, 0);
      today.set(Calendar.MILLISECOND, 0);

      return date(cal).before(today.getTime());
    }

    @Override
    public int hashCode() {
      return route.hashCode() * 17 + cal.getServiceId().hashCode() * 31;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof BlockTask))
        return false;
      BlockTask k = (BlockTask) o;
      if (!this.route.equals(k.route))
        return false;
      if (!this.cal.equals(k.cal))
        return false;

      return true;
    }
  }

}

/**
 * Adapted from TripResolutionService in wsf-gtfsrealtime
 */
class WSFTripResolutionService {

  private static final Logger _log = LoggerFactory.getLogger(
      WSFTripResolutionService.class);

  GtfsRelationalDao _dao;
  String _agencyId;
  CalendarServiceData _csd;
  TimeZone _agencyTimeZone;

  int _maxStopTime;

  public WSFTripResolutionService(GtfsRelationalDao dao, String agencyId,
      TimeZone agencyTimeZone) {
    _dao = dao;
    _agencyId = agencyId;
    _agencyTimeZone = agencyTimeZone;

    CalendarServiceDataFactory factory = new CalendarServiceDataFactoryImpl(
        _dao);
    _csd = factory.createData();
    
    _maxStopTime = calculateMaxStopTime();
  }

  public Trip resolve(String departingTerminalId, long departureTime,
      String arrivingTerminalId) {
    ServiceDate initialServiceDate = new ServiceDate(
        new Date(departureTime * 1000));
    int lookBackDays = (_maxStopTime / 86400) + 1;

    AgencyAndId stopId = new AgencyAndId(_agencyId, departingTerminalId);
    AgencyAndId routeId = new AgencyAndId(_agencyId,
        departingTerminalId + arrivingTerminalId);

    for (StopTime st : _dao.getAllStopTimes()) {
      if (st.getStop().getId().equals(stopId)
          && st.getTrip().getRoute().getId().equals(routeId)) {

        ServiceDate sd = initialServiceDate;
        for (int i = 0; i < lookBackDays; i++) {

          if (_csd.getServiceIdsForDate(sd).contains(
              st.getTrip().getServiceId())
              && st.getDepartureTime() == (departureTime
                  - (sd.getAsCalendar(_agencyTimeZone).getTimeInMillis()
                      / 1000))) {

            return st.getTrip();
          }

          sd = sd.previous();
        }

      }
    }

    _log.warn("no trip found for resolve(departId=" + departingTerminalId
        + ", departureTime=" + departureTime + ", arrivalId="
        + arrivingTerminalId + ")");
    return null;

  }

  private int calculateMaxStopTime() {
    Set<Integer> times = new HashSet<Integer>();

    for (StopTime st : _dao.getAllStopTimes()) {
      if (st.isArrivalTimeSet()) {
        times.add(st.getArrivalTime());
      }
      if (st.isDepartureTimeSet()) {
        times.add(st.getDepartureTime());
      }
    }

    return Collections.max(times);
  }
}

/**
 * Query the WSF API for a schedule response.
 */
class WSFScheduleService {

  private static Logger _log = LoggerFactory.getLogger(
      WSFScheduleService.class);

  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(
      "yyyy-MM-dd");

  private String _apiAccessCode;

  // Context is thread-safe. Unmarshaller is not.
  JAXBContext _jc;

  public WSFScheduleService(String apiAccessCode) throws JAXBException {
    _jc = JAXBContext.newInstance(SchedResponse.class);
    _apiAccessCode = apiAccessCode;
  }

  public SchedResponse getSchedule(Date serviceDate, String departTerminal,
      String arriveTerminal) {
    long start = System.currentTimeMillis();
    StringBuffer url = new StringBuffer(
        "http://www.wsdot.wa.gov/ferries/api/schedule/rest/schedule");
    url.append("/" + formatDate(serviceDate));
    url.append("/" + departTerminal);
    url.append("/" + arriveTerminal);
    url.append("?apiaccesscode=" + _apiAccessCode);

    try {
      URLConnection conn = new URL(url.toString()).openConnection();
      conn.setRequestProperty("Accept", "text/xml");

      InputStream is = conn.getInputStream();
      try {
        JAXBElement<SchedResponse> resp = _jc.createUnmarshaller().unmarshal(
            new StreamSource(is), SchedResponse.class);

        return resp.getValue();
      } finally {
        is.close();
        long finish = System.currentTimeMillis();
        _log.info("wsf call complete in " + (finish-start)/1000 + "s for call=" + url);
      }
    } catch (Exception e) {
      _log.error("Exception processing WSF API: " + e);
      return null;
    }

  }

  private static String formatDate(Date date) {
    return DATE_FORMATTER.format(date);
  }
}
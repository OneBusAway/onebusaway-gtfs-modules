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

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import org.onebusaway.csv_entities.CSVLibrary;
import org.onebusaway.csv_entities.CSVListener;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnomalyCheckFutureTripCounts implements GtfsTransformStrategy {

  private final Logger _log = LoggerFactory.getLogger(AnomalyCheckFutureTripCounts.class);

  @CsvField(optional = true)
  private String datesToIgnoreUrl; // a sample url might be

  // "https://raw.githubusercontent.com/wiki/caylasavitzky/onebusaway-gtfs-modules/Testing-pulling-problem-routes.md";

  @CsvField(optional = true)
  private String datesToIgnoreFile;

  @CsvField(optional = true)
  private String holidaysUrl;

  @CsvField(optional = true)
  private String holidaysFile;

  @CsvField(optional = true)
  private String dayAvgTripMapUrl;

  @CsvField(optional = true)
  private String dayAvgTripMapFile;

  @CsvField(optional = true)
  private double percentageMatch = 10;

  @CsvField(optional = true)
  private boolean silentMode = true;

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    Collection<Date> datesToIgnore;
    SetListener datesToIgnoreListener = new SetListener();
    datesToIgnoreListener =
        (SetListener) readCsvFrom(datesToIgnoreListener, datesToIgnoreUrl, datesToIgnoreFile);
    datesToIgnore = datesToIgnoreListener.returnContents();

    Collection<Date> holidays;
    SetListener holidaysListener = new SetListener();
    holidaysListener = (SetListener) readCsvFrom(holidaysListener, holidaysUrl, holidaysFile);
    holidays = holidaysListener.returnContents();

    Map<String, Double> dayAvgTripsMap = new HashMap<String, Double>();
    MapListener mapListener = new MapListener();
    mapListener = (MapListener) readCsvFrom(mapListener, dayAvgTripMapUrl, dayAvgTripMapFile);
    dayAvgTripsMap = mapListener.returnContents();

    SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE");
    for (Date date : holidays) {
      _log.info("holiday: " + date.toString() + " on a " + dayOfWeekFormat.format(date));
    }
    for (Date date : datesToIgnore) {
      _log.info("ignore: " + date.toString());
    }
    for (String key : dayAvgTripsMap.keySet()) {
      _log.info("key: " + key + " value: " + dayAvgTripsMap.get(key));
    }

    String[] days = {
      "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Holiday"
    };

    Map<Date, Integer> dateTripMap = getDateTripMap(dao);

    int dayCounter = 0;
    Map<String, ArrayList<Double>> dayAvgTripsMapUpdate = new HashMap<String, ArrayList<Double>>();

    for (String day : days) {
      dayAvgTripsMapUpdate.put(day, new ArrayList<Double>());
    }
    Date dateDay = removeTime(addDays(new Date(), dayCounter));
    while (dateTripMap.get(dateDay) != null) {
      int tripCount = dateTripMap.get(dateDay);
      if (dayAvgTripMapFile == null && dayAvgTripMapUrl == null) {
        _log.info("On {} there are {} trips", dateDay.toString(), tripCount);
      } else {
        if ((tripCount
                < dayAvgTripsMap.get(dayOfWeekFormat.format(dateDay)) * (1 + percentageMatch / 100))
            && (tripCount
                > dayAvgTripsMap.get(dayOfWeekFormat.format(dateDay)) * (1 - percentageMatch / 100))
            && !holidays.contains(dateDay)) {
          _log.info(
              dateDay + " has " + tripCount + " trips, and that's within reasonable expections");
          dayAvgTripsMapUpdate.get(dayOfWeekFormat.format(dateDay)).add((double) tripCount);
        } else if (holidays.contains(dateDay)
            & (tripCount < dayAvgTripsMap.get("Holiday") * (1 + percentageMatch / 100)
                && (tripCount > dayAvgTripsMap.get("Holiday") * (1 - percentageMatch / 100)))) {
          _log.info(
              dateDay
                  + " has "
                  + tripCount
                  + " trips, is a holiday, and that's within reasonable expections");
          dayAvgTripsMapUpdate.get("Holiday").add((double) tripCount);
        } else if (datesToIgnore.contains(dateDay)) {
          _log.info(
              dateDay + " has " + tripCount + " trips, and we are ignoring this possible anomoly");
        } else {
          _log.info(dateDay + " has " + tripCount + " trips, this may indicate a problem.");
        }
      }
      dayCounter++;
      dateDay = removeTime(addDays(new Date(), dayCounter));
    }

    String out = "";

    for (String stringDay : days) {
      double tripsUpcoming;
      try {
        tripsUpcoming =
            dayAvgTripsMapUpdate.get(stringDay).stream()
                .mapToDouble(a -> a)
                .average()
                .getAsDouble();
      } catch (NoSuchElementException exception) {
        tripsUpcoming = dayAvgTripsMap.get(stringDay);
      }

      double currentAvg = dayAvgTripsMap.get(stringDay);
      int aprox_hours_per_month = 28 * 24;
      double suggestedAvg = currentAvg + ((tripsUpcoming - currentAvg) / (aprox_hours_per_month));
      out += stringDay + "," + suggestedAvg + "\n";
    }

    _log.info(out);
    try {
      Files.deleteIfExists(Path.of(dayAvgTripMapFile));
      Files.write(Path.of(dayAvgTripMapFile), out.getBytes());
    } catch (IOException io) {
      _log.error(io.getMessage());
    }
  }

  private Map<Date, Integer> getDateTripMap(GtfsMutableRelationalDao dao) {
    Map<Date, Integer> dateTripMap = new HashMap<Date, Integer>();
    for (Trip trip : dao.getAllTrips()) {
      _log.debug(trip.toString());
      // check for service
      boolean hasCalDateException = false;
      // are there calendar dates?
      for (ServiceCalendarDate calDate : dao.getCalendarDatesForServiceId(trip.getServiceId())) {
        // _log.info(calDate.toString());
        Date date = constructDate(calDate.getDate());
        if (dateTripMap.get(date) == null) {
          dateTripMap.put(date, 1);
        } else {
          dateTripMap.put(date, dateTripMap.get(date) + 1);
        }
      }
      // if there are no entries in calendarDates, check serviceCalendar
      if (!hasCalDateException) {
        ServiceCalendar servCal = dao.getCalendarForServiceId(trip.getServiceId());
        if (servCal != null) {
          // check for service using calendar
          Date start = removeTime(servCal.getStartDate().getAsDate());
          _log.info(start.toString());
          Date end = removeTime(servCal.getEndDate().getAsDate());
          _log.info(end.toString());
        }
      }
    }

    return dateTripMap;
  }

  private Date addDays(Date date, int daysToAdd) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.DATE, daysToAdd);
    return cal.getTime();
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

  public void setDatesToIgnoreUrl(String url) {
    this.datesToIgnoreUrl = url;
  }

  public void setDatesToIgnoreFile(String url) {
    this.datesToIgnoreFile = url;
  }

  public void setDayAvgTripMapUrl(String url) {
    this.dayAvgTripMapUrl = url;
  }

  public void setDayAvgTripMapFile(String url) {
    this.dayAvgTripMapFile = url;
  }

  public void setHolidaysUrl(String url) {
    this.holidaysUrl = url;
  }

  public void setHolidaysFile(String url) {
    this.holidaysFile = url;
  }

  public void setPercentageMatch(double percentageMatch) {
    this.percentageMatch = percentageMatch;
  }

  public void setSilentMode(boolean silentMode) {
    this.silentMode = silentMode;
  }

  private CSVListener readCsvFrom(CSVListener listener, String urlSource, String fileSource) {
    try {
      if (urlSource != null) {
        URL url = new URL(urlSource);
        try (InputStream is = url.openStream()) {
          new CSVLibrary().parse(is, listener);
        }
      }
      if (fileSource != null) {
        InputStream is = new BufferedInputStream(new FileInputStream(fileSource));
        new CSVLibrary().parse(is, listener);
      }
    } catch (Exception e) {
      _log.error(e.getMessage());
    }
    return listener;
  }

  private static class SetListener implements CSVListener {
    private Collection<Date> inputSet = new HashSet<>();
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void handleLine(List<String> list) throws Exception {
      inputSet.add(dateFormatter.parse(list.getFirst()));
    }

    Collection<Date> returnContents() {
      return inputSet;
    }
  }

  private static class MapListener implements CSVListener {
    private Map<String, Double> inputMap = new HashMap<>();

    @Override
    public void handleLine(List<String> list) {
      inputMap.put(list.getFirst(), Double.parseDouble(list.get(1)));
    }

    Map<String, Double> returnContents() {
      return inputMap;
    }
  }
}

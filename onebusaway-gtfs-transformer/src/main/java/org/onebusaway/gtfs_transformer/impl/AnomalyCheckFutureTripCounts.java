/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
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

import org.onebusaway.cloud.api.ExternalServices;
import org.onebusaway.cloud.api.ExternalServicesBridgeFactory;
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

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;


public class AnomalyCheckFutureTripCounts implements GtfsTransformStrategy {


    private final Logger _log = LoggerFactory.getLogger(AnomalyCheckFutureTripCounts.class);
    private final int aprox_hours_per_month = 28*24;

    @CsvField(optional = true)
    private String datesToIgnoreUrl; // a sample url might be "https://raw.githubusercontent.com/wiki/caylasavitzky/onebusaway-gtfs-modules/Testing-pulling-problem-routes.md";

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
    private boolean silentMode = false;


    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        Collection<Date> datesToIgnore = new HashSet<Date>();
        SetListener datesToIgnoreListener = new SetListener();
        try {
            if (datesToIgnoreUrl != null) {
                URL url = new URL(datesToIgnoreUrl);
                try (InputStream is = url.openStream()) {
                    new CSVLibrary().parse(is, datesToIgnoreListener);
                }
            }
            if (datesToIgnoreFile != null) {
                InputStream is = new BufferedInputStream(new FileInputStream(datesToIgnoreFile));
                new CSVLibrary().parse(is, datesToIgnoreListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        datesToIgnore = datesToIgnoreListener.returnContents();


        Collection<Date> holidays = new HashSet<Date>();
        SetListener holidaysListener = new SetListener();
        try {
            if (holidaysUrl != null) {
                URL url = new URL(holidaysUrl);
                try (InputStream is = url.openStream()) {
                    new CSVLibrary().parse(is, holidaysListener);
                }
            }
            if (holidaysFile != null) {
                InputStream is = new BufferedInputStream(new FileInputStream(holidaysFile));
                new CSVLibrary().parse(is, holidaysListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        holidays = holidaysListener.returnContents();


        Map<String, Double> dayAvgTripsMap = new HashMap<String, Double>();
        MapListener mapListener = new MapListener();
        try {
            if (dayAvgTripMapUrl != null) {
                URL url = new URL(dayAvgTripMapUrl);
                try (InputStream is = url.openStream()) {
                    new CSVLibrary().parse(is, mapListener);
                }
            }
            if (dayAvgTripMapFile != null) {
                InputStream is = new BufferedInputStream(new FileInputStream(dayAvgTripMapFile));
                new CSVLibrary().parse(is, mapListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dayAvgTripsMap = mapListener.returnContents();

        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE");
        for (Date date : holidays) {
            _log.info("holiday: " + date.toString() + " on a " + dayOfWeekFormat.format(date));
        }
        for (Date date : datesToIgnore) {
            _log.info("ignore: " + date.toString());
        }
        for (String key : dayAvgTripsMap.keySet()){
            System.out.println("key: " + key + " value: " + dayAvgTripsMap.get(key));
        }


        ExternalServices es =  new ExternalServicesBridgeFactory().getExternalServices();
        String agency = dao.getAllAgencies().iterator().next().getId();
        String agencyName = dao.getAllAgencies().iterator().next().getName();

        String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday","Holiday"};


        Map<Date, Integer> dateTripMap = getDateTripMap(dao);


        int dayCounter = 0;
        Map<String, ArrayList<Double>> dayAvgTripsMapUpdate = new HashMap<String, ArrayList<Double>>();


        for (String day:days){
            dayAvgTripsMapUpdate.put(day, new ArrayList<Double>());
        }

        while (true) {
            Date day = removeTime(addDays(new Date(), dayCounter));
            if (dateTripMap.get(day) == null){
                break;
            }
            int tripCount = dateTripMap.get(day);
            if ((tripCount < dayAvgTripsMap.get(dayOfWeekFormat.format(day))*(1+percentageMatch/100)) &&
                    (tripCount > dayAvgTripsMap.get(dayOfWeekFormat.format(day))*(1-percentageMatch/100))){
                _log.info(day + " has " + tripCount + " trips, and that's within reasonable expections");
                dayAvgTripsMapUpdate.get(dayOfWeekFormat.format(day)).add((double) tripCount);
            }
            else if (holidays.contains(day) & (tripCount < dayAvgTripsMap.get("Holiday")*(1+percentageMatch/100)
                    && (tripCount > dayAvgTripsMap.get("Holiday")*(1-percentageMatch/100)))) {
                _log.info(day + " has " + tripCount + " trips, is a holiday, and that's within reasonable expections");
                dayAvgTripsMapUpdate.get("Holiday").add((double) tripCount);
            }
            else if (datesToIgnore.contains(day)) {
                _log.info(day + " has " + tripCount + " trips, and we are ignoring this possible anomoly");
            }
            else {
                _log.info(day + " has " + tripCount + " trips, this may indicate a problem.");
                if (!silentMode) {
                    es.publishMessage(getTopic(), day.toString() + " has " + tripCount + " trips, this may indicate a problem.");
                }
            }
            dayCounter ++;
        }


        for (String day: days) {
            double tripsUpcoming = dayAvgTripsMapUpdate.get(day).stream().mapToDouble(a -> a).average().getAsDouble();
            double currentAvg = dayAvgTripsMap.get(day);
            double suggestedAvg = currentAvg+((tripsUpcoming-currentAvg)/(aprox_hours_per_month));
            _log.info(day+","+suggestedAvg);
        }





    }


    private Map<Date, Integer> getDateTripMap(GtfsMutableRelationalDao dao){
        Map<Date, Integer> dateTripMap = new HashMap<Date, Integer>();
        for (Trip trip : dao.getAllTrips()) {
            //_log.info(trip.toString());
            //check for service
            boolean hasCalDateException = false;
            //are there calendar dates?
            for (ServiceCalendarDate calDate : dao.getCalendarDatesForServiceId(trip.getServiceId())) {
                //_log.info(calDate.toString());
                Date date = constructDate(calDate.getDate());
                if (dateTripMap.get(date) == null) {
                    dateTripMap.put(date, 1);
                } else {
                    //Update this code so you are not using primitives and can just change object value rather than many puts depending on speed cost-- benchmark it!
                    dateTripMap.put(date, dateTripMap.get(date) + 1);
                }
            }
            //if there are no entries in calendarDates, check serviceCalendar
            if (!hasCalDateException) {
                ServiceCalendar servCal = dao.getCalendarForServiceId(trip.getServiceId());
                if (servCal != null) {
                    //check for service using calendar
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
        calendar.set(Calendar.MONTH, date.getMonth()-1);
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

    private String getTopic() {
        return System.getProperty("sns.topic");
    }

    public void setDatesToIgnoreUrl(String url){
        this.datesToIgnoreUrl = url;
    }

    public void setDatesToIgnoreFile(String url){
        this.datesToIgnoreFile = url;
    }

    public void setDayAvgTripMapUrl(String url){
        this.dayAvgTripMapUrl = url;
    }

    public void setDayAvgTripMapFile(String url){
        this.dayAvgTripMapFile = url;
    }

    public void setHolidaysUrl(String url){
        this.holidaysUrl = url;
    }

    public void setHolidaysFile(String url){
        this.holidaysFile = url;
    }

    private class SetListener implements CSVListener {
        private Collection<Date> inputSet = new HashSet<Date>();
        private GtfsMutableRelationalDao dao;
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

        @Override
        public void handleLine(List<String> list) throws Exception {
            inputSet.add(dateFormatter.parse(list.get(0)));
        }

        public Collection<Date> returnContents (){
            return inputSet;
        }
    }

    private class MapListener implements CSVListener {
        private Map<String, Double> inputMap = new HashMap<String, Double>();
        private GtfsMutableRelationalDao dao;

        @Override
        public void handleLine(List<String> list) throws Exception {

            inputMap.put( list.get(0),Double.parseDouble(list.get(1)));
        }

        public Map<String, Double> returnContents (){
            return inputMap;
        }
    }


}

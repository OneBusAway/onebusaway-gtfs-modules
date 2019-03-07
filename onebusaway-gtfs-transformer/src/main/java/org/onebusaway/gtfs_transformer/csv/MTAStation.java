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
package org.onebusaway.gtfs_transformer.csv;

import org.onebusaway.csv_entities.schema.annotations.CsvField;

public class MTAStation {
    @CsvField(name = "Station ID")
    private String stationId;

    @CsvField(name = "Complex ID")
    private String complexId;

    @CsvField(name = "GTFS Stop ID")
    private String gtfsStopId;

    @CsvField(name = "Division")
    private String division;

    @CsvField(name = "Line")
    private String line;

    @CsvField(name = "Stop Name")
    private String stopName;

    @CsvField(name = "Borough")
    private String borough;

    @CsvField(name = "Daytime Routes")
    private String dayTimeRoutes;

    @CsvField(name = "Structure")
    private String structure;

    @CsvField(name = "GTFS Latitude")
    private String lat;

    @CsvField(name = "GTFS Longitude")
    private String lon;

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getComplexId() {
        return complexId;
    }

    public void setComplexId(String complexId) {
        this.complexId = complexId;
    }

    public String getGtfsStopId() {
        return gtfsStopId;
    }

    public void setGtfsStopId(String gtfsStopId) {
        this.gtfsStopId = gtfsStopId;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getBorough() {
        return borough;
    }

    public void setBorough(String borough) {
        this.borough = borough;
    }

    public String getDayTimeRoutes() {
        return dayTimeRoutes;
    }

    public void setDayTimeRoutes(String dayTimeRoutes) {
        this.dayTimeRoutes = dayTimeRoutes;
    }

    public String getStructure() {
        return structure;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }
}

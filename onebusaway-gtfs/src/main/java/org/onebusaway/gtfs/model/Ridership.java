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
package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.mappings.ServiceDateFieldMappingFactory;

/**
 * ridership.txt from https://github.com/ODOT-PTS/GTFS-ride/blob/master/spec/en/reference.md#board_alighttxt
 */
@CsvFields(filename = "ridership.txt", required = false)
public final class Ridership extends IdentityBean<Integer> {

    private static final long serialVersionUID = 1L;
    private static final int MISSING_VALUE = -999;

    @CsvField(ignore = true)
    private int id; // this is internal only
    @CsvField(optional = true)
    private String agencyId;
    @CsvField(optional = true)
    private String routeId;
    @CsvField(optional = true)
    private String tripId;
    @CsvField(optional = true)
    private String stopId;

    private int totalBoardings;
    private int totalAlightings;

    @CsvField(name = "ridership_start_date", optional = true, mapping = ServiceDateFieldMappingFactory.class)
    private ServiceDate startDate;
    @CsvField(name = "ridership_end_date", optional = true, mapping = ServiceDateFieldMappingFactory.class)
    private ServiceDate endDate;
    @CsvField(optional = true)
    private double averageLoad; // this isn't part of the spec, but a proposed addition


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String id) {
        this.agencyId = id;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public int getTotalBoardings() {
        return totalBoardings;
    }

    public void setTotalBoardings(int totalBoardings) {
        this.totalBoardings = totalBoardings;
    }

    public void clearTotalBoardings() {
        this.totalBoardings = MISSING_VALUE;
    }

    public int getTotalAlightings() {
        return totalAlightings;
    }

    public void setTotalAlightings(int totalAlightings) {
        this.totalAlightings = totalAlightings;
    }

    public void clearTotalAlightings() {
        this.totalAlightings = MISSING_VALUE;
    }

    public ServiceDate getStartDate() {
        return startDate;
    }

    public void setStartDate(ServiceDate startDate) {
        this.startDate = startDate;
    }

    public ServiceDate getEndDate() {
        return endDate;
    }

    public void setEndDate(ServiceDate endDate) {
        this.endDate = endDate;
    }

    public double getAverageLoad() {
        return averageLoad;
    }

    public void setAverageLoad(double averageLoad) {
        this.averageLoad = averageLoad;
    }

    public String toString() { return "<Ridership [" + getId() + "] "
            + getRouteId() + ":"
            + getTripId() + ":"
            + getStopId() + " "
            +">" ;
    }

}

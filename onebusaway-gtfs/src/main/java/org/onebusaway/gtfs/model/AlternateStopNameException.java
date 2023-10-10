/**
 * Copyright (C) 2022 Cambridge Systematics <csavitzky@camsys.com>
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

@CsvFields(filename = "alternate_stop_names_exceptions.txt", required = false)
public class AlternateStopNameException extends IdentityBean<Integer> {

    @CsvField(ignore = true)
    private int id;

    @CsvField(optional = true)
    int routeId;

    @CsvField(optional = true)
    int directionId;

    @CsvField(optional = true)
    int stopId;

    @CsvField(optional = true)
    String alternateStopName;

    public AlternateStopNameException() {
    }

    public AlternateStopNameException(AlternateStopNameException asne) {
        this.routeId = asne.routeId;
        this.directionId = asne.directionId;
        this.stopId = asne.stopId;
        this.alternateStopName = asne.alternateStopName;
    }


    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public int getDirectionId() {
        return directionId;
    }

    public void setDirectionId(int directionId) {
        this.directionId = directionId;
    }

    public int getStopId() {
        return stopId;
    }

    public void setStopId(int stopId) {
        this.stopId = stopId;
    }

    public String getAlternateStopName() {
        return alternateStopName;
    }

    public void setAlternateStopName(String alternateStopName) {
        this.alternateStopName = alternateStopName;
    }
}

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
import org.onebusaway.csv_entities.schema.annotations.CsvFields;

@CsvFields(filename = "elevators.txt")
public class MTAElevator {
    // Equip ID	Equip Loc	Stop Desc	Stop ID	Direction	Track	Line

    @CsvField(name = "Equip ID")
    private String id;

    @CsvField(name = "Equip Loc")
    private String loc;

    @CsvField(name = "Stop Desc")
    private String desc;

    @CsvField(name = "Stop ID")
    private String stopId;

    @CsvField(name = "Direction")
    private String direction;

    @CsvField(name = "Track", optional = true)
    private String track;

    @CsvField(name = "Line", optional = true)
    private String line;

    @CsvField(name = "mezzanine_name_1", optional = true)
    private String mezzanineName1;

    @CsvField(name = "mezzanine_name_2", optional = true)
    private String mezzanineName2;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getMezzanineName1() {
        return mezzanineName1;
    }

    public void setMezzanineName1(String mezzanineName1) {
        this.mezzanineName1 = mezzanineName1;
    }

    public String getMezzanineName2() {
        return mezzanineName2;
    }

    public void setMezzanineName2(String mezzanineName2) {
        this.mezzanineName2 = mezzanineName2;
    }
}

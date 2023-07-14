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
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.StopLocationFieldMappingFactory;

import java.io.Serializable;

@CsvFields(filename = "facilities.txt", required = false)
public class Facility extends IdentityBean<AgencyAndId>{

    private static final long serialVersionUID = 2L;


    @CsvField(name = "facility_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
    private AgencyAndId id;

    @CsvField(optional = true)
    private String facilityCode;

    @CsvField(optional = true)
    private String facilityClass;

    @CsvField(optional = true)
    private String facilityType;

    @CsvField(name = "stop_id", mapping = EntityFieldMappingFactory.class, optional = true)
    private Stop stop;

    @CsvField(optional = true)
    private String facilityShortName;

    @CsvField(optional = true)
    private String facilityLongName;

    @CsvField(optional = true)
    private int wheelchairFacility;

    public Facility(){

    }

    public Facility(Facility fa){
        this.facilityCode = fa.facilityCode;
        this.facilityClass = fa.facilityClass;
        this.facilityType = fa.facilityType;
        this.stop = fa.stop;
        this.facilityShortName = fa.facilityShortName;
        this.facilityLongName = fa.facilityLongName;
        this.wheelchairFacility = fa.wheelchairFacility;

    }

    @Override
    public void setId(AgencyAndId id) {
        this.id = id;
    }
    @Override
    public AgencyAndId getId() {
        return id;
    }

    public String getFacilityCode() {
        return facilityCode;
    }

    public String getFacilityClass() {
        return facilityClass;
    }

    public String getFacilityLongName() {
        return facilityLongName;
    }

    public String getFacilityShortName() {
        return facilityShortName;
    }

    public String getFacilityType() {
        return facilityType;
    }

    public StopLocation getStop() {
        return stop;
    }

    public void setStop(Stop stop) {
        this.stop = stop;
    }

    public int getWheelchairFacility() {
        return wheelchairFacility;
    }

    public void setFacilityClass(String facilityClass) {
        this.facilityClass = facilityClass;
    }

    public void setFacilityCode(String facilityCode) {
        this.facilityCode = facilityCode;
    }

    public void setFacilityLongName(String facilityLongName) {
        this.facilityLongName = facilityLongName;
    }

    public void setFacilityShortName(String facilityShortName) {
        this.facilityShortName = facilityShortName;
    }

    public void setFacilityType(String facilityType) {
        this.facilityType = facilityType;
    }

    public void setWheelchairFacility(int wheelchairFacility) {
        this.wheelchairFacility = wheelchairFacility;
    }

}

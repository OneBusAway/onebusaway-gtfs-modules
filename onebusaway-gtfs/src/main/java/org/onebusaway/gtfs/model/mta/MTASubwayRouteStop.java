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
package org.onebusaway.gtfs.model.mta;

import org.onebusaway.csv_entities.schema.EnumFieldMappingFactory;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.serialization.mappings.AgencyIdFieldMappingFactory;

@CsvFields(filename = "subwayRouteStops.csv")
public class MTASubwayRouteStop {

	public enum ADAStatus {
		NOT_ACCESSIBLE,
		FULLY_ACCESSIBLE,
		PARTLY_ACCESSIBLE		
	}
	
	@CsvField(name = "route_id", mapping=AgencyIdFieldMappingFactory.class)
    private AgencyAndId routeAgencyAndId;

    @CsvField(name = "stop_sequence")
    private int stopSequence;
    
    @CsvField(name = "stop_id", mapping=AgencyIdFieldMappingFactory.class)
    private AgencyAndId stopAgencyAndId;
    
    @CsvField(name = "stop_type")
    private int stopType;
    
    @CsvField(name = "ada", mapping=EnumFieldMappingFactory.class, optional = true)
    private ADAStatus ada;
       
    @CsvField(name = "stop_status")
    private int stopStatus;
    
    @CsvField(name = "stop_name")
    public String stopName;
    
    @CsvField(name = "borough")
    public String borough;

    @CsvField(name = "Notes", optional = true)
    private String notes;
    
    public AgencyAndId getRouteAgencyAndId() {
		return routeAgencyAndId;
	}

	public void setRouteAgencyAndId(AgencyAndId routeAgencyAndId) {
		this.routeAgencyAndId = routeAgencyAndId;
	}

	public int getStopSequence() {
		return stopSequence;
	}

	public void setStopSequence(int stopSequence) {
		this.stopSequence = stopSequence;
	}

	public AgencyAndId getStopAgencyAndId() {
		return stopAgencyAndId;
	}

	public void setStopAgencyAndId(AgencyAndId stopAgencyAndId) {
		this.stopAgencyAndId = stopAgencyAndId;
	}

	public int getStopType() {
		return stopType;
	}

	public void setStopType(int stopType) {
		this.stopType = stopType;
	}

	public ADAStatus getAda() {
		return ada;
	}

	public void setAda(ADAStatus ada) {
		this.ada = ada;
	}

	public int getStopStatus() {
		return stopStatus;
	}

	public void setStopStatus(int stopStatus) {
		this.stopStatus = stopStatus;
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

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}


}

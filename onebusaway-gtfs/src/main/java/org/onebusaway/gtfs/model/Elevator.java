/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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

@CsvFields(filename = "elevators.txt", required = false)
public final class Elevator extends IdentityBean<Integer> {

    private static final long serialVersionUID = 1L;

    @CsvField(ignore = true)
    private int id;

    @CsvField(name = "elevator_id", mapping = DefaultAgencyIdFieldMappingFactory.class)
    private AgencyAndId elevatorId;

    @CsvField(name = "pathway_id", mapping = EntityFieldMappingFactory.class, order = -1)
    private Pathway pathway;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public AgencyAndId getElevatorId() {
        return elevatorId;
    }

    public void setElevatorId(AgencyAndId elevatorId) {
        this.elevatorId = elevatorId;
    }

    public Pathway getPathway() {
        return pathway;
    }

    public void setPathway(Pathway pathway) {
        this.pathway = pathway;
    }

    @Override
    public String toString() {
        return "<Elevator " + this.id + ">";
    }
}

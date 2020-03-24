/**
 * Copyright (C) 2020 Hannes Junnila <hannes@kyyti.com>
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
import org.onebusaway.gtfs.serialization.mappings.StoplikeFieldMappingFactory;

@CsvFields(filename = "location_groups.txt", required = false, prefix = "location_group_")
public class LocationGroupElement extends IdentityBean<AgencyAndId> implements Stoplike {

    private static final long serialVersionUID = 1L;

    @CsvField(mapping = DefaultAgencyIdFieldMappingFactory.class)
    private AgencyAndId id;

    @CsvField(name = "location_id", mapping = StoplikeFieldMappingFactory.class)
    private Stoplike locationId;

    @CsvField(optional = true)
    private String name;

    @Override
    public AgencyAndId getId() {
        return id;
    }

    public void setId(AgencyAndId id) {
        this.id = id;
    }

    public Stoplike getLocationId() {
        return locationId;
    }

    public void setLocationId(Stoplike locationId) {
        this.locationId = locationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

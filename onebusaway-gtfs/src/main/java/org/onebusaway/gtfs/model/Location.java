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

import org.geojson.GeoJsonObject;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;

@CsvFields(filename = "locations.geojson", required = false)
public class Location extends IdentityBean<AgencyAndId> implements Stoplike {
    private AgencyAndId id;

    private String name;

    private GeoJsonObject geometry;

    @Override
    public AgencyAndId getId() {
        return id;
    }

    public void setId(AgencyAndId id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoJsonObject getGeometry() {
        return geometry;
    }

    public void setGeometry(GeoJsonObject geometry) {
        this.geometry = geometry;
    }
}

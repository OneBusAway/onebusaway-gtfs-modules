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

import org.onebusaway.csv_entities.schema.EntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.csv_entities.schema.FieldMappingFactory;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.Pathway;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.util.PathwayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.onebusaway.gtfs.model.Pathway.MODE_STAIRS;

public class StationComplexStrategy implements GtfsTransformStrategy {

    private enum Type {
        PATHWAYS,
        PARENT_STATION
    }

    private final Logger _log = LoggerFactory.getLogger(StationComplexStrategy.class);

    private static final String STOP_SEPARATOR = " ";

    // File format: lines are a list of stops which comprise a station complex
    private String complexFile;

    @CsvField(optional = true)
    private int genericPathwayTraversalTime = 60;

    @CsvField(ignore = true)
    private Type typeInternal = Type.PATHWAYS;

    @CsvField(optional = true)
    private String type = null;

    public String getName() {
        return this.getClass().getName();
    }

    // Create pathways between all stops in a station complex
    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        File stComplexFile = new File(complexFile);
        if (!stComplexFile.exists()) {
            throw new IllegalStateException(
                    "Station Complex file does not exist: " + stComplexFile.getName());
        }
        Collection<List<Stop>> complexes = getComplexList(dao);
        if (typeInternal.equals(Type.PATHWAYS)) {
            makePathways(complexes, dao);
        } else if (typeInternal.equals(Type.PARENT_STATION)) {
            setParents(complexes, dao);
        }
    }

    private void setParents(Collection<List<Stop>> complexes, GtfsMutableRelationalDao dao) {
        for (List<Stop> complex : complexes) {

            Map<String, List<Stop>> grouped = complex.stream()
                    .collect(Collectors.groupingBy(Stop::getName));
            for (List<Stop> group : grouped.values()) {
                String parent = group.get(0).getParentStation();
                for (Stop stop : group) {
                    stop.setParentStation(parent);
                    dao.updateEntity(stop);
                }
            }
        }
    }

    private void makePathways(Collection<List<Stop>> complexes, GtfsMutableRelationalDao dao) {
        String feedId = dao.getAllStops().iterator().next().getId().getAgencyId();
        List<Pathway> newPathways = new ArrayList<>();
        PathwayUtil util = new PathwayUtil(feedId, newPathways);
        for (List<Stop> complex : complexes) {
            for (Stop s : complex) {
                for (Stop t : complex) {
                    if (s != null && s.getParentStation() != null && t != null) {
                        if (!s.equals(t)) {
                            String id = String.format("complex-%s-%s", s.getId().getId(), t.getId().getId());
                            util.createPathway(s, t, MODE_STAIRS, genericPathwayTraversalTime, id, null, false);
                        }
                    } else {
                        _log.error("Illegal Stop {}", s);
                    }
                }
            }
        }
        for (Pathway p : newPathways) {
            dao.saveEntity(p);
        }
    }

    private Collection<List<Stop>> getComplexList(GtfsDao dao) {
        Map<String, Stop> stops = getStopMap(dao);
        Collection<List<Stop>> complexes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(complexFile)))) {
            String line;
            while ((line = br.readLine()) != null) {
                List<Stop> complex = new ArrayList<>();
                for (String id : line.split(STOP_SEPARATOR)) {
                    Stop stop = stops.get(id);
                    if (stop == null)
                        _log.info("null stop: {}", id);
                    complex.add(stop);
                }
                complexes.add(complex);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return complexes;
    }

    private Map<String, Stop> getStopMap(GtfsDao dao) {
        Map<String, Stop> map = new HashMap<>();
        for (Stop stop : dao.getAllStops()) {
            if (stop.getLocationType() == 0) {
                map.put(stop.getId().getId(), stop);
            }
        }
        return map;
    }

    public void setComplexFile(String complexFile) {
        this.complexFile = complexFile;
    }

    public void setGenericPathwayTraversalTime(int genericPathwayTraversalTime) {
        this.genericPathwayTraversalTime = genericPathwayTraversalTime;
    }

    public void setType(String type) {
        this.type = type;
        this.typeInternal = Type.valueOf(type);
    }
}
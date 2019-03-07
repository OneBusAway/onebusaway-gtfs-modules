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

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Pathway;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.csv.CSVUtil;
import org.onebusaway.gtfs_transformer.csv.MTAStation;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.util.PathwayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.onebusaway.gtfs_transformer.util.PathwayUtil.PATHWAY_MODE_GENERIC;

public class StationComplexStrategy implements GtfsTransformStrategy {

    private enum Type {
        PATHWAYS,
        PARENT_STATION
    }

    private final Logger _log = LoggerFactory.getLogger(StationComplexStrategy.class);

    private static final String STOP_SEPARATOR = " ";

    // File format: Stations csv
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
        List<MTAStation> stations = CSVUtil.readCsv(MTAStation.class, complexFile);
        Map<String, Stop> stopMap = getStopMap(dao);
        String feedId = dao.getAllFeedInfos().iterator().next().getId();
        if (typeInternal.equals(Type.PATHWAYS)) {
            Collection<Complex> complexes = toComplexes(stations, stopMap);
            makePathways(complexes, dao);
        } else if (typeInternal.equals(Type.PARENT_STATION)) {
            makeParents(dao, feedId, stations);
        }
    }

    private void makeParents(GtfsMutableRelationalDao dao, String feedId, Collection<MTAStation> stations) {
        Map<String, Stop> stopMap = new HashMap<>();
        for (Stop stop : dao.getAllStops()) {
            if (stop.getLocationType() == 1) {
                stopMap.put(stop.getId().getId(), stop);
            }
        }
        for (MTAStation station : stations) {
            Stop stop = stopMap.get(station.getGtfsStopId());
            if (stop == null) {
                _log.info("No stop in GTFS: {}", station.getGtfsStopId());
                continue;
            }
            if (stop.getLocationType() != 1) { // gtfs station
                throw new RuntimeException("Bad data! Unexpected, not station: " + station.getGtfsStopId());
            }
            // MTA station, "above" parent station in the hierarchy
            // 3 is MTA Station, 4 is complex
            Stop stationStop = getOrCreateStation(dao, feedId, station, 3);
            stop.setParentStation(stationStop.getId().getId());
            dao.updateEntity(stationStop);
        }
    }

    private Stop getOrCreateStation(GtfsMutableRelationalDao dao, String feedId, MTAStation station, int locationType) {
        String code, entityId;
        if (locationType == 3) {
            code = station.getStationId();
            entityId = code + "-station";
        } else if (locationType == 4) {
            code = station.getComplexId();
            entityId = code + "-complex";
        } else {
            throw new RuntimeException("Unexpected locationType: " + locationType);
        }
        AgencyAndId stationId = new AgencyAndId(feedId, entityId);
        Stop stop = dao.getStopForId(stationId);
        if (stop != null) {
            return stop;
        }
        stop = new Stop();
        stop.setId(stationId);
        stop.setCode(code);
        stop.setName(station.getStopName());
        stop.setLat(Double.parseDouble(station.getLat()));
        stop.setLon(Double.parseDouble(station.getLon()));
        stop.setLocationType(locationType);
        if (locationType == 3 && !station.getComplexId().equals(station.getStationId())) {
            Stop parentComplex = getOrCreateStation(dao, feedId, station, 4);
            stop.setParentStation(parentComplex.getId().getId());
        }
        dao.saveEntity(stop);
        return stop;
    }

    private void makePathways(Collection<Complex> complexes, GtfsMutableRelationalDao dao) {
        String feedId = dao.getAllStops().iterator().next().getId().getAgencyId();
        List<Pathway> newPathways = new ArrayList<>();
        PathwayUtil util = new PathwayUtil(feedId, newPathways);
        for (Complex complex : complexes) {
            for (Stop s : complex.stops) {
                for (Stop t : complex.stops) {
                    if (s != null && s.getParentStation() != null && t != null) {
                        if (!s.equals(t)) {
                            String id = String.format("complex-%s-%s", s.getId().getId(), t.getId().getId());
                            util.createPathway(s, t, PATHWAY_MODE_GENERIC, genericPathwayTraversalTime, -1, id, null, false);
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

    private Map<String, Stop> getStopMap(GtfsDao dao) {
        Map<String, Stop> map = new HashMap<>();
        for (Stop stop : dao.getAllStops()) {
            if (stop.getLocationType() == 0) {
                map.put(stop.getId().getId(), stop);
            }
        }
        return map;
    }

    private Collection<Complex> toComplexes(List<MTAStation> stations, Map<String, Stop> stopMap) {
        Map<String, Complex> complexes = new HashMap<>();
        for (MTAStation station : stations) {
            Complex complex = complexes.computeIfAbsent(station.getComplexId(), Complex::new);
            Stop stop = stopMap.get(station.getGtfsStopId());
            complex.stops.add(stop);
        }
        return complexes.values();
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

    private class Complex {
        Complex(String name) {
            this.name = name;
            stops = new ArrayList<>();
        }
        String name;
        List<Stop> stops;
    }
}
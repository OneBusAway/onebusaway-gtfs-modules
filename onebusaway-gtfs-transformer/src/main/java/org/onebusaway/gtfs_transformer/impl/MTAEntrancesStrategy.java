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
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.csv.MTAElevator;
import org.onebusaway.gtfs_transformer.csv.MTAEntrance;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.util.PathwayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.onebusaway.gtfs_transformer.csv.CSVUtil.readCsv;

import static org.onebusaway.gtfs_transformer.util.PathwayUtil.*;

public class MTAEntrancesStrategy implements GtfsTransformStrategy {

    /**
     * Create entrances/pathways for MTA GTFS.
     * Not-accessible street entrances are created for all stops.
     * Accessible street entrances (elevators) are determined from elevator_to_node.csv, if possible.
     * Potentially could be integrated with Station Planning entrances file in the future.
     *
     * See: https://docs.google.com/document/d/1qJOTe4m_a4dcJnvXYt4smYj4QQ1ejZ8CvLBYzDM5IyM/edit?ts=5a39452a
     *
     */

    private static final int LOCATION_TYPE_STOP = 0;
    private static final int LOCATION_TYPE_STATION = 1;
    private static final int LOCATION_TYPE_ENTRANCE = 2;
    private static final int LOCATION_TYPE_PAYGATE = 3;
    private static final int LOCATION_TYPE_GENERIC = 4;


    private static final int WHEELCHAIR_ACCESSIBLE = 1;
    private static final int NOT_WHEELCHAIR_ACCESSIBLE = 2;

    private static final String DEFAULT_MEZZ = "default";

    private static final Logger _log = LoggerFactory.getLogger(MTAEntrancesStrategy.class);

    private static final List<String> accessibleEntranceTypes = Arrays.asList(
            "Ramp", "Walkway", "Road_Walkway", "Elevator", "Door", "Entrance", "Tunnel");

    @CsvField(ignore = true)
    private String agencyId;

    @CsvField(ignore = true)
    private Collection<Stop> newStops;

    @CsvField(ignore = true)
    private Collection<Pathway> newPathways;

    @CsvField(ignore = true)
    private PathwayUtil pathwayUtil;

    @CsvField(optional = true)
    private String elevatorsCsv;

    private String entrancesCsv;

    // control a few things so this can be reused for the railroads:
    private boolean stopsHaveParents;

    private boolean createMissingLinks;

    private boolean contextualAccessibility;

    @CsvField(optional = true)
    private int genericPathwayTraversalTime = 60;

    @CsvField(optional = true)
    private int stairTraversalTime = 60;

    @CsvField(optional = true)
    private int escalatorTraversalTime = 60;

    @CsvField(optional = true)
    private int walkwayTraversalTime = 60;

    @CsvField(optional = true)
    private int elevatorTraversalTime = 120;

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        agencyId = dao.getAllAgencies().iterator().next().getId();

        newStops = new HashSet<>();
        newPathways = new HashSet<>();

        pathwayUtil = new PathwayUtil(agencyId, newPathways);

        Map<String, StopGroup> stopGroups = new HashMap<>();

        // For every stop that's not a station, add an entrance which is not wheelchair accessible, and a pathway.
        for (Stop stop : dao.getAllStops()) {
            if (stopsHaveParents) {
                // Put stop into a stop-group with parent, uptown, downtown
                String gid = stop.getLocationType() == LOCATION_TYPE_STOP ? stop.getParentStation() : stop.getId().getId();
                StopGroup group = stopGroups.get(gid);
                if (group == null) {
                    group = new StopGroup();
                    stopGroups.put(gid, group);
                }
                if (stop.getLocationType() == LOCATION_TYPE_STATION) {
                    group.parent = stop;
                } else if (stop.getId().getId().endsWith("S")) {
                    group.downtown = stop;
                } else if (stop.getId().getId().endsWith("N")) {
                    group.uptown = stop;
                } else throw new RuntimeException("unexpected!");
            } else {
                StopGroup group = new StopGroup();
                group.parent = stop;
                String gid = stop.getId().getId();
                stopGroups.put(gid, group);
            }
        }

        readEntranceData(stopGroups);

        if (elevatorsCsv != null) {
            readElevatorData(stopGroups);
        }

        for (Stop s : newStops) {
            dao.saveEntity(s);
        }

        for (Pathway pathway : newPathways) {
            dao.saveEntity(pathway);
        }
    }

    /*
    Read entrances, for each, create only non-accessible (and non-easement) entrances and pathways.
    In the future, entrance dataset should include elevator IDs, so it can be matched to elevator dataset.
     */
    private void readEntranceData(Map<String, StopGroup> stopGroups) {
        // FYI, blacklist: Easement, Elevator.
        // LIRR: Overpass_Walkway, Overpass_Walkway_Stair, Road_Walkway_Stair, Overpass_Walkway_Elevator
        // MNR: Tunnel
        List<String> whitelist = new ArrayList<>(Arrays.asList("Door", "Escalator", "Ramp", "Stair", "Walkway", // subway
                "Stair_Escalator", "Road_Walkway", "Entrance", // LIRR
                "Tunnel" // MNR
                ));

        if (contextualAccessibility)
            whitelist.add("Elevator");

        List<MTAEntrance> entrances = getEntrances();

        for (MTAEntrance e : entrances) {
            if (whitelist.contains(e.getEntranceType()) && e.hasLocation()) {
                StopGroup g = stopGroups.get(e.getStopId());
                if (g == null)
                    _log.error("No stop group for station {}", e.getStopId());
                else
                    g.entrances.add(e);
            }
        }

        for (StopGroup group : stopGroups.values()) {
            if (group.entrances.isEmpty() && createMissingLinks) {
                _log.error("Station {} has no entrances", group.parent.getId());
                // mock, like we were doing before
                Stop entrance = createNonAccessibleStreetEntrance(group.parent);
                pathwayUtil.createPathway(entrance, group.uptown, PATHWAY_MODE_GENERIC, genericPathwayTraversalTime, -1, "GENERIC", null);
                pathwayUtil.createPathway(entrance, group.downtown, PATHWAY_MODE_GENERIC, genericPathwayTraversalTime, -1, "GENERIC", null);
                continue;
            }

            int i = 0;
            for (MTAEntrance entrance : group.entrances) {
                Stop entranceStop = createStopFromMTAEntrance(group.parent, entrance, i);
                int pathwayMode, traversalTime, wheelchairTraversalTime;
                switch(entrance.getEntranceType()) {
                    case "Stair_Escalator": // treat as stair for now
                    case "Stair":
                        pathwayMode = PATHWAY_MODE_STAIR;
                        traversalTime = stairTraversalTime;
                        wheelchairTraversalTime = -1;
                        break;
                    case "Ramp":
                    case "Walkway":
                    case "Road_Walkway":
                        pathwayMode = PATHWAY_MODE_WALKWAY;
                        traversalTime = walkwayTraversalTime;
                        wheelchairTraversalTime = traversalTime * 2;
                        break;
                    case "Escalator":
                        pathwayMode = PATHWAY_MODE_ESCALATOR;
                        traversalTime = escalatorTraversalTime;
                        wheelchairTraversalTime = -1;
                        break;
                    case "Elevator":
                        pathwayMode = PATHWAY_MODE_ELEVATOR;
                        traversalTime = elevatorTraversalTime;
                        wheelchairTraversalTime = elevatorTraversalTime;
                        break;
                    case "Door":
                    case "Entrance":
                    default:
                        pathwayMode = PATHWAY_MODE_GENERIC;
                        traversalTime = genericPathwayTraversalTime;
                        wheelchairTraversalTime = traversalTime * 2;
                }
                String id = entrance.getEntranceType() + "-" + i;
                wheelchairTraversalTime = contextualAccessibility ? wheelchairTraversalTime : -1;
                if (stopsHaveParents) {
                    if (!entrance.hasDirection() || entrance.getDirection().equals("N")) {
                        pathwayUtil.createPathway(entranceStop, group.uptown, pathwayMode, traversalTime, wheelchairTraversalTime, id, null);
                    }
                    if (!entrance.hasDirection() || entrance.getDirection().equals("S")) {
                        pathwayUtil.createPathway(entranceStop, group.downtown, pathwayMode, traversalTime, wheelchairTraversalTime, id, null);
                    }
                } else {
                    pathwayUtil.createPathway(entranceStop, group.parent, pathwayMode, traversalTime, wheelchairTraversalTime, id, null);
                }
                i++;
            }
        }
    }

    private void readElevatorData(Map<String, StopGroup> stopGroups) {
        List<MTAElevator> elevators = getElevators();
        for (MTAElevator e : elevators) {
            StopGroup g = stopGroups.get(e.getStopId());
            if (g == null)
                _log.error("No stop group for elevator={}, stop={}", e.getId(), e.getStopId());
            else
                g.elevators.add(e);
        }

        int unknown = 0;

        for (StopGroup group : stopGroups.values()) {
            Stop entrance = null;

            // elevator is defined by ID, type, and direction iff it includes platform
            Set<String> seenElevatorPathways = new HashSet<>();

            Map<String, Stop> mezzByName = new HashMap<>();

            for (MTAElevator e : group.elevators) {
                ElevatorPathwayType type = ElevatorPathwayType.valueOf(e.getLoc());
                type.resolveElevatorNames(e);
                if (type == ElevatorPathwayType.UNKNOWN) {
                    unknown++;
                    _log.debug("unknown type={}, elev={}", e.getLoc(), e.getId());
                    continue;
                }
                if (entrance == null && type.shouldCreateStreetEntrance()) {
                    entrance = createAccessibleStreetEntrance(group.parent);
                }

                if (type.shouldCreateMezzanine()) {
                    for (String name : type.mezzanineNames) {
                        Stop m = mezzByName.get(name);
                        if (m == null) {
                            m = createMezzanine(group.parent, name);
                            mezzByName.put(name, m);
                        }
                    }
                }

                Stop platform = null;
                if (e.getDirection() != null) {
                    if (e.getDirection().equals("N")) {
                        platform = group.uptown;
                    } else if (e.getDirection().equals("S")) {
                        platform = group.downtown;
                    } else {
                        _log.error("Unexpected direction={}, elev={}", e.getDirection(), e.getId());
                    }
                }

                String code = e.getId();

                if (type.shouldCreateStreetToMezzanine()) {
                    String id_base = "S2M_" + code;
                    for (String name : type.mezzanineNames) {
                        Stop mezz = mezzByName.get(name);
                        String id = id_base + "_" + name;
                        createElevPathways(entrance, mezz, code, id, seenElevatorPathways);
                    }
                }

                if (type.shouldCreateMezzanineToPlatform()) {
                    String id_base = "M2P_" + code + "_" + e.getDirection();
                    for (String name : type.mezzanineNames) {
                        Stop mezz = mezzByName.get(name);
                        String id = id_base + "_" + name;
                        createElevPathways(mezz, platform, code, id, seenElevatorPathways);
                    }
                }

                if (type.shouldCreateStreetToPlatform()) {
                    String id = "S2P_" + code + "_" + e.getDirection();
                    createElevPathways(entrance, platform, code, id, seenElevatorPathways);
                }

                if (type.shouldCreateMezzanineToMezzanine()) {
                    for (int i = 0; i < type.mezzanineNames.size(); i++) {
                        for (int j = i + 1; j < type.mezzanineNames.size(); j++) {
                            String name0 = type.mezzanineNames.get(i);
                            String name1 = type.mezzanineNames.get(j);
                            Stop mezz0 = mezzByName.get(name0);
                            Stop mezz1 = mezzByName.get(name1);
                            String id = "M2M_" + code + "_" + name0 + "_" + name1;
                            createElevPathways(mezz0, mezz1, code, id, seenElevatorPathways);
                        }
                    }
                }
            }
        }

        _log.info("Processed {} / {} ({} are unknown)", elevators.size() - unknown, elevators.size(), unknown);
    }

    /**
     * Encapsulate an elevator type, determine what should be created, and what the synonyms are.
     */
    private enum ElevatorPathwayType {
        STREET_TO_MEZZ_TO_PLATFORM,
        STREET_TO_MEZZ,
        MEZZ_TO_PLATFORM,
        STREET_TO_PLATFORM,
        MEZZ_TO_MEZZ,
        MEZZ_TO_MEZZ_TO_STREET,
        MEZZ_TO_MEZZ_TO_PLATFORM,
        MEZZ_TO_MEZZ_TO_STREET_TO_PLATFORM,
        UNKNOWN;

        List<String> mezzanineNames;

        boolean shouldCreateStreetEntrance() {
            return this == STREET_TO_MEZZ || this == STREET_TO_MEZZ_TO_PLATFORM || this == STREET_TO_PLATFORM || this == MEZZ_TO_MEZZ_TO_STREET_TO_PLATFORM;
        }

        boolean shouldCreateMezzanine() {
            return this == STREET_TO_MEZZ_TO_PLATFORM | this == STREET_TO_MEZZ || this == MEZZ_TO_PLATFORM
                    || this == MEZZ_TO_MEZZ || this == MEZZ_TO_MEZZ_TO_STREET || this == MEZZ_TO_MEZZ_TO_PLATFORM
                    || this == MEZZ_TO_MEZZ_TO_STREET_TO_PLATFORM;
        }

        boolean shouldCreateStreetToMezzanine() {
            return this == STREET_TO_MEZZ_TO_PLATFORM || this == STREET_TO_MEZZ || this == MEZZ_TO_MEZZ_TO_STREET
                    || this == MEZZ_TO_MEZZ_TO_STREET_TO_PLATFORM;
        }

        boolean shouldCreateMezzanineToPlatform() {
            return this == STREET_TO_MEZZ_TO_PLATFORM || this == MEZZ_TO_PLATFORM || this == MEZZ_TO_MEZZ_TO_PLATFORM
                    || this == MEZZ_TO_MEZZ_TO_STREET_TO_PLATFORM;
        }

        boolean shouldCreateStreetToPlatform() {
            return this == STREET_TO_MEZZ_TO_PLATFORM || this == STREET_TO_PLATFORM
                    || this == MEZZ_TO_MEZZ_TO_STREET_TO_PLATFORM;
        }

        boolean shouldCreateMezzanineToMezzanine() {
            return this == MEZZ_TO_MEZZ || this == MEZZ_TO_MEZZ_TO_STREET || this == MEZZ_TO_MEZZ_TO_PLATFORM
                    || this == MEZZ_TO_MEZZ_TO_STREET_TO_PLATFORM;
        }

        void resolveElevatorNames(MTAElevator e) {
            String mezz1 = e.getMezzanineName1();
            String mezz2 = e.getMezzanineName2();
            switch (this) {
                case STREET_TO_MEZZ_TO_PLATFORM:
                case STREET_TO_MEZZ:
                case MEZZ_TO_PLATFORM:
                    if (mezz1 != null)
                        mezzanineNames = Collections.singletonList(mezz1);
                    else
                        mezzanineNames = Collections.singletonList(DEFAULT_MEZZ);
                    break;
                case MEZZ_TO_MEZZ:
                case MEZZ_TO_MEZZ_TO_STREET:
                case MEZZ_TO_MEZZ_TO_PLATFORM:
                case MEZZ_TO_MEZZ_TO_STREET_TO_PLATFORM:
                    if (mezz1 == null | mezz2 == null)
                        throw new IllegalArgumentException("elevators with >1 mezzanine require names: " + e.getId());
                    mezzanineNames = Arrays.asList(mezz1, mezz2);
                default:
                    // pass
            }
        }
    }


    /**
     * StopGroup: collect uptown, downtown, and parent stop together.
     */
    private class StopGroup {
        Stop uptown;
        Stop downtown;
        Stop parent;

        List<MTAElevator> elevators = new ArrayList<>();

        List<MTAEntrance> entrances = new ArrayList<>();

        @Override
        public int hashCode() {
            return parent.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof StopGroup))
                return false;
            return parent.equals(((StopGroup) o).parent);
        }
    }

    // Utility functions

    private Stop createStopFromMTAEntrance(Stop parent, MTAEntrance ent, int num) {
        int wheelchairFlag = NOT_WHEELCHAIR_ACCESSIBLE;
        if (contextualAccessibility && accessibleEntranceTypes.contains(ent.getEntranceType()))
            wheelchairFlag = WHEELCHAIR_ACCESSIBLE;
        Stop entrance = createStop(parent, LOCATION_TYPE_ENTRANCE, wheelchairFlag, "entrance-" + num);
        entrance.setLat(ent.getLatitude());
        entrance.setLon(ent.getLongitude());
        return entrance;
    }

    private Stop createNonAccessibleStreetEntrance(Stop parent) {
        return createStop(parent, LOCATION_TYPE_ENTRANCE, NOT_WHEELCHAIR_ACCESSIBLE, "ent");
    }

    private Stop createAccessibleStreetEntrance(Stop parent) {
        return createStop(parent, LOCATION_TYPE_ENTRANCE, WHEELCHAIR_ACCESSIBLE, "ent-acs");
    }

    private Stop createMezzanine(Stop parent, String name) {
        return createStop(parent, LOCATION_TYPE_GENERIC, WHEELCHAIR_ACCESSIBLE, "mezz-" + name);
    }

    private Stop createStop(Stop stop, int locationType, int wheelchairAccessible, String suffix) {
        Stop entrance = new Stop();
        AgencyAndId id = new AgencyAndId();
        id.setAgencyId(agencyId);
        id.setId(stop.getId().getId() + "-" + suffix);
        entrance.setId(id);
        entrance.setName(stop.getName());
        entrance.setLat(stop.getLat());
        entrance.setLon(stop.getLon());
        entrance.setLocationType(locationType);
        entrance.setWheelchairBoarding(wheelchairAccessible);
        entrance.setParentStation(stop.getId().getId());
        newStops.add(entrance);
        return entrance;
    }

    private void createElevPathways(Stop from, Stop to, String code, String idStr, Set<String> seenElevatorPathways) {
        if (seenElevatorPathways.contains(idStr)) {
            _log.debug("Duplicate elevator pathway id={}", code);
            return;
        }
        seenElevatorPathways.add(idStr);
        pathwayUtil.createPathway(from, to, PATHWAY_MODE_ELEVATOR, elevatorTraversalTime, elevatorTraversalTime, idStr, code);
    }

    private List<MTAElevator> getElevators() {
       return readCsv(MTAElevator.class, elevatorsCsv);
    }

    private List<MTAEntrance> getEntrances() {
        return readCsv(MTAEntrance.class, entrancesCsv);
    }

    public void setElevatorsCsv(String elevatorsCsv) {
        this.elevatorsCsv = elevatorsCsv;
    }

    public void setEntrancesCsv(String entrancesCsv) {
        this.entrancesCsv = entrancesCsv;
    }

    public void setGenericPathwayTraversalTime(int genericPathwayTraversalTime) {
        this.genericPathwayTraversalTime = genericPathwayTraversalTime;
    }

    public void setStairTraversalTime(int stairTraversalTime) {
        this.stairTraversalTime = stairTraversalTime;
    }

    public void setEscalatorTraversalTime(int escalatorTraversalTime) {
        this.escalatorTraversalTime = escalatorTraversalTime;
    }

    public void setWalkwayTraversalTime(int walkwayTraveralTime) {
        this.walkwayTraversalTime = walkwayTraveralTime;
    }

    public void setElevatorTraversalTime(int elevatorTraversalTime) {
        this.elevatorTraversalTime = elevatorTraversalTime;
    }

    public void setStopsHaveParents(boolean stopsHaveParents) {
        this.stopsHaveParents = stopsHaveParents;
    }

    public void setCreateMissingLinks(boolean createMissingLinks) {
        this.createMissingLinks = createMissingLinks;
    }

    public void setContextualAccessibility(boolean contextualAccessibility) {
        this.contextualAccessibility = contextualAccessibility;
    }
}


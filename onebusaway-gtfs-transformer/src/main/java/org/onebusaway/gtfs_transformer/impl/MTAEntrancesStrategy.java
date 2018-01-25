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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.onebusaway.gtfs_transformer.csv.CSVUtil.readCsv;

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

    private static final int PATHWAY_MODE_GENERIC = 0;
    private static final int PATHWAY_MODE_WALKWAY = 1;
    private static final int PATHWAY_MODE_STAIR = 2;
    private static final int PATHWAY_MODE_TRAVELATOR = 3;
    private static final int PATHWAY_MODE_ESCALATOR = 4;
    private static final int PATHWAY_MODE_ELEVATOR = 5;

    private static final Logger _log = LoggerFactory.getLogger(MTAEntrancesStrategy.class);

    @CsvField(ignore = true)
    private String agencyId;

    @CsvField(ignore = true)
    private Collection<Stop> newStops;

    @CsvField(ignore = true)
    private Collection<Pathway> newPathways;

    private String elevatorsCsv;

    private String entrancesCsv;

    @CsvField(optional = true)
    private int genericPathwayTraversalTime = 60;

    @CsvField(optional = true)
    private int stairTraversalTime = 60;

    @CsvField(optional = true)
    private int escalatorTraversalTime = 60;

    @CsvField(optional = true)
    private int walkwayTraveralTime = 60;

    @CsvField(optional = true)
    private int elevatorTraversalTime = 120;

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        agencyId = dao.getAllAgencies().iterator().next().getId();

        newStops = new HashSet<>();
        newPathways = new HashSet<>();

        Map<String, StopGroup> stopGroups = new HashMap<>();

        // For every stop that's not a station, add an entrance which is not wheelchair accessible, and a pathway.
        for (Stop stop : dao.getAllStops()) {
            // Put stop into a stop-group with parent, uptown, downtown
            String gid = stop.getLocationType() == LOCATION_TYPE_STOP ? stop.getParentStation() : stop.getId().getId();
            StopGroup group = stopGroups.get(gid);
            if (group == null) {
                group = new StopGroup();
                stopGroups.put(gid, group);
            }
            if (stop.getLocationType() == LOCATION_TYPE_STATION) {
                group.parent = stop;
            }
            else if (stop.getId().getId().endsWith("S")) {
                group.downtown = stop;
            } else if (stop.getId().getId().endsWith("N")) {
                group.uptown = stop;
            } else throw new RuntimeException("unexpected!");
        }

        readEntranceData(stopGroups);

        readElevatorData(stopGroups);

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
        // FYI, blacklist: Easement, Elevator, easement_elevator, easement_escalator, easement_stair, street_elevator
        List<String> whitelist = Arrays.asList("Door", "Escalator", "Ramp", "Stair", "Walkway", "street_escalator", "street_stair");

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
            if (group.entrances.isEmpty()) {
                _log.error("Station {} has no entrances", group.parent.getId());
                // mock, like we were doing before
                Stop entrance = createNonAccessibleStreetEntrance(group.parent);
                createPathway(entrance, group.uptown, PATHWAY_MODE_GENERIC, genericPathwayTraversalTime, -1, "GENERIC", null);
                createPathway(entrance, group.downtown, PATHWAY_MODE_GENERIC, genericPathwayTraversalTime, -1, "GENERIC", null);
                continue;
            }

            int i = 0;
            for (MTAEntrance entrance : group.entrances) {
                Stop entranceStop = createStopFromMTAEntrance(group.parent, entrance, i);
                int pathwayMode, traversalTime;
                switch(entrance.getEntranceType()) {
                    case "Stair":
                    case "street_stair":
                        pathwayMode = PATHWAY_MODE_STAIR;
                        traversalTime = stairTraversalTime;
                        break;
                    case "Ramp":
                    case "Walkway":
                        pathwayMode = PATHWAY_MODE_WALKWAY;
                        traversalTime = walkwayTraveralTime;
                        break;
                    case "Escalator":
                    case "street_escalator":
                        pathwayMode = PATHWAY_MODE_ESCALATOR;
                        traversalTime = escalatorTraversalTime;
                        break;
                    case "Door":
                    default:
                        pathwayMode = PATHWAY_MODE_GENERIC;
                        traversalTime = genericPathwayTraversalTime;
                }
                String id = entrance.getEntranceType() + "-" + i;
                // TODO: for stops with no free crossover, we should have identified which platform it goes to.
                createPathway(entranceStop, group.uptown, pathwayMode, traversalTime, -1, id, null);
                createPathway(entranceStop, group.downtown, pathwayMode, traversalTime, -1, id, null);
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
            Stop mezzanine = null;

            // elevator is defined by ID, type, and direction iff it includes platform
            Set<String> seenElevatorPathways = new HashSet<>();

            for (MTAElevator e : group.elevators) {
                ElevatorPathwayType type = ElevatorPathwayType.valueOf(e.getLoc());
                if (type == ElevatorPathwayType.UNKNOWN || type == ElevatorPathwayType.MEZZ_TO_MEZZ) {
                    unknown++;
                    _log.debug("unknown type={}, elev={}", e.getLoc(), e.getId());
                    continue;
                }
                if (entrance == null && type.shouldCreateStreetEntrance()) {
                    entrance = createAccessibleStreetEntrance(group.parent);
                }
                if (mezzanine == null && type.shouldCreateMezzanine()) {
                    mezzanine = createMezzanine(group.parent);
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
                    String id = "S2M_" + code;
                    createElevPathways(entrance, mezzanine, code, id, seenElevatorPathways);
                }

                if (type.shouldCreateMezzanineToPlatform()) {
                    String id = "M2P_" + code + "_" + e.getDirection();
                    createElevPathways(mezzanine, platform, code, id, seenElevatorPathways);
                }

                if (type.shouldCreateStreetToPlatform()) {
                    String id = "S2P_" + code + "_" + e.getDirection();
                    createElevPathways(entrance, platform, code, id, seenElevatorPathways);
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
        UNKNOWN;

        boolean shouldCreateStreetEntrance() {
            return this == STREET_TO_MEZZ || this == STREET_TO_MEZZ_TO_PLATFORM || this == STREET_TO_PLATFORM;
        }

        boolean shouldCreateMezzanine() {
            return this == STREET_TO_MEZZ_TO_PLATFORM | this == STREET_TO_MEZZ || this == MEZZ_TO_PLATFORM;
        }

        boolean shouldCreateStreetToMezzanine() {
            return this == STREET_TO_MEZZ_TO_PLATFORM || this == STREET_TO_MEZZ;
        }

        boolean shouldCreateMezzanineToPlatform() {
            return this == STREET_TO_MEZZ_TO_PLATFORM || this == MEZZ_TO_PLATFORM;
        }

        boolean shouldCreateStreetToPlatform() {
            return this == STREET_TO_MEZZ_TO_PLATFORM || this == STREET_TO_PLATFORM;
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
        Stop entrance = createStop(parent, LOCATION_TYPE_ENTRANCE, NOT_WHEELCHAIR_ACCESSIBLE, "entrance-" + num);
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

    private Stop createMezzanine(Stop parent) {
        return createStop(parent, LOCATION_TYPE_GENERIC, WHEELCHAIR_ACCESSIBLE, "mezz");
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
        newStops.add(entrance);
        return entrance;
    }

    private void createElevPathways(Stop from, Stop to, String code, String idStr, Set<String> seenElevatorPathways) {
        if (seenElevatorPathways.contains(idStr)) {
            _log.debug("Duplicate elevator pathway id={}", code);
            return;
        }
        seenElevatorPathways.add(idStr);
        createPathway(from, to, PATHWAY_MODE_ELEVATOR, elevatorTraversalTime, elevatorTraversalTime, idStr, code);
    }

    private void createPathway(Stop from, Stop to, int mode, int traversalTime, int wheelchairTraversalTime, String id, String code) {
        Pathway pathway = new Pathway();
        pathway.setFromStop(from);
        pathway.setToStop(to);
        pathway.setPathwayMode(mode);
        pathway.setTraversalTime(traversalTime);
        if (wheelchairTraversalTime > 0) {
            pathway.setWheelchairTraversalTime(wheelchairTraversalTime);
        }
        pathway.setId(new AgencyAndId(agencyId, to.getId().getId() + "-" + id + "-IN"));
        if (code != null) {
            pathway.setPathwayCode(code);
        }

        Pathway reverse = reverse(pathway, new AgencyAndId(agencyId, to.getId().getId() + "-" + id + "-OUT"));

        newPathways.add(pathway);
        newPathways.add(reverse);
    }

    private Pathway reverse(Pathway p, AgencyAndId id) {
        Pathway q = new Pathway();
        q.setFromStop(p.getToStop());
        q.setToStop(p.getFromStop());
        q.setTraversalTime(p.getTraversalTime());
        q.setWheelchairTraversalTime(p.getWheelchairTraversalTime());
        q.setPathwayMode(p.getPathwayMode());
        q.setPathwayCode(p.getPathwayCode());
        q.setId(id);
        return q;
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

    public void setWalkwayTraveralTime(int walkwayTraveralTime) {
        this.walkwayTraveralTime = walkwayTraveralTime;
    }

    public void setElevatorTraversalTime(int elevatorTraversalTime) {
        this.elevatorTraversalTime = elevatorTraversalTime;
    }
}


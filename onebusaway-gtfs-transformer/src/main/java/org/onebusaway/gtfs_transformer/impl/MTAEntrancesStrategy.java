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

import org.onebusaway.cloud.api.ExternalServices;
import org.onebusaway.cloud.api.ExternalServicesBridgeFactory;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FeedInfo;
import org.onebusaway.gtfs.model.Pathway;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.csv.MTAElevator;
import org.onebusaway.gtfs_transformer.csv.MTAEntrance;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.onebusaway.gtfs.model.Pathway.*;
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

    private static final String DEFAULT_MEZZ = "default";

    private static final String STOP_SEPARATOR = " ";

    private static final Logger _log = LoggerFactory.getLogger(MTAEntrancesStrategy.class);

    private static final List<String> accessibleEntranceTypes = Arrays.asList(
            "Ramp", "Walkway", "Road_Walkway", "Elevator", "Door", "Entrance", "Tunnel");

    @CsvField(ignore = true)
    private Set<AgencyAndId> stopIdsWithPathways = new HashSet<AgencyAndId>();
    
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

    @CsvField(optional = true)
    private String accessibleComplexFile;
    
    // control a few things so this can be reused for the railroads:
    private boolean stopsHaveParents;

    private boolean createMissingLinks;

    private boolean contextualAccessibility;

    @CsvField(optional = true)
    private boolean skipStopsWithExistingPathways = true;
    
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

    public String getName() {
      return this.getClass().getName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        ExternalServices es =  new ExternalServicesBridgeFactory().getExternalServices();
        Collection<FeedInfo> feedInfos = dao.getAllFeedInfos();
        String feed = null;
        if(feedInfos.size() > 0)
         feed = feedInfos.iterator().next().getPublisherName();
        File entrancesFile = new File(entrancesCsv);
        if(!entrancesFile.exists()) {
            es.publishMultiDimensionalMetric(getNamespace(),"MissingControlFiles",
                    new String[]{"feed","controlFileName"},
                    new String[]{feed,entrancesCsv},1);
            throw new IllegalStateException(
                    "Entrances file does not exist: " + entrancesFile.getName());
        }

        if (elevatorsCsv != null) {
            File elevatorsFile = new File(elevatorsCsv);
            if(!elevatorsFile.exists()) {
                es.publishMultiDimensionalMetric(getNamespace(),"MissingControlFiles",
                        new String[]{"feed","controlFileName"},
                        new String[]{feed,elevatorsCsv},1);
                throw new IllegalStateException(
                        "Elevators file does not exist: " + elevatorsFile.getName());
            }
        }

        agencyId = dao.getAllAgencies().iterator().next().getId();

        newStops = new HashSet<>();
        newPathways = new HashSet<>();

        pathwayUtil = new PathwayUtil(agencyId, newPathways);

        for (Pathway pathway : dao.getAllPathways()) {
        	stopIdsWithPathways.add(pathway.getFromStop().getId());
        	stopIdsWithPathways.add(pathway.getToStop().getId());
        }

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
                } else {
                	// it's a pathway, ignore
                	if(stop.getLocationType() >= 2)
                		continue;
                	
                    _log.error("unexpected stop not of parent type but of {} for stop {}: {}", stop.getLocationType(), stop.getId(), stop.getName());
                    continue;

                }
            } else {
                StopGroup group = new StopGroup();
                group.parent = stop;
                String gid = stop.getId().getId();
                stopGroups.put(gid, group);
            }
        }

        readEntranceData(stopGroups);

        if (elevatorsCsv != null) {
            readElevatorData(stopGroups, getComplexList(dao));
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
        	// pathways for any given station are supposed to be complete, so if we have at least one already there,
        	// there should be no more. Therefore, we can skip this stop completely. 
        	if(skipStopsWithExistingPathways &&
        			 (group.parent != null && stopIdsWithPathways.contains(group.parent.getId()) || 
        			 (group.uptown != null && stopIdsWithPathways.contains(group.uptown.getId())) ||
        			 (group.downtown != null && stopIdsWithPathways.contains(group.downtown.getId()))
        			)) {
                _log.info("Stop {} already has pathways from other sources; skipping.", group);
        		continue;
        	}

             if (group.entrances.isEmpty() && createMissingLinks) {
                _log.error("Station {} has no entrances", group.parent.getId());
                // mock, like we were doing before
                Stop entrance = createNonAccessibleStreetEntrance(group.parent);
                                
                pathwayUtil.createPathway(entrance, group.uptown, MODE_WALKWAY, genericPathwayTraversalTime, "GENERIC", null);
                pathwayUtil.createPathway(entrance, group.downtown, MODE_WALKWAY, genericPathwayTraversalTime, "GENERIC", null);
                continue;
            }

            int i = 0;
            for (MTAEntrance entrance : group.entrances) {
                Stop entranceStop = createStopFromMTAEntrance(group.parent, entrance, i);
                if (entranceStop == null) {
                    _log.error("data issue with entrance={}", entrance.getStopId());
                    continue;
                }
                int pathwayMode;
                int traversalTime;
                switch(entrance.getEntranceType()) {
                    case "Stair_Escalator": // treat as stair for now
                    case "Stair":
                        pathwayMode = MODE_STAIRS;
                        traversalTime = stairTraversalTime;
                        break;
                    case "Ramp":
                    case "Walkway":
                    case "Road_Walkway":
                        pathwayMode = MODE_STAIRS; // after discussion with MTA, most of these include stairs
                        traversalTime = walkwayTraversalTime;
                        break;
                    case "Escalator":
                        pathwayMode = MODE_ESCALATOR;
                        traversalTime = escalatorTraversalTime;
                        break;
                    case "Elevator":
                        pathwayMode = MODE_ELEVATOR;
                        traversalTime = elevatorTraversalTime;
                        break;
                    case "Door":
                    case "Entrance":
                    default:
                        pathwayMode = MODE_STAIRS; // after discussion with MTA, most of these include stairs
                        traversalTime = genericPathwayTraversalTime;
                }
                String id = entrance.getEntranceType() + "-" + i;
                if (stopsHaveParents) {
                    if (!entrance.hasDirection() || entrance.getDirection().equals("N")) {
                        pathwayUtil.createPathway(entranceStop, group.uptown, pathwayMode, traversalTime, id, null);
                    }
                    if (!entrance.hasDirection() || entrance.getDirection().equals("S")) {
                        pathwayUtil.createPathway(entranceStop, group.downtown, pathwayMode, traversalTime, id, null);
                    }
                } else {
                    pathwayUtil.createPathway(entranceStop, group.parent, pathwayMode, traversalTime, id, null);
                }
                i++;
            }
        }
    }

    private void readElevatorData(Map<String, StopGroup> stopGroups, Map<String, List<Stop>> complexIdToStops) {
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
        	// pathways for any given station are supposed to be complete, so if we have at least one already there,
        	// there should be no more. Therefore, we can skip this stop completely. 
        	if(skipStopsWithExistingPathways &&
        			 (group.parent != null && stopIdsWithPathways.contains(group.parent.getId()) || 
        			 (group.uptown != null && stopIdsWithPathways.contains(group.uptown.getId())) ||
        			 (group.downtown != null && stopIdsWithPathways.contains(group.downtown.getId()))
        			)) {
                _log.info("Stop {} already has pathways from other sources; skipping.", group);
        		continue;
        	}
        	
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
                
                if (type.shouldCreateMezzanine()) {
                    for (String name : type.mezzanineNames) {
                        Stop m = mezzByName.get(name);
                        if (m == null) {
                            m = createMezzanine(group.parent, name);
                            mezzByName.put(name, m);
                        }
                    }

                    // if we're linking to a platform and that platform is part of a complex,
                    // we need to create a complex mezzanine which connects all elevators that
                    // serve platforms that are part of the complex
                    for(String complexId : complexIdToStops.keySet()) {
                    	List<Stop> stopsInComplex = complexIdToStops.get(complexId);

                    	if(stopsInComplex.contains(platform)) {
                    		AgencyAndId mezzName = new AgencyAndId(platform.getId().getAgencyId(), "mezz-" + complexId);
                            Stop m = mezzByName.get(AgencyAndId.convertToString(mezzName));
                            if (m == null) {
                        		m = createMezzanineWithId(group.parent, mezzName);
                                mezzByName.put(AgencyAndId.convertToString(mezzName), m);
                            }
                    		break;
                    	}
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
                        
                        // if we're adding a *to platform elevator and the platform is part of a complex,
                        // link to the complex mezzanine that is connected to other platforms in the complex
                        // vs. the default mezzanine
                        boolean foundInComplex = false;
                        for(String complexId : complexIdToStops.keySet()) {
                        	List<Stop> stopsInComplex = complexIdToStops.get(complexId);

                        	if(stopsInComplex.contains(platform)) {
                        		Stop complexMezz = mezzByName.get(AgencyAndId.convertToString(new AgencyAndId(platform.getId().getAgencyId(), "mezz-" + complexId)));
                                createElevPathways(complexMezz, platform, code, id, seenElevatorPathways);
                                foundInComplex = true;
                        		break;
                        	}
                        }
                        
                        if(!foundInComplex)
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

    private Map<String, List<Stop>> getComplexList(GtfsDao dao) {
        Map<String, Stop> stops = getStopMap(dao);
        Map<String, List<Stop>> complexes = new HashMap<String, List<Stop>>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(this.accessibleComplexFile)))) {
            String line;
            while ((line = br.readLine()) != null) {
                List<Stop> complex = new ArrayList<>();
                for (String id : line.split(STOP_SEPARATOR)) {
                    Stop stop = stops.get(id);
                    if (stop == null)
                        _log.info("null stop: {}", id);
                    complex.add(stop);
                }
                complexes.put("complex-" + UUID.randomUUID(), complex);
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
        
        public String toString() {
        	return parent + " -> (" + uptown + "," + downtown + ")";
        }
    }

    // Utility functions

    private Stop createStopFromMTAEntrance(Stop parent, MTAEntrance ent, int num) {
        int wheelchairFlag = NOT_WHEELCHAIR_ACCESSIBLE;
        if (contextualAccessibility && accessibleEntranceTypes.contains(ent.getEntranceType()))
            wheelchairFlag = WHEELCHAIR_ACCESSIBLE;
        Stop entrance = createStop(parent, LOCATION_TYPE_ENTRANCE, wheelchairFlag, "entrance-" + num);
        if (entrance == null) return null;
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

    private Stop createMezzanineWithId(Stop parent, AgencyAndId id) {
        return createStop(id, parent, LOCATION_TYPE_GENERIC, WHEELCHAIR_ACCESSIBLE);
    }

    private Stop createStop(Stop stop, int locationType, int wheelchairAccessible, String suffix) {
        AgencyAndId id = new AgencyAndId();
        id.setAgencyId(agencyId);
        id.setId(stop.getId().getId() + "-" + suffix);
        return createStop(id, stop, locationType, wheelchairAccessible);
    }
    
    private Stop createStop(AgencyAndId id, Stop stop, int locationType, int wheelchairAccessible) {
        if (stop == null) return null;
        Stop entrance = new Stop();
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
        pathwayUtil.createPathway(from, to, MODE_ELEVATOR, elevatorTraversalTime, idStr, code);
    }

    private String getTopic() {
        return System.getProperty("sns.topic");
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

    public void setAccessibleComplexFile(String accessibleCsv) {
    	this.accessibleComplexFile = accessibleCsv;
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

    public void setSkipStopsWithExistingPathways(boolean skipStopsWithExistingPathways) {
        this.skipStopsWithExistingPathways = skipStopsWithExistingPathways;
    }
    
    public void setContextualAccessibility(boolean contextualAccessibility) {
        this.contextualAccessibility = contextualAccessibility;
    }
    private String getNamespace(){
        return System.getProperty("cloudwatch.namespace");
    }
}


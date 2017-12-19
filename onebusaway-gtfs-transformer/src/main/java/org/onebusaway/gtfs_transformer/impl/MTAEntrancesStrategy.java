/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.csv_entities.CsvEntityReader;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Pathway;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.csv.MTAElevator;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MTAEntrancesStrategy implements GtfsTransformStrategy {

    /**
     * Create entrances/pathways for MTA GTFS.
     * Not-accessible street entrances are created for all stops.
     * Accessible street entrances (elevators) are determined from elevator_to_node.csv, if possible.
     * Potentially could be integrated with Station Planning entrances file in the future.
     *
     * See: https://docs.google.com/document/d/1qJOTe4m_a4dcJnvXYt4smYj4QQ1ejZ8CvLBYzDM5IyM/edit?ts=5a39452a
     *
     * assumptions/notes:
     * - mezzanine is shared
     * - "mezzanine and uptown" => street to mezz and uptown
     * - Good examples for assumptions: A09, Court Sq 719
     * - need to figure out what to do for cross-feed entities (PATH, LIRR)
     * - will need track info (look at 34th st/penn)
     */

    private static final int LOCATION_TYPE_STOP = 0;
    private static final int LOCATION_TYPE_STATION = 1;
    private static final int LOCATION_TYPE_ENTRANCE = 2;
    private static final int LOCATION_TYPE_PAYGATE = 3;
    private static final int LOCATION_TYPE_GENERIC = 4;


    private static final int WHEELCHAIR_ACCESSIBLE = 1;
    private static final int NOT_WHEELCHAIR_ACCESSIBLE = 2;

    private static final int PATHWAY_MODE_GENERIC = 0;
    private static final int PATHWAY_MODE_ELEVATOR = 5;

    private static final int GENERIC_PATHWAY_TRAVERSAL_TIME = 30;
    private static final int ELEVATOR_TRAVERSAL_TIME = 60;

    private static final Logger _log = LoggerFactory.getLogger(MTAEntrancesStrategy.class);

    @CsvField(ignore = true)
    private String agencyId;

    @CsvField(ignore = true)
    private Collection<Stop> newStops;

    @CsvField(ignore = true)
    private Collection<Pathway> newPathways;

    private String elevatorsCsv;

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        agencyId = dao.getAllAgencies().iterator().next().getId();

        newStops = new HashSet<>();
        newPathways = new HashSet<>();

        Map<String, StopGroup> stopGroups = new HashMap<>();

        // For every stop that's not a station, add an entrance which is not wheelchair accessible, and a pathway.
        for (Stop stop : dao.getAllStops()) {
            if (stop.getLocationType() == LOCATION_TYPE_STOP) {
                // create NON-WHEELCHAIR ACCESSIBLE entrances and pathways
                Stop entrance = createNonAccessibleStreetEntrance(stop);
                createPathway(entrance, stop, PATHWAY_MODE_GENERIC, GENERIC_PATHWAY_TRAVERSAL_TIME, -1, "GENERIC", null);
            }

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

        readElevatorData(stopGroups);

        for (Stop s : newStops) {
            dao.saveEntity(s);
        }

        for (Pathway pathway : newPathways) {
            dao.saveEntity(pathway);
        }
    }

    // elevators...

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
                ElevatorPathwayType type = ElevatorPathwayType.fromString(e.getLoc());
                if (type == ElevatorPathwayType.UNKNOWN) {
                    unknown++;
                    _log.warn("unknown type={}, elev={}", e.getLoc(), e.getId());
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

        static ElevatorPathwayType fromString(String s) {
            switch (s) {
                case "STREET TO MEZZANINE / MEZZANINE TO PLATFORM":
                case "MEZZANINE AND UPTOWN":
                case "MEZZANINE AND DOWNTOWN":
                case "STREET TO MEZZANINE & UPTOWN PLATFORM":
                case "STREET TO MEZZANINE &PLATFORMS":
                case "STREET TO MEZZANINE ALL TRAINS":
                    return STREET_TO_MEZZ_TO_PLATFORM;

                case "MEZZANINE TO STREET":
                case "STREET TO MEZZANINE":
                case "STREET TO TERMINAL/MEZZANINE":
                    // needs work:
                case "STREET TO LIRR PLATFORM A & MEZZANINE":
                case "STREET TO TA MEZZANINE AND LIRR OVERPASS":
                    return STREET_TO_MEZZ;

                case "CENTER ISLAND PLATFORM TO MEZZANINE":
                case "MEZZANINE TO PLATFORM":
                case "MEZZANINE TO PLATFORMS":
                case "MEZZANINE TO UPTOWN AND DOWNTOWN PLATFORMS":
                case "MEZZANINES TO UPTOWN 4 PLATFORM":
                case "MEZZANINES TO DOWNTOWN 4 PLATFORM":
                case "MEZZANINE TO DOWNTOWN B":
                case "MEZZANINE TO UPTOWN B":
                case "MEZZANINE TO UPTOWN":
                case "MEZZANINE TO DOWNTOWN":
                case "NORTHBOUND PLATFORM TO MEZZANINE":
                case "MEZZANINE TO 7 PLATFORM":
                case "MEZZANINE TO L PLATFORM":
                case "SOUTHBOUND PLATFORM TO MEZZANINE":
                case "MEZZANINE TO DOWNTOWN A,C&E PLATFORM":
                case "MEZZANINE TO UPTOWN A,C&E PLATFORM":
                    // likely work:
                case "MEZZANINE TO DOWNTOWN LOCAL PLATFORM":
                case "MEZZANINE TO EXPRESS PLATFORM":
                    return MEZZ_TO_PLATFORM;

                case "STREET TO PLATFORM":
                case "STREET TO NORTH BOUND PLATFORM":
                case "STREET TO SOUTH BOUND PLATFORM":
                case "STREET TO UPTOWN PLATFORM":
                case "STREET TO DOWNTOWN PLATFORM":
                case "STREET TO DOWNTOWN C PLATFORM AND S PLATFORM":
                    // likely work
                case "STREET TO MEZZANINE & OVERPASS":
                    return STREET_TO_PLATFORM;

                default:
                    return UNKNOWN;
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
            _log.error("Duplicate elevator pathway id={}", code);
            return;
        }
        seenElevatorPathways.add(idStr);
        createPathway(from, to, PATHWAY_MODE_ELEVATOR, ELEVATOR_TRAVERSAL_TIME, ELEVATOR_TRAVERSAL_TIME, idStr, code);
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
        CsvEntityReader reader = new CsvEntityReader();
        final List<MTAElevator> elevators = new ArrayList<>();
        reader.addEntityHandler(new EntityHandler() {
            @Override
            public void handleEntity(Object o) {
                elevators.add((MTAElevator) o);
            }
        });
        try {
            reader.readEntities(MTAElevator.class, new FileReader(elevatorsCsv));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return elevators;
    }

    public void setElevatorsCsv(String elevatorsCsv) {
        this.elevatorsCsv = elevatorsCsv;
    }
}


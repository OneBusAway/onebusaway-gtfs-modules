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
package org.onebusaway.gtfs_transformer.util;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Pathway;
import org.onebusaway.gtfs.model.Stop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class PathwayUtil {

    private final Logger _log = LoggerFactory.getLogger(PathwayUtil.class);

    public static final int PATHWAY_MODE_GENERIC = 0;
    public static final int PATHWAY_MODE_WALKWAY = 1;
    public static final int PATHWAY_MODE_STAIR = 2;
    public static final int PATHWAY_MODE_TRAVELATOR = 3;
    public static final int PATHWAY_MODE_ESCALATOR = 4;
    public static final int PATHWAY_MODE_ELEVATOR = 5;

    private String agencyId;

    private Collection<Pathway> newPathways;

    public PathwayUtil(String agencyId, Collection<Pathway> newPathways) {
        this.agencyId = agencyId;
        this.newPathways = newPathways;
    }

    public void createPathway(Stop from, Stop to, int mode, int traversalTime, int wheelchairTraversalTime, String id, String code) {
        createPathway(from, to, mode, traversalTime, wheelchairTraversalTime, id, code, true);
    }

    public void createPathway(Stop from, Stop to, int mode, int traversalTime, int wheelchairTraversalTime, String id, String code, boolean reverse) {
        if (to == null || to.getId() == null) {
            _log.error("invalid to {}", to);
            return;
        }
        if (from == null || from.getId() == null) {
            _log.error("invalid from {}", from);
            return;
        }
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

        if (reverse) {
            Pathway reversePathway = reverse(pathway, new AgencyAndId(agencyId, to.getId().getId() + "-" + id + "-OUT"));
            newPathways.add(reversePathway);
        }

        newPathways.add(pathway);
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
}

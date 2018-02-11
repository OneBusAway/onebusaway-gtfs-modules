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
package org.onebusaway.gtfs_transformer.util;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Pathway;
import org.onebusaway.gtfs.model.Stop;

import java.util.Collection;

public class PathwayUtil {

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

/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_transformer.util;

import java.util.Collection;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Pathway;
import org.onebusaway.gtfs.model.Stop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathwayUtil {

  private final Logger _log = LoggerFactory.getLogger(PathwayUtil.class);

  private String agencyId;

  private Collection<Pathway> newPathways;

  public PathwayUtil(String agencyId, Collection<Pathway> newPathways) {
    this.agencyId = agencyId;
    this.newPathways = newPathways;
  }

  public void createPathway(
      Stop from, Stop to, int mode, int traversalTime, String id, String code) {
    createPathway(from, to, mode, traversalTime, id, code, true);
  }

  public void createPathway(
      Stop from, Stop to, int mode, int traversalTime, String id, String code, boolean reverse) {
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
    pathway.setId(new AgencyAndId(agencyId, to.getId().getId() + "-" + id + "-IN"));

    if (reverse) {
      Pathway reversePathway =
          reverse(pathway, new AgencyAndId(agencyId, to.getId().getId() + "-" + id + "-OUT"));
      newPathways.add(reversePathway);
    }

    newPathways.add(pathway);
  }

  private Pathway reverse(Pathway p, AgencyAndId id) {
    Pathway q = new Pathway();
    q.setFromStop(p.getToStop());
    q.setToStop(p.getFromStop());
    q.setTraversalTime(p.getTraversalTime());
    q.setPathwayMode(p.getPathwayMode());
    q.setId(id);
    return q;
  }
}

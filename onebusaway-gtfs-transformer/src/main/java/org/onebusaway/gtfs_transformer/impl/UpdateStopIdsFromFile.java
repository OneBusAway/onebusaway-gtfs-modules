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
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

//based on the control file, we are changing from the "new" id to the "old" id
//we are only changing the id, nothing else
public class UpdateStopIdsFromFile implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(UpdateStopIdFromControlStrategy.class);

    private static final int NEW_STOP_ID = 1;
    private static final int OLD_STOP_ID = 2;

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        File controlFile = new File((String)context.getParameter("controlFile"));

        ExternalServices es =  new ExternalServicesBridgeFactory().getExternalServices();
        if(!controlFile.exists()) {
            es.publishMessage(getTopic(), "Agency: "
                    + dao.getAllAgencies().iterator().next().getId()
                    + " "
                    + dao.getAllAgencies().iterator().next().getName()
                    + " Control file does not exist: "
                    + controlFile.getName());
            throw new IllegalStateException(
                    "Control file does not exist: " + controlFile.getName());
        }

        List<String> controlLines = new InputLibrary().readList((String) context.getParameter("controlFile"));
        int matched = 0;
        int unmatched = 0;
        int duplicate = 0;

        AgencyAndId agencyAndId = dao.getAllStops().iterator().next().getId();

        for (String controlLine : controlLines) {
            String[] controlArray = controlLine.split(",");
            if (controlArray == null || controlArray.length < 2) {
                _log.info("bad control line {}", controlLine);
                continue;
            }
            String oldId = controlArray[OLD_STOP_ID];
            String newId = controlArray[NEW_STOP_ID];

            Stop stop = dao.getStopForId(new AgencyAndId(agencyAndId.getAgencyId(), newId));

            if (stop == null) {
                if (!newId.equals("0")) {
                    _log.info("missing stop for new id {}", newId);
                }
                else {
                    _log.error("No stop found for id {}", newId);
                }
                unmatched++;
                continue;
            }

            matched++;
            _log.error("Setting existing new id {} to old id {}", newId, oldId);
            stop.setId(new AgencyAndId(stop.getId().getAgencyId(), oldId));

        }
        _log.info("Complete with {} matched and {} unmatched and {} duplicates", matched, unmatched, duplicate);

    }

    private String getTopic() {
        return System.getProperty("sns.topic");
    }
}

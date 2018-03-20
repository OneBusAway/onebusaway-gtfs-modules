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
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

/**
 * using crontrol file re-map GTFS stop ids and other stop properties
 */
public class UpdateStopIdFromControlStrategy implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(UpdateStopIdFromControlStrategy.class);

    private static final int LOCATION_NAME_INDEX = 0;
    private static final int ATIS_ID_INDEX = 6;

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        GtfsMutableRelationalDao reference = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();

        List<String> controlLines = new InputLibrary().readList((String) context.getParameter("controlFile"));
        int matched = 0;
        int unmatched = 0;

        HashMap<String, Stop> referenceStops = new HashMap<>();
        for (Stop stop : reference.getAllStops()) {
            referenceStops.put(stop.getId().getId(), stop);
        }

        AgencyAndId agencyAndId = dao.getAllStops().iterator().next().getId();
        _log.info("Stop Agency: {}, dao AgencyAndId: {}", agencyAndId.getAgencyId(), getDaoAgencyId(dao));


        for (String controlLine : controlLines) {
            String[] controlArray = controlLine.split(",");
            if (controlArray == null || controlArray.length < 2) {
                _log.info("bad control line {}", controlLine);
                continue;
            }
            String referenceId = controlArray[LOCATION_NAME_INDEX];
            String atisId = controlArray[ATIS_ID_INDEX];

            Stop refStop = referenceStops.get(referenceId);
            if (refStop == null) {
                _log.info("missing reference stop {} for agency {}", referenceId, getReferenceAgencyId(reference));
                unmatched++;
                continue;
            }

            Stop atisStop = dao.getStopForId(new AgencyAndId(agencyAndId.getAgencyId(), atisId));
            if (atisStop == null) {
                _log.info("missing atis stop {} for agency {}", atisId, getDaoAgencyId(dao));
                unmatched++;
                continue;
            }
            matched++;
            atisStop.setName(refStop.getName());
            atisStop.setDirection(refStop.getDirection());
            atisStop.setId(refStop.getId());
        }
        _log.info("Complete with {} matched and {} unmatched", matched, unmatched);
    }

    @CsvField(ignore = true)
    private String _referenceAgencyId = null;
    private String getReferenceAgencyId(GtfsMutableRelationalDao dao) {
        if (_referenceAgencyId == null) {
            _referenceAgencyId = dao.getAllAgencies().iterator().next().getId();
        }
        return _referenceAgencyId;
    }

    @CsvField(ignore = true)
    private String _daoAgencyId = null;
    private String getDaoAgencyId(GtfsMutableRelationalDao dao) {
        if (_daoAgencyId == null) {
            _daoAgencyId = dao.getAllAgencies().iterator().next().getId();
        }
        return _daoAgencyId;
    }
}

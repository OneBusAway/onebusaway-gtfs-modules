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

import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class SanitizeStopIdForApiAccess implements GtfsTransformStrategy {
    private final Logger _log = LoggerFactory.getLogger(SanitizeStopIdForApiAccess.class);
    private final String sanitize_selector = "[\\[||\\:||\\]||\\@||\\.|| ||\\\\||\\(||\\)||\\_||\\-||\\/||\\\"]";

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        _log.info("Removing the following characters from trip names: [, :, ], \\, (, ), _, @, and .");
        Collection<Stop> Stops = dao.getAllStops();
        for (Stop stop : Stops) {
            stop.getId().setId(stop.getId().getId().replaceAll(sanitize_selector,""));
        }
    }
}

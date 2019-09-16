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
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collection;

public class SanitizeForApiAccess implements GtfsTransformStrategy {
    private final Logger _log = LoggerFactory.getLogger(SanitizeForApiAccess.class);

    private String identitybean;

    @CsvField(optional = true)
    private String regex = "[\\[\\]\\@\\.\\ \\:\\\\\\(\\)\\_\\-\\/\\\"]";

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        Collection<?> beans =  dao.getAllTrips();

        if (identitybean.equalsIgnoreCase("trip")) {
            _log.info("Removing characters from "  + identitybean + " using this regex: " + regex);
            beans = dao.getAllTrips();
        }
        else if (identitybean.equalsIgnoreCase("stop")) {
            _log.info("Removing characters from "  + identitybean + " using this regex: " + regex);
            beans = dao.getAllStops();
        }
        else if (identitybean.equalsIgnoreCase("route") ){
            _log.info("Removing characters from "  + identitybean + " using this regex: " + regex);
            beans = dao.getAllRoutes();
        }
        else{
            _log.error("No matching Bean Type "+identitybean);
            return;
        }

    }


    public void setIdentitybean(String identitybean){
        this.identitybean = identitybean;
    }

    public void setRegex(String regex){
        this.regex = regex;
    }
}

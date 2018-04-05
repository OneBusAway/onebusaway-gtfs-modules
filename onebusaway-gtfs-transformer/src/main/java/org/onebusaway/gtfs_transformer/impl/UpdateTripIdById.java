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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class UpdateTripIdById implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(UpdateTripIdById.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }


    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        GtfsMutableRelationalDao reference = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();
        RemoveEntityLibrary removeEntityLibrary = new RemoveEntityLibrary();

        //trips updated, array of mta_ids that we've updated
        HashMap<String, AgencyAndId> tripsUpdated = new HashMap<>();

        //trips to remove, list of trips that have duplicate mta_ids
        ArrayList<Trip> tripsToRemove = new ArrayList<>();

        for (Trip trip : dao.getAllTrips()) {
            if (trip.getMtaTripId() != null) {
                if (tripsUpdated.containsKey(trip.getMtaTripId())) {
                    tripsToRemove.add(trip);
                } else {
                    trip.setId(new AgencyAndId(trip.getId().getAgencyId(), trip.getMtaTripId()));
                    tripsUpdated.put(trip.getMtaTripId(), trip.getServiceId());
                }
            }
        }

        for (Trip tripToRemove : tripsToRemove) {
            removeEntityLibrary.removeTrip(dao, tripToRemove);
        }
    }
}

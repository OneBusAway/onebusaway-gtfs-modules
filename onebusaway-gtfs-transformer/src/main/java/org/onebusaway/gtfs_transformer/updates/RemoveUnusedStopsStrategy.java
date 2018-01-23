/**
 * Copyright (C) 2017 Tony Laidig
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

package org.onebusaway.gtfs_transformer.updates;

import java.util.Collection;
import java.util.List;

import java.util.HashSet;
import java.util.Set;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remove Stops that are not present in StopTimes
 * @author laidig
 *
 */

public class RemoveUnusedStopsStrategy implements GtfsTransformStrategy {
    private static Logger _log = LoggerFactory.getLogger(RemoveNonRevenueStopsStrategy.class);

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {

        int removedStopsCount = 0;

        HashSet<Stop> usedStops = new HashSet<>();
        HashSet<Stop> stopsToRemove = new HashSet<>();

        for (StopTime stopTime : dao.getAllStopTimes()) {
            usedStops.add(stopTime.getStop());
        }

        Collection<Stop> stops = dao.getAllStops();

        for (Stop stop : stops){
            if (!usedStops.contains(stop)){
                // modifying the DAO at this point risks ConcurrentModificationException
                stopsToRemove.add(stop);
                removedStopsCount++;
            }
        }

        for (Stop stopToRemove : stopsToRemove ){
            dao.removeEntity(stopToRemove);
        }

        _log.info("removed=" + removedStopsCount + " of " + stops.size() + " stops");

        UpdateLibrary.clearDaoCache(dao);
    }

    private boolean isNonRevenue (StopTime s){
        return (s.getDropOffType() ==1 && s.getPickupType() ==1);
    }
}

/**
 * Copyright (C) 2024 Tisseo Voyageurs.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.geom.*;

import org.onebusaway.collections.beans.PropertyPathExpression;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.onebusaway.gtfs_transformer.updates.TrimTripTransformStrategy;
import org.onebusaway.gtfs_transformer.updates.TrimTripTransformStrategy.TrimOperation;
import org.onebusaway.gtfs_transformer.match.PropertyValueEntityMatch;
import org.onebusaway.gtfs_transformer.match.SimpleValueMatcher;
import org.onebusaway.gtfs_transformer.match.TypedEntityMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * remove entities outside polygone geometry
 *      remove procedre is based on the trim_trip strategy
 *      providing the trip_id key and the parameters from_stop_id/to_stop_id
 * example: 
 *      {"op":"transform","class":"org.onebusaway.gtfs_transformer.impl.RemoveEntitiesOutsidePolygone","polygone": wkt_polygone ...}
 */

public class RemoveEntitiesOutsidePolygone implements GtfsTransformStrategy {
    private final Logger _log = LoggerFactory.getLogger(RemoveEntitiesOutsidePolygone.class);

    // retrieves string polygone from config file
    @CsvField(optional = true)
    private String polygone;

    public void setPolygone(String polygone) {
        this.polygone = polygone;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

     @Override
    public void run(TransformContext transformContext, GtfsMutableRelationalDao gtfsMutableRelationalDao) {
        
        Geometry geometry = buildPolygone(polygone);
        TrimTripTransformStrategy strategy = new TrimTripTransformStrategy();
        TransformContext context = new TransformContext();
        Map<Object, List<Object>> tripsWithStopsOutsidePolygone = new HashMap<>();

         // browse all trips
        for (Trip trip : gtfsMutableRelationalDao.getAllTrips()){
            for (StopTime stopTime: gtfsMutableRelationalDao.getStopTimesForTrip(trip)){
                
                Stop stop = gtfsMutableRelationalDao.getStopForId(stopTime.getStop().getId());
                if (! insidePolygon(geometry,stop.getLon(),stop.getLat())){
                    List<Object> stopsWithSequence = new ArrayList<>();
                    Integer totalStopTimes = gtfsMutableRelationalDao.getStopTimesForTrip(trip).size();
                    // append in List <stopTime_sequence_number,stopId,totalStopTimes>
                    stopsWithSequence.add(new Object[] {stopTime.getStopSequence(), stop.getId().getId(),totalStopTimes});
                    // update dictionary if stop is outside polygone
                    updateDict(tripsWithStopsOutsidePolygone,trip.getId(),stopsWithSequence);
                }
            }
        }
        // browse Map <tripsWithStopsOutsidePolygone>
        for (Map.Entry<Object,List<Object>> entry:  tripsWithStopsOutsidePolygone.entrySet()){
            
            TrimOperation operation = new TrimOperation();

            Boolean trimFrom = false;
            Boolean trimTo = false;
            Integer indexFromStopId = 0;
            Integer indexToStopId = 0;
            Integer allStopTimes = 0;
            String fromStopId = null; 
            String toStopId = null;
            Object firstObject = entry.getValue().get(0);
            Object[] firstStop = (Object[]) firstObject;
            Object[] lastStop = null;

            // get from stop id : fromStopId value, indexFromStopId
            if (firstStop.length > 0){
                fromStopId = (String) firstStop[1]; // get fromStopId
                indexFromStopId = (Integer) firstStop[0];
                allStopTimes = (Integer) firstStop[2] - 1;
            }
          
            // get to stop id : toStopId value, indexToStopIds
            if (entry.getValue().size() > 1) {
                Object lastObject = entry.getValue().get(entry.getValue().size() - 1);
                lastStop = (Object[]) lastObject;
                toStopId = (String) lastStop[1];
                indexToStopId = (Integer) lastStop[0];
            }

            // check whether trimFrom or trimTo is used in the trim operation 
            if (indexFromStopId != null && indexFromStopId > 0){
                trimFrom = true;
            }
            else if (indexToStopId > 0 && indexToStopId < allStopTimes){
                trimTo = true;
            }
            // operation object to used it int trim trip strategy
            PropertyPathExpression expression = new PropertyPathExpression("id");
            operation.setMatch(new TypedEntityMatch(Trip.class,
            new PropertyValueEntityMatch(expression,
            new SimpleValueMatcher(entry.getKey()))));
            
            if (Boolean.TRUE.equals(trimFrom))
                operation.setFromStopId(fromStopId);
            if (Boolean.TRUE.equals(trimTo))
                operation.setToStopId(toStopId);
            
            // add operation + execute strategy
            strategy.addOperation(operation);
            strategy.run(context, gtfsMutableRelationalDao);
        }
    }  

    /*
     *  updateDict : function used to add/update dictionary
     */
    private static void updateDict(Map<Object, List<Object>> dictionary, Object key, List<Object> values) {
        // If the key already exists, append the values to its existing list
        if (dictionary.containsKey(key)) {
            List<Object> existingValues = dictionary.get(key);
            existingValues.addAll(values);
        }
        // If the key is new, add it to the dictionary with the list of values
        else {
            dictionary.put(key, values);
        }
    }
    
    /*
     * create polygone/multiPolygone from polygone variable in json file
        * return Geometry variable
        * return null if an exception is encountered when parsing the wkt string
     */
    private Geometry buildPolygone(String wktPolygone) {
        WKTReader reader = new WKTReader();
        try{
            return  reader.read(wktPolygone);
        } catch (ParseException e){
            String message = String.format("Error parsing WKT string : %s", e.getMessage());
            _log.error(message);
            return null;
        }
        
    }
    /*
     * insidePolygone returns boolean variable
        * true: if polygone contains point
        * false if point is outside polygone
     */
    private boolean insidePolygon(Geometry geometry, double lon, double lat) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
        return geometry.contains(point);
    }

}

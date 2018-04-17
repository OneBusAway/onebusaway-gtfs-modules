package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

import java.io.IOException;

public class CountAndTestBus implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(CountAndTestBus.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        GtfsMutableRelationalDao reference = (GtfsMutableRelationalDao) context.getReferenceReader().getEntityStore();

        HashMap<String, Route> referenceRoutes = new HashMap<>();
        for (Route route : reference.getAllRoutes()) {
            referenceRoutes.put(route.getId().getId(), route);
        }

        HashMap<String, Trip> referenceTrips = new HashMap<>();
        for (Trip trip : reference.getAllTrips()) {
            referenceTrips.put(trip.getId().getId(), trip);
        }

        HashMap<String, Stop> referenceStops = new HashMap<>();
        for (Stop stop : reference.getAllStops()) {
            referenceStops.put(stop.getId().getId(), stop);
        }

        int matches = 0;
        for(Route route : dao.getAllRoutes()) {
            if (referenceRoutes.containsKey(route.getId().getId())){
                matches++;
            }
            else {
                _log.info("ATIS route {} doesn't have match in reference", route.getId().getId());
            }
        }
        _log.info("ATIS Routes: {}, References: {}, ATIS match to reference: {}", dao.getAllRoutes().size(), reference.getAllRoutes().size(), matches);

        matches = 0;
        for (Trip trip : dao.getAllTrips()) {
            if (referenceTrips.containsKey(trip.getId().getId())) {
                matches++;
            }
        }
        _log.info("ATIS Trips: {}, Reference: {}, ATIS match to reference: {}", dao.getAllTrips().size(), reference.getAllTrips().size(), matches);

        matches = 0;
        for (Stop stop : dao.getAllStops()) {
            if(referenceStops.containsKey(stop.getId().getId())){
                matches++;
            }
        }
        _log.info("ATIS Stops: {}, Reference: {}, ATIS match to reference: {}", dao.getAllStops().size(), reference.getAllStops().size(), matches);

        /* The following code is for counting bus trips, looking at what matches between the ATIS
        * and reference files and taking into consideration the Sdon trips in the reference file
        * Also prints out the trips that don't match
        *

        int sdonRef=0;
        int minusSdonRef=0;
        int minusSdonAtis=0;

        ArrayList<String> refsToRemove = new ArrayList<>();
        ArrayList<String> ATISToRemove = new ArrayList<>();

        for (HashMap.Entry<String, Trip> referenceTrip : referenceTrips.entrySet()) {
            Trip refTrip = referenceTrip.getValue();
            if (refTrip.getId().getId().contains("SDon")) {
                sdonRef++;
                String refId = refTrip.getId().getId();
                refsToRemove.add(refId);
                String refIdMinusSDon = refId.replace("-SDon", "");
                if(referenceTrips.containsKey(refIdMinusSDon)) {
                    minusSdonRef++;
                    refsToRemove.add(refIdMinusSDon);
                }
                if (atisTrips.containsKey(refIdMinusSDon)) {
                   minusSdonAtis++;
                   ATISToRemove.add(refIdMinusSDon);
                }
            }
        }

        for (String toRemove : refsToRemove) {
            referenceTrips.remove(toRemove);
        }

        for (String toRemove : ATISToRemove) {
            atisTrips.remove(toRemove);
        }

        _log.info("Sdon in ref: {}, minusSdon in ref: {}, minusSdon in ATIS: {}", sdonRef, minusSdonRef, minusSdonAtis);
        _log.info("Total ATIS Trips: {} and {}", dao.getAllTrips().size(), atisTrips.size());
        _log.info("Total Ref Trips: {} and {}", reference.getAllTrips().size(), referenceTrips.size());

        int inBoth=0;
        ArrayList<String> inBothLists = new ArrayList<>();

        for (HashMap.Entry<String, Trip> referenceTrip : referenceTrips.entrySet()) {
            if(atisTrips.containsKey(referenceTrip.getKey())) {
                inBoth++;
                inBothLists.add(referenceTrip.getKey());
            }

        }
        _log.info("In both {}", inBoth);

        for (String toRemove : inBothLists) {
            referenceTrips.remove(toRemove);
        }

        for (String toRemove : inBothLists) {
            atisTrips.remove(toRemove);
        }

        _log.info("Total ATIS Trips: {} and {}", dao.getAllTrips().size(), atisTrips.size());
        _log.info("Total Ref Trips: {} and {}", reference.getAllTrips().size(), referenceTrips.size());


        //write out remaining to files
        ArrayList<String> daoTripList = new ArrayList<String>();

        int inDaoNotRef = 0;
        int inboth = 0;
        int inRefNotDao = 0;
        int daoTrips = 0;
        int refTrips = 0;

        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("filename.txt"), "utf-8"));
            writer.write("Trips in ATIS not Reference\n");
            for (Trip trip: dao.getAllTrips()) {
                daoTrips++;
                daoTripList.add(trip.getId().getId());
                if (referenceTrips.get(trip.getId().getId()) == null){
                    inDaoNotRef++;
                    String line = trip.getId().getId() + "\n";
                    writer.write(line);
                }
                else {
                    inboth++;
                }
            }
        } catch (IOException ex) {
            // Report
        } finally {
            try {writer.close();} catch (Exception ex) { }
        }

        _log.info("Dao trips: {}", daoTrips);
        _log.info("In DaoNotRef: {} in both: {}", inDaoNotRef, inboth);

        inboth = 0;

        Writer writer2 = null;
        try {
            writer2 = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("filename2.txt"), "utf-8"));
            writer2.write("Trips in Reference not in ATIS\n");
            for (HashMap.Entry<String, Trip> refTrip : referenceTrips.entrySet()) {
                refTrips++;
                if (daoTripList.contains(refTrip.getKey())){
                    inboth++;
                } else {
                    String line = refTrip.getValue().getId().getId() + "\n";
                    writer2.write(line);
                    inRefNotDao++;
                }
            }
        } catch (IOException ex) {
            // Report
        } finally {
            try {writer2.close();} catch (Exception ex) { }
        }

        _log.info("Ref trips: {}", refTrips);
        _log.info("In RefNotDao: {} in both: {}", inRefNotDao, inboth);
    */

    }
}
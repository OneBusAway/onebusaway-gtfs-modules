package org.onebusaway.gtfs.serialization;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.services.GtfsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

public class RouteWriter {

    private final Logger _log = LoggerFactory.getLogger(RouteWriter.class);

    private String ARG_ROUTES_OUTPUT_NAME;


    public File _outputLocation;

    public RouteWriter(){

    }

    public void setOutputLocation(File outputLocation){
        _outputLocation = outputLocation;
    }
    public void setRoutesOutputLocation(String routesOutputName){
        ARG_ROUTES_OUTPUT_NAME = routesOutputName;
    }


    public void run(GtfsDao dao) throws IOException {

        Collection<Route> routes = dao.getAllRoutes();
        String output = "";
        for (Route route : routes) {
            output += route.getId().getAgencyId() + "***" + route.getId().getId() +",";
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(_outputLocation+"/" + ARG_ROUTES_OUTPUT_NAME));
            writer.write(output);
            writer.close();
        }
        catch (IOException exception){
            _log.error("Issue writing listOfRoutes in ModTask/NycGtfsModTask for later use in FixedRouteValidationTask");
        }
    }
}

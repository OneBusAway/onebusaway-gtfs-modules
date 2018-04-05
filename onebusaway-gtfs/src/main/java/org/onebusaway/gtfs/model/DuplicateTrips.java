package org.onebusaway.gtfs.model;

import java.util.ArrayList;

public class DuplicateTrips {

    private String id;

    private String serviceId;

    private ArrayList<Trip> trips = new ArrayList<Trip>();

    private ArrayList<ServiceCalendarDate> dates = new ArrayList<ServiceCalendarDate>();

    public DuplicateTrips() {

    }

    public DuplicateTrips(String id, String svcId, ArrayList<Trip> trips) {
        this.setId(id);
        this.setServiceId(svcId);
        this.setTrips(trips);
    }

    public DuplicateTrips(DuplicateTrips dts) {
        this.setId(dts.getId());
        this.setServiceId(dts.getServiceId());
        this.setTrips(dts.getTrips());
        this.setDates(dts.getDates());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public ArrayList<Trip> getTrips() {
        return trips;
    }

    public void setTrips(ArrayList<Trip> trips) {
        this.trips = trips;
    }

    public void addTrip(Trip trip) {
        this.trips.add(trip);
    }

    public ArrayList<ServiceCalendarDate> getDates() {
        return dates;
    }

    public void setDates(ArrayList<ServiceCalendarDate> dates) {
        this.dates = dates;
    }

    public void addServiceDate(ServiceCalendarDate date) {
        this.dates.add(date);
    }


}

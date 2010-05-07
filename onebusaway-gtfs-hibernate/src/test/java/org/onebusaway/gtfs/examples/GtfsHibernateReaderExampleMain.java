package org.onebusaway.gtfs.examples;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.HibernateGtfsFactory;
import org.onebusaway.gtfs.services.calendar.CalendarService;

public class GtfsHibernateReaderExampleMain {

  private static final String KEY_CLASSPATH = "classpath:";

  private static final String KEY_FILE = "file:";

  public static void main(String[] args) throws IOException {

    if (!(args.length == 1 || args.length == 2)) {
      System.err.println("usage: gtfsPath [hibernate-config.xml]");
      System.exit(-1);
    }

    String resource = "classpath:org/onebusaway/gtfs/examples/hibernate-configuration-examples.xml";
    if (args.length == 2)
      resource = args[1];

    HibernateGtfsFactory factory = createHibernateGtfsFactory(resource);

    GtfsReader reader = new GtfsReader();
    reader.setInputLocation(new File(args[0]));

    GtfsMutableRelationalDao dao = factory.getDao();
    reader.setEntityStore(dao);
    reader.run();

    Collection<Stop> stops = dao.getAllStops();

    for (Stop stop : stops)
      System.out.println(stop.getName());

    CalendarService calendarService = factory.getCalendarService();
    Set<AgencyAndId> serviceIds = calendarService.getServiceIds();

    for (AgencyAndId serviceId : serviceIds) {
      Set<ServiceDate> dates = calendarService.getServiceDatesForServiceId(serviceId);
      ServiceDate from = null;
      ServiceDate to = null;
      for (ServiceDate date : dates) {
        from = min(from, date);
        to = max(to, date);
      }

      System.out.println("serviceId=" + serviceId + " from=" + from + " to="
          + to);
    }
  }

  private static ServiceDate min(ServiceDate a, ServiceDate b) {
    if (a == null)
      return b;
    if (b == null)
      return a;
    return a.compareTo(b) <= 0 ? a : b;
  }

  private static ServiceDate max(ServiceDate a, ServiceDate b) {
    if (a == null)
      return b;
    if (b == null)
      return a;
    return a.compareTo(b) <= 0 ? b : a;
  }

  private static HibernateGtfsFactory createHibernateGtfsFactory(String resource) {

    Configuration config = new Configuration();

    if (resource.startsWith(KEY_CLASSPATH)) {
      resource = resource.substring(KEY_CLASSPATH.length());
      config = config.configure(resource);
    } else if (resource.startsWith(KEY_FILE)) {
      resource = resource.substring(KEY_FILE.length());
      config = config.configure(new File(resource));
    } else {
      config = config.configure(new File(resource));
    }

    SessionFactory sessionFactory = config.buildSessionFactory();
    return new HibernateGtfsFactory(sessionFactory);
  }
}

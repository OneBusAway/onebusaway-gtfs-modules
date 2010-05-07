package org.onebusaway.gtfs.services;

import org.hibernate.SessionFactory;
import org.onebusaway.gtfs.impl.HibernateGtfsRelationalDaoImpl;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.services.calendar.CalendarService;

/**
 * Convenience factory methods for constructing Hibernate-based implementations
 * of various GTFS service interfaces.
 * 
 * @author bdferris
 */
public class HibernateGtfsFactory {

  private SessionFactory _sessionFactory;

  private GtfsMutableRelationalDao _dao;

  private CalendarService _calendarService;

  public HibernateGtfsFactory() {

  }

  public HibernateGtfsFactory(SessionFactory sessionFactory) {
    setSessionFactory(sessionFactory);
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  public GtfsMutableRelationalDao getDao() {
    if (_dao == null)
      _dao = new HibernateGtfsRelationalDaoImpl(_sessionFactory);
    return _dao;
  }

  public CalendarService getCalendarService() {
    if (_calendarService == null)
      _calendarService = new CalendarServiceImpl(
          new CalendarServiceDataFactoryImpl(getDao()));
    return _calendarService;
  }
}

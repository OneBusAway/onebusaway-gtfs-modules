package org.onebusaway.gtfs.services;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Session;

public interface HibernateOperation {

  public Object doInHibernate(Session session) throws HibernateException, SQLException;

}

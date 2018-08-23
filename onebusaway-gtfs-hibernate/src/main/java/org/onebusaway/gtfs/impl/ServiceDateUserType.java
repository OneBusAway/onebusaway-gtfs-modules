/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.gtfs.impl;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

public class ServiceDateUserType implements UserType {

  private static final int[] SQL_TYPES = {Types.VARCHAR};

  @Override
  public Class<?> returnedClass() {
    return ServiceDate.class;
  }

  @Override
  public int[] sqlTypes() {
    return SQL_TYPES;
  }

  @Override
  public boolean equals(Object x, Object y) throws HibernateException {
    return x == y;
  }

  @Override
  public int hashCode(Object x) throws HibernateException {
    return x.hashCode();
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public Object deepCopy(Object value) throws HibernateException {
    if (value == null) {
      return null;
    }
    return new ServiceDate((ServiceDate) value);
  }

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
			throws HibernateException, SQLException {

	    String value = rs.getString(names[0]);

	    if (rs.wasNull())
	      return null;

	    try {
	      return ServiceDate.parseString(value);
	    } catch (ParseException ex) {
	      throw new SQLException("error parsing service date value: " + value, ex);
	    }
	}
	
	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
			throws HibernateException, SQLException {

	    if (value == null) {
	      st.setNull(index, SQL_TYPES[0]);
	    } else {
	      ServiceDate serviceDate = (ServiceDate) value;
	      st.setString(index, serviceDate.getAsString());
	    }
	}
	
  @Override
  public Object assemble(Serializable cached, Object owner)
      throws HibernateException {
    return deepCopy(cached);
  }

  @Override
  public Serializable disassemble(Object value) throws HibernateException {
    return (Serializable) deepCopy(value);
  }

  @Override
  public Object replace(Object original, Object target, Object owner)
      throws HibernateException {
    if (original == null)
      return null;
    return deepCopy(original);
  }

}

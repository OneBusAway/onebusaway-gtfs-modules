package org.onebusaway.gtfs.impl.calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

public class CalendarServiceImplTest {

  @Test
  public void test() throws IOException {

    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    GtfsTestData.readGtfs(dao, GtfsTestData.getIslandGtfs(), "26");

    CalendarServiceDataFactoryImpl factory = new CalendarServiceDataFactoryImpl();
    factory.setGtfsDao(dao);

    CalendarServiceData data = factory.createData();

    CalendarServiceImpl service = new CalendarServiceImpl();
    service.setData(data);

    ServiceDate from = new ServiceDate(2008, 10, 27);
    ServiceDate to = new ServiceDate(2009, 9, 27);

    Set<ServiceDate> toExclude = new HashSet<ServiceDate>();
    toExclude.add(new ServiceDate(2009, 1, 1));

    // 23,1,1,1,1,1,0,0,20081027,20090927
    Set<ServiceDate> dates = service.getServiceDatesForServiceId(new AgencyAndId(
        "26", "23"));
    assertEquals(239, dates.size());

    Date fromDate = from.getAsDate();
    Date toDate = to.getAsDate();

    Calendar c = Calendar.getInstance();
    c.setTime(fromDate);

    while (c.getTime().compareTo(toDate) <= 0) {
      ServiceDate day = new ServiceDate(c);
      int dow = c.get(Calendar.DAY_OF_WEEK);

      if (!(dow == Calendar.SATURDAY || dow == Calendar.SUNDAY || toExclude.contains(day))) {
        assertTrue(dates.contains(day));
      }

      c.add(Calendar.DAY_OF_YEAR, 1);
    }
  }
}

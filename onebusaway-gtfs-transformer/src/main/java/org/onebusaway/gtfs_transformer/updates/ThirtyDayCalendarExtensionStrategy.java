/**
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


import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class ThirtyDayCalendarExtensionStrategy implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(ThirtyDayCalendarExtensionStrategy.class);
    private final long milisPerDay = 24 * 60 * 60 * 1000;

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {


        // gets serviceIds for seven days ago till 31 days from now
        Map<Date,List<IdentityBean<Integer>>> serviceIdsByDate = getServiceIdsByDate(
                dao,
                removeTime(new Date(System.currentTimeMillis() - 7 * milisPerDay)),
                removeTime(new Date(System.currentTimeMillis() + 31 * milisPerDay)));;

       //  for the week 30 days from now, ensures there's an active calendar
       for (int i = 31; i > 24; i--){
           recursivelyEnsureActiveCalendar(i, serviceIdsByDate, dao);
       }

    }

    private List<IdentityBean<Integer>> recursivelyEnsureActiveCalendar(int daysFromNow,
                                                               Map<Date,List<IdentityBean<Integer>>> serviceIdsByDate,
                                                               GtfsMutableRelationalDao dao){
        if(daysFromNow < 0){
            return null;
        }
        Date date = removeTime(new Date(System.currentTimeMillis() + daysFromNow * milisPerDay));
        List<IdentityBean<Integer>> activeServiceIds = serviceIdsByDate.get(date);
        if (activeServiceIds == null){
            activeServiceIds = recursivelyEnsureActiveCalendar(daysFromNow-7, serviceIdsByDate,dao);
        }
        if(activeServiceIds!=null){
            _log.info("updating empty service ids for " + date);
            // update service ids in dao
            for (IdentityBean<Integer> serviceId : activeServiceIds) {
                if(serviceId instanceof ServiceCalendar){
                    ServiceCalendar serviceCalendar = (ServiceCalendar) serviceId;
                    if(date.after(serviceCalendar.getEndDate().getAsDate())) {
                        serviceCalendar.setEndDate(new ServiceDate(date));
                        dao.saveOrUpdateEntity(serviceCalendar);
                    }
                }
                if(serviceId instanceof ServiceCalendarDate){
                    ServiceCalendarDate serviceCalendarDate = (ServiceCalendarDate) serviceId;
                    ServiceCalendarDate newServiceCalendarDate = new ServiceCalendarDate();
                    newServiceCalendarDate.setDate(new ServiceDate(date));
                    newServiceCalendarDate.setServiceId(serviceCalendarDate.getServiceId());
                    newServiceCalendarDate.setExceptionType(serviceCalendarDate.getExceptionType());
                    dao.saveOrUpdateEntity(newServiceCalendarDate);
                }
                dao.flush();
            }
        }
        return activeServiceIds;
    }

    private Date removeTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        date = calendar.getTime();
        return date;
    }

    private Date constructDate(ServiceDate date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, date.getYear());
        calendar.set(Calendar.MONTH, date.getMonth()-1);
        calendar.set(Calendar.DATE, date.getDay());
        Date date1 = calendar.getTime();
        date1 = removeTime(date1);
        return date1;
    }


    private Map<Date, List<IdentityBean<Integer>>> getServiceIdsByDate (GtfsMutableRelationalDao dao,
                                                              Date minDate,
                                                              Date maxDate){
        Map<Date,List<IdentityBean<Integer>>> serviceIdsByDate = new HashMap<>();


        for(AgencyAndId serviceId : dao.getAllServiceIds()){
            //if there are no entries in calendarDates, check serviceCalendar
            ServiceCalendar servCal = dao.getCalendarForServiceId(serviceId);
            if (servCal != null) {
                //check for service using calendar
                Date start = removeTime(servCal.getStartDate().getAsDate());
                if(minDate.after(start)){start = minDate;}
                Date end = removeTime(addDays(servCal.getEndDate().getAsDate(),1));
                if (end.after(maxDate)){end = maxDate;}
                int dayIndexCounter = 0;
                Date index = removeTime(addDays(start, dayIndexCounter));
                int[] activeDays = {0,
                        servCal.getSunday(),
                        servCal.getMonday(),
                        servCal.getTuesday(),
                        servCal.getWednesday(),
                        servCal.getThursday(),
                        servCal.getFriday(),
                        servCal.getSaturday(),
                };

                while(index.before(end)){
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(index);
                    int day = calendar.get(Calendar.DAY_OF_WEEK);
                    if(activeDays[day] == 1) {
                        if (serviceIdsByDate.get(index) == null) {
                            serviceIdsByDate.put(index, new ArrayList<>());
                        }
                        serviceIdsByDate.get(index).add(servCal);
                    }
                    dayIndexCounter += 1;
                    index = removeTime(addDays(start, dayIndexCounter));
                }
            }

            for (ServiceCalendarDate calDate : dao.getCalendarDatesForServiceId(serviceId)) {
                Date date = constructDate(calDate.getDate());
                if(date.after(maxDate)){continue;}
                if(minDate.after(date)){continue;}
                if(calDate.getExceptionType() == 1){
                    if (serviceIdsByDate.get(date) == null) {
                        serviceIdsByDate.put(date, new ArrayList<>());
                    }
                    serviceIdsByDate.get(date).add(calDate);
                }
                if(calDate.getExceptionType() == 2){
                    if(serviceIdsByDate.get(date) != null) {
                        serviceIdsByDate.get(date).remove(serviceId);
                        if (serviceIdsByDate.get(date).size() == 0) {
                            serviceIdsByDate.remove(date);
                        }
                    }
                }

            }

        }
        LinkedHashMap<Date,List<IdentityBean<Integer>>> sortedServiceIdsByDate = new LinkedHashMap<>();
        serviceIdsByDate.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEachOrdered(x -> sortedServiceIdsByDate.put(x.getKey(), x.getValue()));
        return sortedServiceIdsByDate;
    }

    private Date addDays(Date date, int daysToAdd) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, daysToAdd);
        return cal.getTime();
    }
}

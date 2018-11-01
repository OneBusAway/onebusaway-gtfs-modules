/**
 * Copyright (C) 2018 Metropolitan Transportation Authority
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

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.mappings.ServiceDateFieldMappingFactory;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

import java.util.ArrayList;

public class CalendarTruncationStrategy implements GtfsTransformStrategy {

    @CsvField(mapping = ServiceDateFieldMappingFactory.class)
    private ServiceDate calendarEndDate;

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        for (ServiceCalendar sc : dao.getAllCalendars()) {
            if (sc.getEndDate().compareTo(calendarEndDate) > 0) {
                sc.setEndDate(calendarEndDate);
                dao.updateEntity(sc);
            }
        }

        for (ServiceCalendarDate scd : new ArrayList<>(dao.getAllCalendarDates())) {
            if (scd.getDate().compareTo(calendarEndDate) > 0) {
                dao.removeEntity(scd);
            }
        }
    }

    public void setCalendarEndDate(ServiceDate calendarEndDate) {
        this.calendarEndDate = calendarEndDate;
    }

}

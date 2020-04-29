/**
 * Copyright (C) 2020 Hannes Junnila <hannes@kyyti.com>
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
package org.onebusaway.gtfs.serialization.mappings;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;
import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsReaderContext;

public class StoplikeFieldMappingImpl extends EntityFieldMappingImpl {
    public StoplikeFieldMappingImpl(Class<?> entityType, String csvFieldName, String objFieldName, Class<?> objFieldType, boolean required) {
        super(entityType, csvFieldName, objFieldName, objFieldType, required);
    }

    @Override
    public Converter create(CsvEntityContext context) {
        GtfsReaderContext ctx = (GtfsReaderContext) context.get(GtfsReader.KEY_CONTEXT);
        return new ConverterImpl(ctx);
    }

    private class ConverterImpl implements Converter {

        private GtfsReaderContext _context;

        public ConverterImpl(GtfsReaderContext context) {
            _context = context;
        }

        @Override
        public Object convert(@SuppressWarnings("rawtypes") Class type, Object value) {
            if (type == String.class) {
                if (value instanceof String)
                    return (String) value;
            } else if (type == Stoplike.class) {
                String entityId = value.toString();


                String agencyId = _context.getDefaultAgencyId();
                AgencyAndId id = new AgencyAndId(agencyId, entityId);
                Object stop =  _context.getEntity(Stop.class, id);
                if (stop != null) return stop;
                Object location =  _context.getEntity(Location.class, id);
                if (location != null) return location;
                Object locationGroup =  _context.getEntity(LocationGroup.class, id);
                if (locationGroup != null) return locationGroup;
            }
            throw new ConversionException("Could not convert " + value + " of type "
                    + value.getClass() + " to " + type);
        }
    }
}

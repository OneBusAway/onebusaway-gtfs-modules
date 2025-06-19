/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_transformer.factory;

import java.text.ParseException;
import org.apache.commons.beanutils2.Converter;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

public class ServiceDateConverter implements Converter {

  @Override
  public Object convert(@SuppressWarnings("rawtypes") Class type, Object value) {
    try {
      return ServiceDate.parseString((String) value);
    } catch (ParseException e) {
      throw new IllegalStateException(e);
    }
  }
}

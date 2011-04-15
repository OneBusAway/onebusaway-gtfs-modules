/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.gtfs.serialization;

import org.onebusaway.csv_entities.exceptions.CsvEntityException;
import org.onebusaway.gtfs.model.AgencyAndId;

/**
 * Indicates that two entities with the same id were found in a GTFS feed as it
 * was being read.
 * 
 * @author bdferris
 * 
 */
public class DuplicateEntityException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  public DuplicateEntityException(Class<?> entityType, AgencyAndId id) {
    super(entityType, "duplicate entity id: type=" + entityType.getName()
        + " agencyId=" + id.getAgencyId() + " id=" + id.getId());
  }
}

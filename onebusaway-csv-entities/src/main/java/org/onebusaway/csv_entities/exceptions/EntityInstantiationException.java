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
package org.onebusaway.csv_entities.exceptions;

/**
 * Indicates an error when attempting to instantiate an instance of the
 * specified entity type
 * 
 * @author bdferris
 */
public class EntityInstantiationException extends CsvEntityException {

  private static final long serialVersionUID = 1L;

  public EntityInstantiationException(Class<?> entityType, Throwable cause) {
    super(entityType, "error instantiating entity of type="
        + entityType.getName(), cause);
  }
}

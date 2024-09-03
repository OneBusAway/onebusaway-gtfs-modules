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
 * Extend from {@link Exception} or {@link RuntimeException}? The debate rages
 * on, but I chose to extend from {@link RuntimeException} to maintain
 * compatibility with existing method signatures and because most of the
 * exceptions thrown here are non-recoverable. That is, you typically just log
 * them and exit.
 * 
 * @author bdferris
 */
public abstract class CsvEntityException extends CsvException {

  private static final long serialVersionUID = 1L;

  private final Class<?> _entityType;

  public CsvEntityException(Class<?> entityType, String message) {
    super(message);
    _entityType = entityType;
  }

  public CsvEntityException(Class<?> entityType, String message, Throwable cause) {
    super(message, cause);
    _entityType = entityType;
  }

  public CsvEntityException(Class<?> entityType, Throwable cause) {
    super(cause);
    _entityType = entityType;
  }

  public Class<?> getEntityType() {
    return _entityType;
  }
}

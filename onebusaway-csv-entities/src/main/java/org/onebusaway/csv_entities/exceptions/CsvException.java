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
package org.onebusaway.csv_entities.exceptions;

/**
 * Extend from {@link Exception} or {@link RuntimeException}? The debate rages on, but I chose to
 * extend from {@link RuntimeException} to maintain compatibility with existing method signatures
 * and because most of the exceptions thrown here are non-recoverable. That is, you typically just
 * log them and exit.
 *
 * @author bdferris
 */
public class CsvException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public CsvException(String message) {
    super(message);
  }

  public CsvException(String message, Throwable cause) {
    super(message, cause);
  }

  public CsvException(Throwable cause) {
    super(cause);
  }
}

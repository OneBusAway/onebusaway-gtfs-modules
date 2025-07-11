/**
 * Copyright (C) 2013 Google, Inc.
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
package org.onebusaway.csv_entities;

import java.util.HashMap;
import java.util.Map;
import org.onebusaway.csv_entities.schema.annotations.CsvField;

public class HasExtensionsImpl implements HasExtensions {

  @CsvField(ignore = true)
  private Map<Class<?>, Object> extensions = new HashMap<Class<?>, Object>();

  @Override
  public void putExtension(Class<?> type, Object extension) {
    extensions.put(type, extension);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <X> X getExtension(Class<X> type) {
    return (X) extensions.get(type);
  }
}

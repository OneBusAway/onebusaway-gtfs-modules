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
package org.onebusaway.csv_entities;

import java.util.HashMap;
import java.util.Map;

public class CsvEntityContextImpl implements CsvEntityContext {

  private Map<Object, Object> _params = new HashMap<>();

  public Object get(Object key) {
    return _params.get(key);
  }

  public Object put(Object key, Object value) {
    return _params.put(key, value);
  }
}

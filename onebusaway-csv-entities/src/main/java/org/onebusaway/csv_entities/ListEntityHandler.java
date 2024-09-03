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
package org.onebusaway.csv_entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ListEntityHandler<T> implements EntityHandler, Iterable<T> {

  private List<T> _values = new ArrayList<T>();

  public List<T> getValues() {
    return _values;
  }

  @Override
  public Iterator<T> iterator() {
    return _values.iterator();
  }

  /****
   * {@link EntityHandler} Interface
   ****/

  @SuppressWarnings("unchecked")
  @Override
  public void handleEntity(Object bean) {
    _values.add((T) bean);
  }
}

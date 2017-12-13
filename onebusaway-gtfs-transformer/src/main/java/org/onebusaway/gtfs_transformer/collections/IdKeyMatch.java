/**
 * Copyright (C) 2012 Google, Inc. 
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
package org.onebusaway.gtfs_transformer.collections;

import org.onebusaway.gtfs_transformer.match.EntityMatch;
import org.onebusaway.gtfs_transformer.match.ObjectEquality;

public abstract class IdKeyMatch implements EntityMatch {

  private IdKey _key = null;

  public IdKey getKey() {
    if (_key == null) {
      _key = resolveKey();
    }
    return _key;
  }

  @Override
  public boolean isApplicableToObject(Object object) {
    return ObjectEquality.objectsAreEqual(getKey(), object);
  }

  protected abstract IdKey resolveKey();
}

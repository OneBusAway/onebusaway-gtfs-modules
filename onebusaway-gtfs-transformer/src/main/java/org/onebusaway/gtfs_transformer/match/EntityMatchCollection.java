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
package org.onebusaway.gtfs_transformer.match;

import java.util.List;

public class EntityMatchCollection implements EntityMatch {

  private final List<EntityMatch> _matches;

  public EntityMatchCollection(List<EntityMatch> matches) {
    _matches = matches;
  }

  @Override
  public boolean isApplicableToObject(Object object) {
    for (EntityMatch match : _matches) {
      if (!match.isApplicableToObject(object)) {
        return false;
      }
    }
    return true;
  }
}

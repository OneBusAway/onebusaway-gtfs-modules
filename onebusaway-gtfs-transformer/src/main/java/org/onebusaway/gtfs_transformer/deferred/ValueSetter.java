/**
 * Copyright (C) 2015 Google Inc.
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
package org.onebusaway.gtfs_transformer.deferred;

import org.onebusaway.csv_entities.schema.BeanWrapper;

/** Provides methods for updating the value of a particular Java bean's property. */
public interface ValueSetter {
  /** Updates the specified property of the specified bean as appropriate. */
  void setValue(BeanWrapper bean, String propertyName);
}

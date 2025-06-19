/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.gtfs_transformer.match;

import org.onebusaway.collections.beans.PropertyInvocationResult;
import org.onebusaway.collections.beans.PropertyPathExpression;

public class PropertyValueEntityMatch implements EntityMatch {

  private final PropertyPathExpression _expression;

  private final ValueMatcher _matcher;

  public PropertyValueEntityMatch(PropertyPathExpression expression, ValueMatcher matcher) {
    _expression = expression;
    _matcher = matcher;
  }

  public boolean isApplicableToObject(Object object) {
    PropertyInvocationResult result = _expression.invokeReturningFullResult(object);
    return _matcher.matches(result.parent.getClass(), result.propertyName, result.value);
  }
}

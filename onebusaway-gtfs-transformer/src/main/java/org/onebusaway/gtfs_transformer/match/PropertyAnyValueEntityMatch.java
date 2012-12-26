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

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.collections.beans.PropertyInvocationResult;
import org.onebusaway.collections.beans.PropertyPathCollectionExpression;
import org.onebusaway.gtfs_transformer.impl.DeferredValueMatcher;

public class PropertyAnyValueEntityMatch implements EntityMatch {

  private final PropertyPathCollectionExpression _expression;

  private final DeferredValueMatcher _matcher;

  public PropertyAnyValueEntityMatch(
      PropertyPathCollectionExpression expression, DeferredValueMatcher matcher) {
    _expression = expression;
    _matcher = matcher;
  }

  public boolean isApplicableToObject(Object object) {
    List<PropertyInvocationResult> results = new ArrayList<PropertyInvocationResult>();
    _expression.invokeReturningFullResult(object, results);
    for (PropertyInvocationResult result : results) {
      if (_matcher.matches(result.parent.getClass(), result.propertyName,
          result.value)) {
        return true;
      }
    }
    return false;
  }
}
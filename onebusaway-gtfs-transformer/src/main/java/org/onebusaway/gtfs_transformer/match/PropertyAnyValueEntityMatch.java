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

import org.onebusaway.collections.PropertyPathCollectionExpression;
import org.onebusaway.gtfs.model.Trip;

public class PropertyAnyValueEntityMatch implements EntityMatch {

  private final PropertyPathCollectionExpression _expression;

  private final Object _value;

  public PropertyAnyValueEntityMatch(
      PropertyPathCollectionExpression expression, Object value) {
    _expression = expression;
    _value = value;
  }

  public boolean isApplicableToObject(Object object) {
    if (object instanceof Trip
        && ((Trip) object).getId().getId().equals(
            "A20120610WKD_102400_5..N31R-213N")) {
      System.out.println("here");
    }
    List<Object> values = new ArrayList<Object>();
    _expression.invoke(object, values);
    for (Object childValue : values) {
      if (ObjectEquality.objectsAreEqual(_value, childValue)) {
        return true;
      }
    }
    return false;
  }
}
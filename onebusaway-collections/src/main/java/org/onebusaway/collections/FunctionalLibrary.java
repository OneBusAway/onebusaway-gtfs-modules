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
package org.onebusaway.collections;

import java.util.ArrayList;
import java.util.List;
import org.onebusaway.collections.beans.PropertyPathExpression;

public final class FunctionalLibrary {
  private FunctionalLibrary() {}

  public static <T> List<T> filter(
      Iterable<T> elements, String propertyPathExpression, Object value) {
    List<T> matches = new ArrayList<T>();
    PropertyPathExpression query = new PropertyPathExpression(propertyPathExpression);
    for (T element : elements) {
      Object result = query.invoke(element);
      if ((value == null && result == null) || (value != null && value.equals(result)))
        matches.add(element);
    }
    return matches;
  }

  public static <T> T filterFirst(
      Iterable<T> elements, String propertyPathExpression, Object value) {
    List<T> matches = filter(elements, propertyPathExpression, value);
    return matches.isEmpty() ? null : matches.get(0);
  }
}

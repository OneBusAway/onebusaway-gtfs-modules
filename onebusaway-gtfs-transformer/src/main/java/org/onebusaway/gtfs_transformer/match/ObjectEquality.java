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

import org.apache.commons.beanutils2.ConvertUtils;
import org.apache.commons.beanutils2.Converter;

public class ObjectEquality {
  public static boolean objectsAreEqual(Object expected, Object actual) {
    boolean nullA = expected == null;
    boolean nullB = actual == null;

    if (nullA && nullB)
      return true;
    if (nullA ^ nullB)
      return false;

    Class<?> expectedType = expected.getClass();
    Class<?> actualType = actual.getClass();

    /**
     * Implementation note: This conversion theoretically will happen over and
     * over with the same value. Is there some way to cache it?
     */
    if (!actualType.isAssignableFrom(expectedType)
        && expectedType == String.class) {

      Converter converter = ConvertUtils.lookup(actualType);

      if (converter != null) {
        Object converted = converter.convert(actualType, expected);
        if (converted != null)
          expected = converted;
      }
    }

    return (expected == null && actual == null)
        || (expected != null && expected.equals(actual));
  }
}

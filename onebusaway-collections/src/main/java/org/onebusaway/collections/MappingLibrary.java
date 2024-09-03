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
package org.onebusaway.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.beans.PropertyPathExpression;

/**
 * A number of functional-programming-inspired convenience methods for mapping
 * one set of values to another.
 * 
 * @author bdferris
 * @see PropertyPathExpression
 */
public class MappingLibrary {

  /**
   * Iterate over a collection of values, evaluating a
   * {@link PropertyPathExpression} on each value, and constructing a
   * {@link List} from the expression results.
   * 
   * @param values an iterable collection of values to iterate over
   * @param propertyPathExpression a property path expression to evaluate
   *          against each collection value
   * @return a List composed of the property path expression evaluation results
   */
  @SuppressWarnings("unchecked")
  public static <T1, T2> List<T2> map(Iterable<T1> values,
      String propertyPathExpression) {
    List<T2> mappedValues = new ArrayList<T2>();
    PropertyPathExpression query = new PropertyPathExpression(
        propertyPathExpression);
    for (T1 value : values)
      mappedValues.add((T2) query.invoke(value));
    return mappedValues;
  }

  /**
   * This method is kept for backwards compatibility, and a more concise version
   * can be found in {@link #map(Iterable, String)}
   */
  public static <T1, T2> List<T2> map(Iterable<T1> values,
      String propertyPathExpression, Class<T2> resultType) {
    return map(values, propertyPathExpression);
  }

  /**
   * Construct a {@link Map} from a set of values where the key for each value
   * is the result from the evaluation of a {@link PropertyPathExpression} on
   * each value. If two values in the iterable collection have the same key,
   * subsequent values will overwrite previous values.
   * 
   * @param values an iterable collection of values to iterate over
   * @param propertyPathExpression a property path expression to evaluate
   *          against each collection value
   * @return a map with values from the specified collection and keys from the
   *         property path expression
   */
  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, V> mapToValue(Iterable<V> values,
      String propertyPathExpression) {

    Map<K, V> byKey = new HashMap<K, V>();
    PropertyPathExpression query = new PropertyPathExpression(
        propertyPathExpression);

    for (V value : values) {
      K key = (K) query.invoke(value);
      byKey.put(key, value);
    }

    return byKey;
  }

  /**
   * This method is kept for backwards compatibility, and a more concise version
   * can be found in {@link #mapToValue(Iterable, String)}
   */
  public static <K, V> Map<K, V> mapToValue(Iterable<V> values,
      String property, Class<K> keyType) {
    return mapToValue(values, property);
  }

  /**
   * Construct a {@link Map} from a set of values where the key for each value
   * is the result of the evaluation of a {@link PropertyPathExpression} on each
   * value. Each key maps to a {@link List} of values that all mapped to that
   * same key.
   * 
   * @param values an iterable collection of values to iterate over
   * @param propertyPathExpression a property path expression to evaluate
   *          against each collection value
   * @return a map with values from the specified collection and keys from the
   *         property path expression
   */
  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, List<V>> mapToValueList(Iterable<V> values,
      String property) {
    return mapToValueCollection(values, property, new ArrayList<V>().getClass());
  }

  /**
   * This method is kept for backwards compatibility, and a more concise version
   * can be found in {@link #mapToValueList(Iterable, String)}
   */
  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, List<V>> mapToValueList(Iterable<V> values,
      String property, Class<K> keyType) {
    return mapToValueCollection(values, property, new ArrayList<V>().getClass());
  }

  /**
   * Construct a {@link Map} from a set of values where the key for each value
   * is the result of the evaluation of a {@link PropertyPathExpression} on each
   * value. Each key maps to a {@link Set} of values that all mapped to that
   * same key.
   * 
   * @param values an iterable collection of values to iterate over
   * @param propertyPathExpression a property path expression to evaluate
   *          against each collection value
   * @return a map with values from the specified collection and keys from the
   *         property path expression
   */

  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, Set<V>> mapToValueSet(Iterable<V> values,
      String property) {
    return mapToValueCollection(values, property, new HashSet<V>().getClass());
  }

  /**
   * Construct a {@link Map} from a set of values where the key for each value
   * is the result of the evaluation of a {@link PropertyPathExpression} on each
   * value. Each key maps to a collection of values that all mapped to that same
   * key. The collection type must have a no-arg constructor that can be used to
   * create new collection instances as necessary.
   * 
   * @param values an iterable collection of values to iterate over
   * @param propertyPathExpression a property path expression to evaluate
   *          against each collection value
   * @param collectionType the collection type used to contain mutiple values
   *          that map to the same key
   * @return a map with values from the specified collection and keys from the
   *         property path expression
   */
  @SuppressWarnings("unchecked")
  public static <K, V, C extends Collection<V>, CIMPL extends C> Map<K, C> mapToValueCollection(
      Iterable<V> values, String property, Class<CIMPL> collectionType) {

    Map<K, C> byKey = new HashMap<K, C>();
    PropertyPathExpression query = new PropertyPathExpression(property);

    for (V value : values) {

      K key = (K) query.invoke(value);
      C valuesForKey = byKey.get(key);
      if (valuesForKey == null) {

        try {
          valuesForKey = collectionType.newInstance();
        } catch (Exception ex) {
          throw new IllegalStateException(
              "error instantiating collection type: " + collectionType, ex);
        }

        byKey.put(key, valuesForKey);
      }
      valuesForKey.add(value);
    }

    return byKey;
  }
}

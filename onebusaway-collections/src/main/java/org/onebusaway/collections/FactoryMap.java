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

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * A extension of {@link HashMap} that will automatically create a {@link Map} key-value entry if a
 * call to {@link #get(Object)} is made where the key is not already present in the map.
 *
 * <p>Default map entries can be created by passing in an instance of {@link IValueFactory} as an
 * object factory (see {@link #FactoryMap(IValueFactory)}).
 *
 * <p>Maps can also be created by passing a plain-old Java object. The class of the Java object will
 * be used to create new value instances on demand, as long the class has a no-arg constructor (see
 * {@link #FactoryMap(Object)}).
 *
 * @author bdferris
 */
public class FactoryMap<K, V> extends HashMap<K, V> {

  private static final long serialVersionUID = 1L;

  private IValueFactory<K, V> _valueFactory;

  /**
   * Object factory interface for creating a new value for a specified key for use in {@link
   * FactoryMap}
   *
   * @author bdferris
   */
  public interface IValueFactory<KF, VF> {
    public VF create(KF key);
  }

  /**
   * A convenience method for creating an instance of {@link FactoryMap} that wraps an existing
   * {@link Map} and has a specific default value. The default value's class will be used to create
   * new value instances as long as it has a no-arg constructor.
   *
   * @param map an existing map to wrap
   * @param defaultValue see {@link #FactoryMap(Object)} for discussion
   * @return a {@link Map} with factory-map behavior
   */
  public static <K, V> Map<K, V> create(Map<K, V> map, V defaultValue) {
    return new MapImpl<K, V>(map, new ClassInstanceFactory<K, V>(defaultValue.getClass()));
  }

  /**
   * A convenience method for creating an instance of {@link FactoryMap} that wraps an existing
   * {@link Map} and has a specific default value factory.
   *
   * @param map an existing map to wrap
   * @param factory see {@link #FactoryMap(IValueFactory)} for discussion
   * @return a {@link Map} with factory-map behavior
   */
  public static <K, V> Map<K, V> create(Map<K, V> map, IValueFactory<K, V> factory) {
    return new MapImpl<K, V>(map, factory);
  }

  /**
   * A convenience method for creating an instance of {@link FactoryMap} that wraps an existing
   * {@link SortedMap} and has a specific default value. The default value's class will be used to
   * create new value instances as long as it has a no-arg constructor.
   *
   * @param map an existing sorted map to wrap
   * @param defaultValue see {@link #FactoryMap(Object)} for discussion
   * @return a {@link SortedMap} with factory-map behavior
   */
  public static <K, V> SortedMap<K, V> createSorted(SortedMap<K, V> map, V defaultValue) {
    return new SortedMapImpl<K, V>(map, new ClassInstanceFactory<K, V>(defaultValue.getClass()));
  }

  /**
   * A convenience method for creating an instance of {@link FactoryMap} that wraps an existing
   * {@link SortedMap} and has a specific default value factory.
   *
   * @param map an existing sorted map to wrap
   * @param factory see {@link #FactoryMap(IValueFactory)} for discussion
   * @return a {@link SortedMap} with factory-map behavior
   */
  public static <K, V> SortedMap<K, V> createSorted(
      SortedMap<K, V> map, IValueFactory<K, V> factory) {
    return new SortedMapImpl<K, V>(map, factory);
  }

  /**
   * A factory map constructor that accepts a default value instance. The {@link Class} of the
   * default value instance will be used to create new default value instances as needed assuming
   * the class has no-arg constructor. New values will be created when calls are made to {@link
   * #get(Object)} and the specified key is not already present in the map. Why do we accept an
   * object instance instead of a class instance? It makes it easier to handle cases where V is
   * itself a parameterized type.
   *
   * @param factoryInstance the {@link Class} of the instance will be used to create new values as
   *     needed
   */
  public FactoryMap(V factoryInstance) {
    this(new ClassInstanceFactory<K, V>(factoryInstance.getClass()));
  }

  /**
   * A factory map constructor that accepts a {@link IValueFactory} default value factory. The value
   * factory will be called when calls are made to {@link #get(Object)} and the specified key is not
   * already present in the map.
   *
   * @param valueFactory the default value factory
   */
  public FactoryMap(IValueFactory<K, V> valueFactory) {
    _valueFactory = valueFactory;
  }

  /**
   * Returns the value to which the specified key is mapped, or a default value instance if the
   * specified key is not present in the map. Subsequent clals to {@link #get(Object)} with the same
   * key will return the same value instance.
   *
   * @see Map#get(Object)
   * @see #put(Object, Object)
   */
  @SuppressWarnings("unchecked")
  @Override
  public V get(Object key) {
    if (!containsKey(key)) put((K) key, createValue((K) key));
    return super.get(key);
  }

  private V createValue(K key) {
    return _valueFactory.create(key);
  }

  private static class ClassInstanceFactory<K, V> implements IValueFactory<K, V>, Serializable {

    private static final long serialVersionUID = 1L;

    private Class<? extends V> _valueClass;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ClassInstanceFactory(Class valueClass) {
      _valueClass = valueClass;
    }

    public V create(K key) {
      try {
        return _valueClass.newInstance();
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }
  }

  private static class MapImpl<K, V> implements Map<K, V>, Serializable {

    private static final long serialVersionUID = 1L;

    private Map<K, V> _source;

    private IValueFactory<K, V> _valueFactory;

    public MapImpl(Map<K, V> source, IValueFactory<K, V> valueFactory) {
      _source = source;
      _valueFactory = valueFactory;
    }

    public void clear() {
      _source.clear();
    }

    public boolean containsKey(Object key) {
      return _source.containsKey(key);
    }

    public boolean containsValue(Object value) {
      return _source.containsValue(value);
    }

    public Set<java.util.Map.Entry<K, V>> entrySet() {
      return _source.entrySet();
    }

    @SuppressWarnings("unchecked")
    public V get(Object key) {
      if (!containsKey(key)) _source.put((K) key, createValue((K) key));
      return _source.get(key);
    }

    public boolean isEmpty() {
      return _source.isEmpty();
    }

    public Set<K> keySet() {
      return _source.keySet();
    }

    public V put(K key, V value) {
      return _source.put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> t) {
      _source.putAll(t);
    }

    public V remove(Object key) {
      return _source.remove(key);
    }

    public int size() {
      return _source.size();
    }

    public Collection<V> values() {
      return _source.values();
    }

    private V createValue(K key) {
      return _valueFactory.create(key);
    }
  }

  private static class SortedMapImpl<K, V> extends MapImpl<K, V> implements SortedMap<K, V> {

    private static final long serialVersionUID = 1L;

    private SortedMap<K, V> _source;

    public SortedMapImpl(SortedMap<K, V> source, IValueFactory<K, V> valueFactory) {
      super(source, valueFactory);
      _source = source;
    }

    public Comparator<? super K> comparator() {
      return _source.comparator();
    }

    public K firstKey() {
      return _source.firstKey();
    }

    public SortedMap<K, V> headMap(K toKey) {
      return _source.headMap(toKey);
    }

    public K lastKey() {
      return _source.lastKey();
    }

    public SortedMap<K, V> subMap(K fromKey, K toKey) {
      return _source.subMap(fromKey, toKey);
    }

    public SortedMap<K, V> tailMap(K fromKey) {
      return _source.tailMap(fromKey);
    }
  }
}

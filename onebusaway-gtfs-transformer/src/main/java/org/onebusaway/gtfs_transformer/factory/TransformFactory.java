/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.gtfs_transformer.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.ConvertUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.onebusaway.collections.beans.PropertyPathCollectionExpression;
import org.onebusaway.collections.beans.PropertyPathExpression;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.csv_entities.schema.EntitySchema;
import org.onebusaway.csv_entities.schema.SingleFieldMapping;
import org.onebusaway.gtfs_transformer.GtfsTransformer;
import org.onebusaway.gtfs_transformer.TransformSpecificationException;
import org.onebusaway.gtfs_transformer.TransformSpecificationMissingArgumentException;
import org.onebusaway.gtfs_transformer.collections.ServiceIdKey;
import org.onebusaway.gtfs_transformer.collections.ServiceIdKeyMatch;
import org.onebusaway.gtfs_transformer.collections.ShapeIdKey;
import org.onebusaway.gtfs_transformer.collections.ShapeIdKeyMatch;
import org.onebusaway.gtfs_transformer.impl.DeferredValueMatcher;
import org.onebusaway.gtfs_transformer.impl.DeferredValueSetter;
import org.onebusaway.gtfs_transformer.impl.EntitySchemaCache;
import org.onebusaway.gtfs_transformer.impl.RemoveEntityUpdateStrategy;
import org.onebusaway.gtfs_transformer.impl.ServiceIdTransformStrategyImpl;
import org.onebusaway.gtfs_transformer.impl.SimpleModificationStrategy;
import org.onebusaway.gtfs_transformer.impl.StringModificationStrategy;
import org.onebusaway.gtfs_transformer.match.EntityMatch;
import org.onebusaway.gtfs_transformer.match.EntityMatchCollection;
import org.onebusaway.gtfs_transformer.match.PropertyAnyValueEntityMatch;
import org.onebusaway.gtfs_transformer.match.PropertyValueEntityMatch;
import org.onebusaway.gtfs_transformer.match.TypedEntityMatch;
import org.onebusaway.gtfs_transformer.services.EntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsEntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategyFactory;
import org.onebusaway.gtfs_transformer.updates.CalendarExtensionStrategy;
import org.onebusaway.gtfs_transformer.updates.SubsectionTripTransformStrategy;
import org.onebusaway.gtfs_transformer.updates.SubsectionTripTransformStrategy.SubsectionOperation;
import org.onebusaway.gtfs_transformer.updates.TrimTripTransformStrategy;
import org.onebusaway.gtfs_transformer.updates.TrimTripTransformStrategy.TrimOperation;

public class TransformFactory {

  private static final String ARG_OBJ = "obj";

  private static final String ARG_UPDATE = "update";

  private static final String ARG_SHAPE_ID = "shape_id";

  private static final String ARG_SHAPE = "shape";

  private static final String ARG_SERVICE_ID = "service_id";

  private static final String ARG_CALENDAR = "calendar";

  private static final String ARG_OP = "op";

  private static final String ARG_MATCH = "match";

  private static final String ARG_FILE = "file";

  private static final String ARG_CLASS = "class";

  private static final String ARG_COLLECTION = "collection";

  private static final Set<String> _excludeForObjectSpec = new HashSet<String>(
      Arrays.asList(ARG_CLASS));

  private static final Set<String> _excludeForMatchSpec = new HashSet<String>(
      Arrays.asList(ARG_FILE, ARG_CLASS, ARG_COLLECTION));

  private static Pattern _anyMatcher = Pattern.compile("^any\\((.*)\\)$");

  private final GtfsTransformer _transformer;

  private List<String> _entityPackages = new ArrayList<String>();

  private final EntitySchemaCache _schemaCache = new EntitySchemaCache();

  private final PropertyMethodResolverImpl _propertyMethodResolver;

  public TransformFactory(GtfsTransformer transformer) {
    _transformer = transformer;
    addEntityPackage("org.onebusaway.gtfs.model");
    _schemaCache.addEntitySchemasFromGtfsReader(_transformer.getReader());
    _propertyMethodResolver = new PropertyMethodResolverImpl(
        _transformer.getDao(), _schemaCache);
  }

  public void addEntityPackage(String entityPackage) {
    _entityPackages.add(entityPackage);
  }

  public void addModificationsFromFile(File path) throws IOException,
      TransformSpecificationException {
    BufferedReader reader = new BufferedReader(new FileReader(path));
    addModificationsFromReader(reader);
  }

  public void addModificationsFromString(String value) throws IOException,
      TransformSpecificationException {
    addModificationsFromReader(new BufferedReader(new StringReader(value)));
  }

  public void addModificationsFromUrl(URL url) throws IOException,
      TransformSpecificationException {
    InputStream in = url.openStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    addModificationsFromReader(reader);
  }

  public void addModificationsFromReader(BufferedReader reader)
      throws IOException, TransformSpecificationException {

    String line = null;

    while ((line = reader.readLine()) != null) {

      try {

        line = line.trim();

        if (line.length() == 0 || line.startsWith("#") || line.equals("{{{")
            || line.equals("}}}"))
          continue;

        JSONObject json = new JSONObject(line);

        if (!json.has(ARG_OP)) {
          throw new TransformSpecificationMissingArgumentException(line, ARG_OP);
        }
        String opType = json.getString(ARG_OP);

        if (opType.equals("add")) {
          handleAddOperation(line, json);
        } else if (opType.equals(ARG_UPDATE) || opType.equals("change")
            || opType.equals("modify")) {
          handleUpdateOperation(line, json);
        } else if (opType.equals("remove") || opType.equals("delete")) {
          handleRemoveOperation(line, json);
        } else if (opType.equals("retain")) {
          handleRetainOperation(line, json);
        } else if (opType.equals("subsection")) {
          handleSubsectionOperation(line, json);
        } else if (opType.equals("trim_trip")) {
          handleTrimOperation(line, json);
        } else if (opType.equals("calendar_extension")) {
          handleTransformOperation(line, json, new CalendarExtensionStrategy());
        } else if (opType.equals("transform")) {
          handleTransformOperation(line, json);
        } else {
          throw new TransformSpecificationException("unknown transform op \""
              + opType + "\"", line);
        }

      } catch (JSONException ex) {
        throw new TransformSpecificationException("error parsing json", ex,
            line);
      }
    }
  }

  /****
   * Private Method
   ****/

  private void handleAddOperation(String line, JSONObject json)
      throws JSONException, TransformSpecificationException {

    if (!json.has(ARG_OBJ)) {
      throw new TransformSpecificationMissingArgumentException(line, ARG_OBJ);
    }
    JSONObject objectSpec = json.getJSONObject(ARG_OBJ);
    if (!objectSpec.has(ARG_CLASS)) {
      throw new TransformSpecificationMissingArgumentException(line, ARG_CLASS,
          ARG_OBJ);
    }
    Class<?> entityClass = getEntityTypeForName(objectSpec.getString(ARG_CLASS));
    Map<String, DeferredValueSetter> propertyUpdates = getPropertyValueSettersFromJsonObject(
        entityClass, objectSpec, _excludeForObjectSpec);
    EntitySourceImpl source = new EntitySourceImpl(entityClass, propertyUpdates);
    AddEntitiesTransformStrategy strategy = getStrategy(AddEntitiesTransformStrategy.class);
    strategy.addEntityFactory(source);

  }

  private void handleUpdateOperation(String line, JSONObject json)
      throws JSONException, TransformSpecificationException {

    EntitiesTransformStrategy strategy = getStrategy(EntitiesTransformStrategy.class);

    TypedEntityMatch match = getMatch(line, json);

    if (json.has("factory")) {
      String factoryType = json.getString("factory");
      try {
        Class<?> clazz = Class.forName(factoryType);
        Object factoryObj = clazz.newInstance();
        if (!(factoryObj instanceof EntityTransformStrategy)) {
          throw new TransformSpecificationException(
              "factory object is not an instance of EntityTransformStrategy: "
                  + clazz.getName(), line);
        }
        strategy.addModification(match, (EntityTransformStrategy) factoryObj);
      } catch (Throwable ex) {
        throw new TransformSpecificationException(
            "error creating factory ModificationStrategy instance", ex, line);
      }
      return;
    }

    if (json.has(ARG_UPDATE)) {

      JSONObject update = json.getJSONObject(ARG_UPDATE);

      EntityTransformStrategy mod = getUpdateEntityTransformStrategy(line,
          match, update);

      strategy.addModification(match, mod);
    }

    if (json.has("strings")) {

      JSONObject strings = json.getJSONObject("strings");

      Map<String, Pair<String>> replacements = getEntityPropertiesAndStringReplacementsFromJsonObject(
          match.getType(), strings);
      StringModificationStrategy mod = new StringModificationStrategy(
          replacements);

      strategy.addModification(match, mod);
    }
  }

  private EntityTransformStrategy getUpdateEntityTransformStrategy(String line,
      TypedEntityMatch match, JSONObject update) throws JSONException,
      TransformSpecificationException {
    if (ServiceIdKey.class.isAssignableFrom(match.getType())) {
      String oldServiceId = ((ServiceIdKeyMatch) match.getPropertyMatches()).getRawId();
      if (!update.has(ARG_SERVICE_ID)) {
        throw new TransformSpecificationMissingArgumentException(line,
            ARG_SERVICE_ID, ARG_UPDATE);
      }
      String newServiceId = update.getString(ARG_SERVICE_ID);
      return new ServiceIdTransformStrategyImpl(oldServiceId, newServiceId);
    } else {
      Set<String> emptySet = Collections.emptySet();
      Map<String, DeferredValueSetter> propertyUpdates = getPropertyValueSettersFromJsonObject(
          match.getType(), update, emptySet);
      return new SimpleModificationStrategy(propertyUpdates);
    }
  }

  private void handleRemoveOperation(String line, JSONObject json)
      throws JSONException, TransformSpecificationException {
    TypedEntityMatch match = getMatch(line, json);

    EntitiesTransformStrategy strategy = getStrategy(EntitiesTransformStrategy.class);
    RemoveEntityUpdateStrategy mod = new RemoveEntityUpdateStrategy();
    strategy.addModification(match, mod);
  }

  private void handleRetainOperation(String line, JSONObject json)
      throws JSONException, TransformSpecificationException {

    RetainEntitiesTransformStrategy strategy = getStrategy(RetainEntitiesTransformStrategy.class);

    TypedEntityMatch match = getMatch(line, json);

    boolean retainUp = true;

    if (json.has("retainUp"))
      retainUp = json.getBoolean("retainUp");

    strategy.addRetention(match, retainUp);

    if (json.has("retainBlocks")) {
      boolean retainBlocks = json.getBoolean("retainBlocks");
      strategy.setRetainBlocks(retainBlocks);
    }
  }

  private void handleSubsectionOperation(String line, JSONObject json)
      throws JSONException, TransformSpecificationException {

    SubsectionTripTransformStrategy strategy = getStrategy(SubsectionTripTransformStrategy.class);

    SubsectionOperation operation = new SubsectionTripTransformStrategy.SubsectionOperation();
    setObjectPropertiesFromJson(operation, json);

    if (operation.getFromStopId() == null && operation.getToStopId() == null) {
      throw new TransformSpecificationException(
          "must specify at least fromStopId or toStopId in subsection op", line);
    }

    strategy.addOperation(operation);
  }

  private void handleTrimOperation(String line, JSONObject json)
      throws JSONException {

    TrimTripTransformStrategy strategy = getStrategy(TrimTripTransformStrategy.class);

    TrimOperation operation = new TrimTripTransformStrategy.TrimOperation();
    setObjectPropertiesFromJson(operation, json);

    strategy.addOperation(operation);
  }

  private void handleTransformOperation(String line, JSONObject json)
      throws JSONException, TransformSpecificationException {

    if (!json.has(ARG_CLASS)) {
      throw new TransformSpecificationMissingArgumentException(line, ARG_CLASS);
    }
    String value = json.getString(ARG_CLASS);

    Object factoryObj = null;
    try {
      Class<?> clazz = Class.forName(value);
      factoryObj = clazz.newInstance();
    } catch (Exception ex) {
      throw new TransformSpecificationException("error instantiating class: "
          + value, ex, line);
    }
    handleTransformOperation(line, json, factoryObj);
  }

  private void handleTransformOperation(String line, JSONObject json,
      Object factoryObj) throws JSONException, TransformSpecificationException {

    setObjectPropertiesFromJson(factoryObj, json);

    boolean added = false;

    if (factoryObj instanceof GtfsTransformStrategy) {
      _transformer.addTransform((GtfsTransformStrategy) factoryObj);
      added = true;
    }
    if (factoryObj instanceof GtfsEntityTransformStrategy) {
      _transformer.addEntityTransform((GtfsEntityTransformStrategy) factoryObj);
      added = true;
    }
    if (factoryObj instanceof GtfsTransformStrategyFactory) {
      GtfsTransformStrategyFactory factory = (GtfsTransformStrategyFactory) factoryObj;
      factory.createTransforms(_transformer);
      added = true;
    }

    if (!added) {
      throw new TransformSpecificationException(
          "factory object is not an instance of GtfsTransformStrategy, GtfsEntityTransformStrategy, or GtfsTransformStrategyFactory: "
              + factoryObj.getClass().getName(), line);
    }
  }

  private void setObjectPropertiesFromJson(Object object, JSONObject json)
      throws JSONException {
    BeanWrapper wrapped = BeanWrapperFactory.wrap(object);
    for (Iterator<?> it = json.keys(); it.hasNext();) {
      String key = (String) it.next();
      if (key.equals(ARG_OP) || key.equals(ARG_CLASS))
        continue;
      Object v = json.get(key);
      Class<?> propertyType = wrapped.getPropertyType(key);
      if (v instanceof JSONArray) {
        JSONArray array = (JSONArray) v;
        List<Object> values = new ArrayList<Object>();
        for (int i = 0; i < array.length(); ++i) {
          values.add(array.get(i));
        }
        v = values;
      } else if (v instanceof String && !propertyType.equals(String.class)) {
        v = ConvertUtils.convert((String) v, propertyType);
      }
      wrapped.setPropertyValue(key, v);
    }
  }

  private Class<?> getEntityTypeForName(String name) {

    Class<?> type = getClassForName(name);

    if (type != null)
      return type;

    for (String entityPackage : _entityPackages) {
      type = getClassForName(entityPackage + "." + name);
      if (type != null)
        return type;
    }

    throw new IllegalArgumentException("class not found: " + name);
  }

  private Class<?> getClassForName(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private TypedEntityMatch getMatch(String line, JSONObject json)
      throws JSONException, TransformSpecificationException {

    if (!json.has(ARG_MATCH)) {
      throw new TransformSpecificationMissingArgumentException(line, ARG_MATCH);
    }
    JSONObject match = json.getJSONObject(ARG_MATCH);

    if (match.has(ARG_COLLECTION)) {
      return getCollectionMatch(line, match.getString(ARG_COLLECTION), match);
    }

    Class<?> entityType = null;

    if (match.has(ARG_FILE)) {
      String fileName = match.getString(ARG_FILE);
      EntitySchema schema = _schemaCache.getSchemaForFileName(fileName);
      if (schema == null) {
        throw new TransformSpecificationException("unknown file type: "
            + fileName, line);
      }
      entityType = schema.getEntityClass();
    } else if (match.has(ARG_CLASS)) {
      String entityTypeString = match.getString(ARG_CLASS);
      entityType = getEntityTypeForName(entityTypeString);
    } else {
      throw new TransformSpecificationMissingArgumentException(line,
          new String[] {ARG_FILE, ARG_CLASS}, ARG_MATCH);
    }

    Map<String, DeferredValueMatcher> propertyMatches = getPropertyValueMatchersFromJsonObject(
        match, _excludeForMatchSpec);

    List<EntityMatch> matches = new ArrayList<EntityMatch>();

    for (Map.Entry<String, DeferredValueMatcher> entry : propertyMatches.entrySet()) {
      String property = entry.getKey();
      Matcher m = _anyMatcher.matcher(property);
      if (m.matches()) {
        PropertyPathCollectionExpression expression = new PropertyPathCollectionExpression(
            m.group(1));
        expression.setPropertyMethodResolver(_propertyMethodResolver);
        matches.add(new PropertyAnyValueEntityMatch(expression,
            entry.getValue()));
      } else {
        PropertyPathExpression expression = new PropertyPathExpression(property);
        expression.setPropertyMethodResolver(_propertyMethodResolver);
        matches.add(new PropertyValueEntityMatch(expression, entry.getValue()));
      }
    }

    return new TypedEntityMatch(entityType, new EntityMatchCollection(matches));
  }

  private TypedEntityMatch getCollectionMatch(String line,
      String collectionType, JSONObject match)
      throws TransformSpecificationException, JSONException {
    if (collectionType.equals(ARG_CALENDAR)) {
      if (!match.has(ARG_SERVICE_ID)) {
        throw new TransformSpecificationMissingArgumentException(line,
            ARG_SERVICE_ID, ARG_MATCH);
      }
      String serviceId = match.getString(ARG_SERVICE_ID);
      return new TypedEntityMatch(ServiceIdKey.class, new ServiceIdKeyMatch(
          _transformer.getReader(), serviceId));
    } else if (collectionType.equals(ARG_SHAPE)) {
      if (!match.has(ARG_SHAPE_ID)) {
        throw new TransformSpecificationMissingArgumentException(line,
            ARG_SHAPE_ID, ARG_MATCH);
      }
      String shapeId = match.getString(ARG_SHAPE_ID);
      return new TypedEntityMatch(ShapeIdKey.class, new ShapeIdKeyMatch(
          _transformer.getReader(), shapeId));
    } else {
      throw new TransformSpecificationException("unknown collection type: \""
          + collectionType + "\"", line);
    }
  }

  private Map<String, DeferredValueSetter> getPropertyValueSettersFromJsonObject(
      Class<?> entityType, JSONObject obj, Set<String> propertiesToExclude)
      throws JSONException {
    Map<String, Object> map = getPropertyValuesFromJsonObject(obj,
        propertiesToExclude);
    Map<String, DeferredValueSetter> setters = new HashMap<String, DeferredValueSetter>();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String propertyName = entry.getKey();
      SingleFieldMapping mapping = _schemaCache.getFieldMappingForCsvFieldName(
          entityType, propertyName);
      if (mapping != null) {
        propertyName = mapping.getObjFieldName();
      }
      DeferredValueSetter setter = new DeferredValueSetter(
          _transformer.getReader(), _schemaCache, _transformer.getDao(),
          entry.getValue());
      setters.put(propertyName, setter);
    }
    return setters;
  }

  private Map<String, DeferredValueMatcher> getPropertyValueMatchersFromJsonObject(
      JSONObject obj, Set<String> propertiesToExclude) throws JSONException {
    Map<String, Object> map = getPropertyValuesFromJsonObject(obj,
        propertiesToExclude);
    Map<String, DeferredValueMatcher> matchers = new HashMap<String, DeferredValueMatcher>();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      matchers.put(entry.getKey(),
          new DeferredValueMatcher(_transformer.getReader(), _schemaCache,
              entry.getValue()));
    }
    return matchers;
  }

  private Map<String, Object> getPropertyValuesFromJsonObject(JSONObject obj,
      Set<String> propertiesToExclude) throws JSONException {
    Map<String, Object> map = new HashMap<String, Object>();
    for (@SuppressWarnings("unchecked")
    Iterator<String> it = obj.keys(); it.hasNext();) {
      String property = it.next();
      if (propertiesToExclude.contains(property)) {
        continue;
      }
      Object value = obj.get(property);
      if (property.equals(ARG_CLASS) || property.equals(ARG_FILE)) {
        continue;
      }
      map.put(property, value);
    }
    return map;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Pair<String>> getEntityPropertiesAndStringReplacementsFromJsonObject(
      Class<?> entityType, JSONObject obj) throws JSONException {

    Map<String, Pair<String>> map = new HashMap<String, Pair<String>>();

    for (Iterator<String> it = obj.keys(); it.hasNext();) {

      String property = it.next();
      JSONObject pairs = obj.getJSONObject(property);
      String from = (String) pairs.keys().next();
      String to = pairs.getString(from);
      Pair<String> pair = Tuples.pair(from, to);
      map.put(property, pair);
    }

    return map;
  }

  @SuppressWarnings("unchecked")
  private <T extends GtfsTransformStrategy> T getStrategy(
      Class<T> transformerType) {

    GtfsTransformStrategy lastTransform = _transformer.getLastTransform();

    if (lastTransform != null
        && transformerType.isAssignableFrom(lastTransform.getClass()))
      return (T) lastTransform;

    T strategy = (T) instantiate(transformerType);
    _transformer.addTransform(strategy);
    return strategy;
  }

  private Object instantiate(Class<?> entityClass) {
    try {
      return entityClass.newInstance();
    } catch (Exception ex) {
      throw new IllegalStateException("error instantiating type: "
          + entityClass.getName());
    }
  }

  private class EntitySourceImpl implements
      AddEntitiesTransformStrategy.EntityFactory {

    private final Class<?> _entityType;

    private final Map<String, DeferredValueSetter> _propertySetters;

    public EntitySourceImpl(Class<?> entityType,
        Map<String, DeferredValueSetter> propertySetters) {
      _entityType = entityType;
      _propertySetters = propertySetters;
    }

    @Override
    public Object create() {
      Object instance = instantiate(_entityType);
      BeanWrapper wrapper = BeanWrapperFactory.wrap(instance);
      for (Map.Entry<String, DeferredValueSetter> entry : _propertySetters.entrySet()) {
        String propertyName = entry.getKey();
        DeferredValueSetter setter = entry.getValue();
        setter.setValue(wrapper, propertyName);
      }
      return instance;
    }
  }
}

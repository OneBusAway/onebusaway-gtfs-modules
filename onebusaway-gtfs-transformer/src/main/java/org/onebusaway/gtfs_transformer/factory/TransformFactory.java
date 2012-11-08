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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerFactory;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.onebusaway.collections.PropertyPathCollectionExpression;
import org.onebusaway.collections.PropertyPathExpression;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.csv_entities.schema.EntitySchema;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.csv_entities.schema.SingleFieldMapping;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.mappings.ConverterFactory;
import org.onebusaway.gtfs_transformer.GtfsTransformer;
import org.onebusaway.gtfs_transformer.impl.MatchingEntityModificationStrategyWrapper;
import org.onebusaway.gtfs_transformer.impl.RemoveEntityUpdateStrategy;
import org.onebusaway.gtfs_transformer.impl.SimpleModificationStrategy;
import org.onebusaway.gtfs_transformer.impl.StringModificationStrategy;
import org.onebusaway.gtfs_transformer.match.EntityMatch;
import org.onebusaway.gtfs_transformer.match.EntityMatchCollection;
import org.onebusaway.gtfs_transformer.match.PropertyAnyValueEntityMatch;
import org.onebusaway.gtfs_transformer.match.PropertyMethodResolverImpl;
import org.onebusaway.gtfs_transformer.match.PropertyValueEntityMatch;
import org.onebusaway.gtfs_transformer.match.TypedEntityMatch;
import org.onebusaway.gtfs_transformer.services.EntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsEntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategyFactory;
import org.onebusaway.gtfs_transformer.updates.CalendarExtensionStrategy;
import org.onebusaway.gtfs_transformer.updates.SubsectionTripTransformStrategy;
import org.onebusaway.gtfs_transformer.updates.SubsectionTripTransformStrategy.SubsectionOperation;
import org.onebusaway.gtfs_transformer.updates.TrimTripTransformStrategy.TrimOperation;
import org.onebusaway.gtfs_transformer.updates.TrimTripTransformStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformFactory {

  private static Logger _log = LoggerFactory.getLogger(TransformerFactory.class);

  private static Pattern _anyMatcher = Pattern.compile("^any\\((.*)\\)$");

  static {
    ConvertUtils.register(new ServiceDateConverter(), ServiceDate.class);
  }

  private List<String> _entityPackages = new ArrayList<String>();

  public TransformFactory() {
    addEntityPackage("org.onebusaway.gtfs.model");
  }

  public void addEntityPackage(String entityPackage) {
    _entityPackages.add(entityPackage);
  }

  public void addModificationsFromFile(GtfsTransformer updater, File path)
      throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(path));
    addModificationsFromReader(updater, reader);
  }

  public void addModificationsFromString(GtfsTransformer updater, String value)
      throws IOException {
    addModificationsFromReader(updater, new BufferedReader(new StringReader(
        value)));
  }

  public void addModificationsFromUrl(GtfsTransformer updater, URL url)
      throws IOException {
    InputStream in = url.openStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    addModificationsFromReader(updater, reader);
  }

  public void addModificationsFromReader(GtfsTransformer transformer,
      BufferedReader reader) throws IOException {

    String line = null;

    while ((line = reader.readLine()) != null) {

      try {

        line = line.trim();

        if (line.length() == 0 || line.startsWith("#") || line.equals("{{{")
            || line.equals("}}}"))
          continue;

        JSONObject json = new JSONObject(line);

        String opType = json.getString("op");
        if (opType == null)
          throw new IllegalStateException(
              "must specify an \"op\" argument: line=" + line);

        if (opType.equals("add")) {
          handleAddOperation(transformer, line, json);
        } else if (opType.equals("update") || opType.equals("change")
            || opType.equals("modify")) {
          handleUpdateOperation(transformer, line, json);
        } else if (opType.equals("remove") || opType.equals("delete")) {
          handleRemoveOperation(transformer, line, json);
        } else if (opType.equals("retain")) {
          handleRetainOperation(transformer, line, json);
        } else if (opType.equals("subsection")) {
          handleSubsectionOperation(transformer, line, json);
        } else if (opType.equals("trim_trip")) {
          handleTrimOperation(transformer, line, json);
        } else if (opType.equals("calendar_extension")) {
          handleTransformOperation(transformer, line, json,
              new CalendarExtensionStrategy());
        } else if (opType.equals("transform")) {
          handleTransformOperation(transformer, line, json);
        } else {
          _log.warn("no strategy found for op: " + opType);
        }

      } catch (JSONException ex) {
        throw new IllegalStateException("error parsing json for line=" + line,
            ex);
      }
    }
  }

  /****
   * Private Method
   ****/

  private void handleAddOperation(GtfsTransformer transformer, String line,
      JSONObject json) throws JSONException {

    EntitySourceImpl source = new EntitySourceImpl(transformer, line, json);
    AddEntitiesTransformStrategy strategy = getStrategy(transformer,
        AddEntitiesTransformStrategy.class);
    strategy.addEntityFactory(source);

  }

  private void handleUpdateOperation(GtfsTransformer transformer, String line,
      JSONObject json) throws JSONException {

    ModifyEntitiesTransformStrategy strategy = getStrategy(transformer,
        ModifyEntitiesTransformStrategy.class);

    TypedEntityMatch match = getMatch(transformer, line, json);

    if (json.has("factory")) {
      String value = json.getString("factory");
      try {
        Class<?> clazz = Class.forName(value);
        Object factoryObj = clazz.newInstance();
        if (!(factoryObj instanceof EntityTransformStrategy))
          throw new IllegalArgumentException(
              "factory object is not an instance of EntityTransformStrategy: "
                  + clazz.getName());

        strategy.addModification(
            match.getType(),
            new MatchingEntityModificationStrategyWrapper(
                match.getPropertyMatches(),
                (EntityTransformStrategy) factoryObj));

      } catch (Throwable ex) {
        throw new IllegalStateException(
            "error creating factory ModificationStrategy instance", ex);
      }
      return;
    }

    if (json.has("update")) {

      JSONObject update = json.getJSONObject("update");

      Map<String, Object> propertyUpdates = getEntityPropertiesAndValuesFromJsonObject(
          transformer, match.getType(), update);
      SimpleModificationStrategy mod = new SimpleModificationStrategy(
          match.getPropertyMatches(), propertyUpdates);

      strategy.addModification(match.getType(), mod);
    }

    if (json.has("strings")) {

      JSONObject strings = json.getJSONObject("strings");

      Map<String, Pair<String>> replacements = getEntityPropertiesAndStringReplacementsFromJsonObject(
          match.getType(), strings);
      StringModificationStrategy mod = new StringModificationStrategy(
          match.getPropertyMatches(), replacements);

      strategy.addModification(match.getType(), mod);
    }
  }

  private void handleRemoveOperation(GtfsTransformer transformer, String line,
      JSONObject json) throws JSONException {

    ModifyEntitiesTransformStrategy strategy = getStrategy(transformer,
        ModifyEntitiesTransformStrategy.class);

    TypedEntityMatch match = getMatch(transformer, line, json);
    RemoveEntityUpdateStrategy mod = new RemoveEntityUpdateStrategy(
        match.getPropertyMatches());

    strategy.addModification(match.getType(), mod);
  }

  private void handleRetainOperation(GtfsTransformer transformer, String line,
      JSONObject json) throws JSONException {

    RetainEntitiesTransformStrategy strategy = getStrategy(transformer,
        RetainEntitiesTransformStrategy.class);

    TypedEntityMatch match = getMatch(transformer, line, json);

    boolean retainUp = true;

    if (json.has("retainUp"))
      retainUp = json.getBoolean("retainUp");

    strategy.addRetention(match, retainUp);

    if (json.has("retainBlocks")) {
      boolean retainBlocks = json.getBoolean("retainBlocks");
      strategy.setRetainBlocks(retainBlocks);
    }
  }

  private void handleSubsectionOperation(GtfsTransformer transformer,
      String line, JSONObject json) throws JSONException {

    SubsectionTripTransformStrategy strategy = getStrategy(transformer,
        SubsectionTripTransformStrategy.class);

    SubsectionOperation operation = new SubsectionTripTransformStrategy.SubsectionOperation();
    setObjectPropertiesFromJson(operation, json);

    try {
      strategy.addOperation(operation);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException(
          "invalid subsection specification: line=" + line, ex);
    }
  }

  private void handleTrimOperation(GtfsTransformer transformer, String line,
      JSONObject json) throws JSONException {

    TrimTripTransformStrategy strategy = getStrategy(transformer,
        TrimTripTransformStrategy.class);

    TrimOperation operation = new TrimTripTransformStrategy.TrimOperation();
    setObjectPropertiesFromJson(operation, json);

    try {
      strategy.addOperation(operation);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException(
          "invalid subsection specification: line=" + line, ex);
    }
  }

  private void handleTransformOperation(GtfsTransformer transformer,
      String line, JSONObject json) throws JSONException {

    if (!json.has("class"))
      throw new IllegalArgumentException("transform does not specify a class: "
          + line);

    String value = json.getString("class");

    Object factoryObj = null;
    try {
      Class<?> clazz = Class.forName(value);
      factoryObj = clazz.newInstance();
    } catch (Exception ex) {
      throw new IllegalStateException("error instantiating class: " + value, ex);
    }
    handleTransformOperation(transformer, line, json, factoryObj);
  }

  private void handleTransformOperation(GtfsTransformer transformer,
      String line, JSONObject json, Object factoryObj) throws JSONException {

    setObjectPropertiesFromJson(factoryObj, json);

    boolean added = false;

    if (factoryObj instanceof GtfsTransformStrategy) {
      transformer.addTransform((GtfsTransformStrategy) factoryObj);
      added = true;
    }
    if (factoryObj instanceof GtfsEntityTransformStrategy) {
      transformer.addEntityTransform((GtfsEntityTransformStrategy) factoryObj);
      added = true;
    }
    if (factoryObj instanceof GtfsTransformStrategyFactory) {
      GtfsTransformStrategyFactory factory = (GtfsTransformStrategyFactory) factoryObj;
      factory.createTransforms(transformer);
      added = true;
    }

    if (!added) {
      throw new IllegalArgumentException(
          "factory object is not an instance of GtfsTransformStrategy, GtfsEntityTransformStrategy, or GtfsTransformStrategyFactory: "
              + factoryObj.getClass().getName());
    }
  }

  private void setObjectPropertiesFromJson(Object object, JSONObject json)
      throws JSONException {
    BeanWrapper wrapped = BeanWrapperFactory.wrap(object);
    for (Iterator<?> it = json.keys(); it.hasNext();) {
      String key = (String) it.next();
      if (key.equals("op") || key.equals("class"))
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

  private TypedEntityMatch getMatch(GtfsTransformer transformer, String line,
      JSONObject json) throws JSONException {

    JSONObject match = json.getJSONObject("match");
    if (match == null)
      throw new IllegalArgumentException(
          "modification must have \"match\" argument: line=" + line);

    String entityTypeString = match.getString("class");
    if (entityTypeString == null)
      throw new IllegalArgumentException(
          "modification match must have \"class\" argument: line=" + line);
    Class<?> entityType = getEntityTypeForName(entityTypeString);

    Map<String, Object> propertyMatches = getEntityPropertiesAndValuesFromJsonObject(
        transformer, entityType, match);

    List<EntityMatch> matches = new ArrayList<EntityMatch>();

    PropertyMethodResolverImpl resolver = new PropertyMethodResolverImpl(
        transformer.getDao());

    for (Map.Entry<String, Object> entry : propertyMatches.entrySet()) {
      String property = entry.getKey();
      Matcher m = _anyMatcher.matcher(property);
      if (m.matches()) {
        PropertyPathCollectionExpression expression = new PropertyPathCollectionExpression(
            m.group(1));
        expression.setPropertyMethodResolver(resolver);
        matches.add(new PropertyAnyValueEntityMatch(expression,
            entry.getValue()));
      } else {
        PropertyPathExpression expression = new PropertyPathExpression(property);
        matches.add(new PropertyValueEntityMatch(expression, entry.getValue()));
      }
    }

    return new TypedEntityMatch(entityType, new EntityMatchCollection(matches));
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getEntityPropertiesAndValuesFromJsonObject(
      GtfsTransformer transformer, Class<?> entityType, JSONObject obj)
      throws JSONException {

    Map<String, Object> map = new HashMap<String, Object>();

    for (Iterator<String> it = obj.keys(); it.hasNext();) {

      String property = it.next();
      Object value = obj.get(property);

      if (property.equals("class"))
        continue;

      Class<?> fromType = value.getClass();

      if (fromType.equals(String.class)
          && !_anyMatcher.matcher(property).matches()) {

        PropertyPathExpression exp = new PropertyPathExpression(property);
        Class<?> toType = exp.initialize(entityType);
        Class<?> parentType = exp.getParentType(entityType);
        String lastProperty = exp.getLastProperty();
        Converter converter = getEntitySchemaConverterForTypeAndProperty(
            transformer, parentType, lastProperty, toType);
        if (converter != null) {
          value = converter.convert(toType, value);
        } else if (!toType.isAssignableFrom(fromType)) {
          value = ConvertUtils.convert((String) value, toType);
        }
      }

      map.put(property, value);
    }

    return map;
  }

  private Converter getEntitySchemaConverterForTypeAndProperty(
      GtfsTransformer transformer, Class<?> entityType, String property,
      Class<?> toType) {
    GtfsReader reader = transformer.getReader();
    EntitySchemaFactory schemaFactory = reader.getEntitySchemaFactory();
    EntitySchema schema = schemaFactory.getSchema(entityType);
    if (schema == null) {
      return null;
    }
    for (FieldMapping mapping : schema.getFields()) {
      if (!(mapping instanceof SingleFieldMapping)) {
        continue;
      }
      SingleFieldMapping singleMapping = (SingleFieldMapping) mapping;
      if (!singleMapping.getObjFieldName().equals(property)) {
        continue;
      }
      if (mapping instanceof ConverterFactory) {
        ConverterFactory factory = (ConverterFactory) mapping;
        return factory.create(reader.getContext());
      }
      if (mapping instanceof Converter) {
        return (Converter) mapping;
      }
    }

    return null;
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
      GtfsTransformer transformer, Class<T> transformerType) {

    GtfsTransformStrategy lastTransform = transformer.getLastTransform();

    if (lastTransform != null
        && transformerType.isAssignableFrom(lastTransform.getClass()))
      return (T) lastTransform;

    T strategy = (T) instantiate(transformerType);
    transformer.addTransform(strategy);
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

    private GtfsTransformer _transformer;

    private String _line;

    private JSONObject _json;

    public EntitySourceImpl(GtfsTransformer transformer, String line,
        JSONObject json) {
      _transformer = transformer;
      _line = line;
      _json = json;
    }

    @Override
    public Object create() {
      try {
        JSONObject properties = _json.getJSONObject("obj");

        Class<?> entityClass = getEntityTypeForName(properties.getString("class"));
        Object instance = instantiate(entityClass);

        Map<String, Object> here = getEntityPropertiesAndValuesFromJsonObject(
            _transformer, entityClass, properties);

        BeanWrapper wrapper = BeanWrapperFactory.wrap(instance);
        for (Map.Entry<String, Object> entry : here.entrySet())
          wrapper.setPropertyValue(entry.getKey(), entry.getValue());
        return instance;
      } catch (Exception ex) {
        throw new IllegalStateException(
            "error processing add operation for line=" + _line, ex);
      }
    }
  }
}

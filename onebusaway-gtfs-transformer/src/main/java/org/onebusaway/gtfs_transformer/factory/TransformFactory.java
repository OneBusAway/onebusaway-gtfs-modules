package org.onebusaway.gtfs_transformer.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.csv.schema.BeanWrapperFactory;
import org.onebusaway.gtfs_transformer.GtfsTransformer;
import org.onebusaway.gtfs_transformer.impl.MatchingEntityModificationStrategyWrapper;
import org.onebusaway.gtfs_transformer.impl.RemoveEntityUpdateStrategy;
import org.onebusaway.gtfs_transformer.impl.SimpleModificationStrategy;
import org.onebusaway.gtfs_transformer.impl.StringModificationStrategy;
import org.onebusaway.gtfs_transformer.services.EntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;

public class TransformFactory {

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
        } else if (opType.equals("transform")) {
          handleTransformOperation(transformer, line, json);
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

    JSONObject properties = json.getJSONObject("obj");

    Class<?> entityClass = getEntityTypeForName(properties.getString("class"));
    Object instance = instantiate(entityClass);

    Map<String, Object> here = getEntityPropertiesAndValuesFromJsonObject(
        entityClass, properties);

    BeanWrapper wrapper = BeanWrapperFactory.wrap(instance);
    for (Map.Entry<String, Object> entry : here.entrySet())
      wrapper.setPropertyValue(entry.getKey(), entry.getValue());

    AddEntitiesTransformStrategy strategy = getStrategy(transformer,
        AddEntitiesTransformStrategy.class);
    strategy.addEntity(instance);
  }

  private void handleUpdateOperation(GtfsTransformer transformer, String line,
      JSONObject json) throws JSONException {

    ModifyEntitiesTransformStrategy strategy = getStrategy(transformer,
        ModifyEntitiesTransformStrategy.class);

    EntityMatch match = getMatch(line, json);

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
          match.getType(), update);
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

    EntityMatch match = getMatch(line, json);
    RemoveEntityUpdateStrategy mod = new RemoveEntityUpdateStrategy(
        match.getPropertyMatches());

    strategy.addModification(match.getType(), mod);
  }

  private void handleRetainOperation(GtfsTransformer transformer, String line,
      JSONObject json) throws JSONException {

    RetainEntitiesTransformStrategy strategy = getStrategy(transformer,
        RetainEntitiesTransformStrategy.class);

    EntityMatch match = getMatch(line, json);

    strategy.addRetention(match);
  }

  private void handleTransformOperation(GtfsTransformer transformer,
      String line, JSONObject json) throws JSONException {

    if (!json.has("class"))
      throw new IllegalArgumentException("transform does not specify a class: "
          + line);

    String value = json.getString("class");

    try {

      Class<?> clazz = Class.forName(value);
      Object factoryObj = clazz.newInstance();
      if (!(factoryObj instanceof GtfsTransformStrategy))
        throw new IllegalArgumentException(
            "factory object is not an instance of GtfsTransformStrategy: "
                + clazz.getName());
      BeanWrapper wrapped = BeanWrapperFactory.wrap(factoryObj);
      for (Iterator<?> it = json.keys(); it.hasNext();) {
        String key = (String) it.next();
        if (key.equals("op") || key.equals("class"))
          continue;
        Object v = json.get(key);
        wrapped.setPropertyValue(key, v);
      }

      transformer.addTransform((GtfsTransformStrategy) factoryObj);
    } catch (Exception ex) {
      throw new IllegalStateException("error instantiating class: " + value, ex);
    }
  }

  private Class<?> getEntityTypeForName(String name) {
    Class<?> type = getClassForName(name);
    if (type == null)
      type = getClassForName("org.onebusaway.gtfs.model." + name);
    if (type == null)
      type = getClassForName("org.onebusaway.gtfs_transformer.king_county_metro.model."
          + name);
    if (type == null)
      throw new IllegalArgumentException("class not found: " + name);

    return type;
  }

  private Class<?> getClassForName(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private EntityMatch getMatch(String line, JSONObject json)
      throws JSONException {

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
        entityType, match);

    return new EntityMatch(entityType, propertyMatches);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getEntityPropertiesAndValuesFromJsonObject(
      Class<?> entityType, JSONObject obj) throws JSONException {

    Map<String, Object> map = new HashMap<String, Object>();

    for (Iterator<String> it = obj.keys(); it.hasNext();) {

      String property = it.next();
      Object value = obj.get(property);

      if (property.equals("class"))
        continue;

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
      Pair<String> pair = Tuples.pair(from,to);
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
}

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.onebusaway.collections.beans.PropertyPathCollectionExpression;
import org.onebusaway.collections.beans.PropertyPathExpression;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.CsvEntityContextImpl;
import org.onebusaway.csv_entities.exceptions.MissingRequiredFieldException;
import org.onebusaway.csv_entities.schema.*;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs_transformer.GtfsTransformer;
import org.onebusaway.gtfs_transformer.GtfsTransformerLibrary;
import org.onebusaway.gtfs_transformer.TransformSpecificationException;
import org.onebusaway.gtfs_transformer.TransformSpecificationMissingArgumentException;
import org.onebusaway.gtfs_transformer.collections.ServiceIdKey;
import org.onebusaway.gtfs_transformer.collections.ServiceIdKeyMatch;
import org.onebusaway.gtfs_transformer.collections.ShapeIdKey;
import org.onebusaway.gtfs_transformer.collections.ShapeIdKeyMatch;
import org.onebusaway.gtfs_transformer.deferred.*;
import org.onebusaway.gtfs_transformer.impl.*;
import org.onebusaway.gtfs_transformer.match.*;
import org.onebusaway.gtfs_transformer.services.EntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsEntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategyFactory;
import org.onebusaway.gtfs_transformer.updates.*;
import org.onebusaway.gtfs_transformer.updates.SubsectionTripTransformStrategy.SubsectionOperation;
import org.onebusaway.gtfs_transformer.updates.TrimTripTransformStrategy.TrimOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransformFactory {

  private static Logger _log = LoggerFactory.getLogger(TransformFactory.class);
  
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
      Arrays.asList(ARG_FILE, ARG_CLASS));

  private static final Set<String> _excludeForMatchSpec = new HashSet<String>(
      Arrays.asList(ARG_FILE, ARG_CLASS, ARG_COLLECTION));

  private static Pattern _anyMatcher = Pattern.compile("^any\\((.*)\\)$");
  
  private static Pattern _pathMatcher = Pattern.compile("^path\\((.*)\\)$");
  
  private static Pattern _replaceMatcher = Pattern.compile("^s/(.*)/(.*)/$");

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
        } else if (opType.equals("stop_times_factory")) {
          handleStopTimesOperation(line, json);
        } else if (opType.equals("calendar_extension")) {
          handleTransformOperation(line, json, new CalendarExtensionStrategy());
        } else if (opType.equals("calendar_simplification")) {
          handleTransformOperation(line, json, new CalendarSimplicationStrategy());
        } else if (opType.equals("deduplicate_service_ids")) {
          handleTransformOperation(line, json, new DeduplicateServiceIdsStrategy());
        } else if (opType.equals("shift_negative_stop_times")) {
          handleTransformOperation(line, json, new ShiftNegativeStopTimesUpdateStrategy());
        } else if (opType.equals("shape_direction")) { 
          handleTransformOperation(line, json, new ShapeDirectionTransformStrategy());
        } else if (opType.equals("remove_non_revenue_stops")) {
          handleTransformOperation(line, json, new RemoveNonRevenueStopsStrategy());
        }
        else if (opType.equals("remove_non_revenue_stops_excluding_terminals")) {
          handleTransformOperation(line, json, new RemoveNonRevenueStopsExcludingTerminalsStrategy());
        }
        else if (opType.equals("update_trip_headsign_by_destination")) {
          handleTransformOperation(line, json, new UpdateTripHeadsignByDestinationStrategy());
        }
        else if (opType.equals("update_trip_headsign_exclude_nonreference")) {
          handleTransformOperation(line, json, new UpdateTripHeadsignExcludeNonreference());
        }
        else if (opType.equals("update_trip_headsign_by_reference")) {
          handleTransformOperation(line, json, new UpdateTripHeadsignByReference());
        }
        else if (opType.equals("update_trip_headsign_if_null")) {
          handleTransformOperation(line, json, new UpdateTripHeadsignIfNull());
        }
        else if (opType.equals("merge_stop_names_from_reference")) {
          handleTransformOperation(line, json, new MergeStopNamesFromReferenceStrategy());
        }
        else if (opType.equals("merge_stop_ids_from_reference")) {
          handleTransformOperation(line, json, new MergeStopIdsFromReferenceStrategy());
        }
        else if (opType.equals("update_stop_ids_from_control")) {
          handleTransformOperation(line, json, new UpdateStopIdFromControlStrategy());
        }
        else if (opType.equals("update_wrong_way_concurrencies")) {
          handleTransformOperation(line, json, new UpdateWrongWayConcurrencies());
        }
        else if (opType.equals("update_stop_ids_from_file")) {
          handleTransformOperation(line, json, new UpdateStopIdsFromFile());
        }
        else if (opType.equals("update_stop_ids_from_reference")) {
            handleTransformOperation(line, json, new UpdateStopIdFromReferenceStrategy());
        }
        else if (opType.equals("merge_route_from_reference_by_longname")) {
          handleTransformOperation(line, json, new MergeRouteFromReferenceStrategyByLongName());
        }
        else if (opType.equals("merge_route_from_reference_by_id")) {
          handleTransformOperation(line, json, new MergeRouteFromReferenceStrategyById());
        }
        else if (opType.equals("merge_route_from_reference")) {
          handleTransformOperation(line, json, new MergeRouteFromReferenceStrategy());
        }
        else if (opType.equals("merge_route_five")) {
          handleTransformOperation(line, json, new MergeRouteFive());
        }
        else if (opType.equals("update_calendar_dates_for_dups")) {
          handleTransformOperation(line, json, new UpdateCalendarDatesForDuplicateTrips());
        }
        else if (opType.equals("update_trip_id_by_id")) {
          handleTransformOperation(line, json, new UpdateTripIdById());
        }
        else if (opType.equals("update_stop_id_by_id")) {
          handleTransformOperation(line, json, new UpdateStopIdById());
        }
        else if (opType.equals("update_route_name")) {
          handleTransformOperation(line, json, new UpdateRouteNames());
        }
        else if (opType.equals("validate_gtfs")) {
          handleTransformOperation(line, json, new ValidateGTFS());
        }
        else if (opType.equals("count_and_test")) {
          handleTransformOperation(line, json, new CountAndTest());
        }
        else if (opType.equals("count_and_test_bus")) {
          handleTransformOperation(line, json, new CountAndTestBus());
        }
        else if (opType.equals("count_and_test_subway")) {
          handleTransformOperation(line, json, new CountAndTestSubway());
        }
        else if (opType.equals("verify_route_service")) {
          handleTransformOperation(line, json, new VerifyRouteService());
        }
        else if (opType.equals("verify_bus_service")) {
          handleTransformOperation(line, json, new VerifyBusService());
        }
        else if (opType.equals("update_stoptimes_for_time")) {
          handleTransformOperation(line, json, new UpdateStopTimesForTime());
        }
        else if (opType.equals("update_trips_for_sdon")) {
          handleTransformOperation(line, json, new UpdateTripsForSdon());
        }
        else if (opType.equals("last_stop_to_headsign")){
          handleTransformOperation(line, json, new LastStopToHeadsignStrategy());
        }
        else if (opType.equals("remove_current_service")){
          handleTransformOperation(line, json, new RemoveCurrentService());
        }
        else if (opType.equals("check_for_future_service")){
          handleTransformOperation(line, json, new CheckForFutureService());
        }
        else if (opType.equals("check_for_plausible_stop_times")){
          handleTransformOperation(line,json, new CheckForPlausibleStopTimes());
        }
        else if (opType.equals("check_for_stop_times_without_stops")){
          handleTransformOperation(line,json, new CheckForPlausibleStopTimes());
        }
        else if (opType.equals("anomaly_check_future_trip_counts")){
          handleTransformOperation(line,json, new AnomalyCheckFutureTripCounts());
        }
        else if (opType.equals("verify_future_route_service")){
          handleTransformOperation(line, json, new VerifyFutureRouteService());
        }
        else if (opType.equals("verify_reference_service")){
          handleTransformOperation(line, json, new VerifyReferenceService());
        }
        else if (opType.equals("sanitize_for_api_access")){
          handleTransformOperation(line, json, new SanitizeForApiAccess());
        }
        else if (opType.equals("add_omny_subway_data")) {
          handleTransformOperation(line, json, new AddOmnySubwayData());
        }
        else if (opType.equals("add_omny_lirr_data")) {
          handleTransformOperation(line, json, new AddOmnyLIRRData());
        }
        else if (opType.equals("add_omny_bus_data")) {
          handleTransformOperation(line, json, new AddOmnyBusData());
        }
        else if (opType.equals("verify_route_ids")) {
          handleTransformOperation(line, json, new VerifyRouteIds());
        }
        else if (opType.equals("KCMSuite")){
          String baseUrl = "https://raw.github.com/wiki/camsys/onebusaway-application-modules";

          handleTransformOperation(line, json, new RemoveMergedTripsStrategy());


          handleTransformOperation(line, json, new DeduplicateStopsStrategy());
//          EntitiesTransformStrategy strategy = getStrategy(EntitiesTransformStrategy.class);
//          TypedEntityMatch match = getMatch(line, json);
//          DeduplicateStopsStrategy mod = new DeduplicateStopsStrategy();
//          strategy.addModification(match, mod);

          //GenerateEntitiesTransformStrategy("DeduplicateStopsStrategy");
          //handleTransformOperation(line, json, new DeduplicateStopsStrategy());


          handleTransformOperation(line, json, new DeduplicateRoutesStrategy());
          handleTransformOperation(line, json, new RemoveRepeatedStopTimesStrategy());
          handleTransformOperation(line, json, new RemoveEmptyBlockTripsStrategy());
          handleTransformOperation(line, json, new EnsureStopTimesIncreaseUpdateStrategy());
          handleTransformOperation(line, json, new NoTripsWithBlockIdAndFrequenciesStrategy());


          configureStopNameUpdates(_transformer, baseUrl
                  + "/KingCountyMetroStopNameModifications.mediawiki");


          try {
            GtfsTransformerLibrary.configureTransformation(_transformer, baseUrl
                    + "/KingCountyMetroModifications.mediawiki");
          } catch (TransformSpecificationException e) {
            throw new RuntimeException(e);
          }

          _transformer.addTransform(new LocalVsExpressUpdateStrategy());
        }
        else if (opType.equals("transform")) {
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
   * Private Method *
   ****/

  private void handleAddOperation(String line, JSONObject json)
      throws JSONException, TransformSpecificationException {

    if (!json.has(ARG_OBJ)) {
      throw new TransformSpecificationMissingArgumentException(line, ARG_OBJ);
    }
    JSONObject objectSpec = json.getJSONObject(ARG_OBJ);
    Class<?> entityType = getEntityClassFromJsonSpec(line, objectSpec);
    if (entityType == null) {
      throw new TransformSpecificationMissingArgumentException(line,
          new String[] {ARG_CLASS, ARG_FILE}, ARG_OBJ);
    }
    Map<String, ValueSetter> propertyUpdates = getPropertyValueSettersFromJsonObject(
        entityType, objectSpec, _excludeForObjectSpec);
    EntitySourceImpl source = new EntitySourceImpl(entityType, propertyUpdates);
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
      Map<String, ValueSetter> propertyUpdates = getPropertyValueSettersFromJsonObject(
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
    setObjectPropertiesFromJsonUsingCsvFields(operation, json, line);

    if (operation.getFromStopId() == null && operation.getToStopId() == null) {
      throw new TransformSpecificationException(
          "must specify at least fromStopId or toStopId in subsection op", line);
    }

    strategy.addOperation(operation);
  }

  private void handleTrimOperation(String line, JSONObject json)
      throws JSONException, TransformSpecificationException {

    TypedEntityMatch match = getMatch(line, json);
    if (match.getType() != Trip.class) {
      throw new TransformSpecificationException(
          "the trim_trip op only supports matching against trips", line);
    }

    TrimTripTransformStrategy strategy = getStrategy(TrimTripTransformStrategy.class);

    TrimOperation operation = new TrimTripTransformStrategy.TrimOperation();
    operation.setMatch(match);
    if (json.has("to_stop_id")) {
      operation.setToStopId(json.getString("to_stop_id"));
    }
    if (json.has("from_stop_id")) {
      operation.setFromStopId(json.getString("from_stop_id"));
    }
    if (operation.getToStopId() == null && operation.getFromStopId() == null) {
      throw new TransformSpecificationMissingArgumentException(line,
          new String[] {"to_stop_id", "from_stop_id"});
    }

    strategy.addOperation(operation);
  }

  private void handleStopTimesOperation(String line, JSONObject json)
      throws JSONException, TransformSpecificationException {
    StopTimesFactoryStrategy strategy = new StopTimesFactoryStrategy();
    setObjectPropertiesFromJsonUsingCsvFields(strategy, json, line);
    _transformer.addTransform(strategy);
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

    setObjectPropertiesFromJsonUsingCsvFields(factoryObj, json, line);

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

  private void setObjectPropertiesFromJsonUsingCsvFields(Object object,
      JSONObject json, String line) throws JSONException,
      TransformSpecificationMissingArgumentException {
    EntitySchemaFactory entitySchemaFactory = _transformer.getReader().getEntitySchemaFactory();
    EntitySchema schema = entitySchemaFactory.getSchema(object.getClass());
    BeanWrapper wrapped = BeanWrapperFactory.wrap(object);
    Map<String, Object> values = new HashMap<String, Object>();
    for (Iterator<?> it = json.keys(); it.hasNext();) {
      String key = (String) it.next();
      Object v = json.get(key);
      if (v instanceof JSONArray) {
        JSONArray array = (JSONArray) v;
        List<Object> asList = new ArrayList<Object>();
        for (int i = 0; i < array.length(); ++i) {
          asList.add(array.get(i));
        }
        v = asList;
      }
      values.put(key, v);
    }
    CsvEntityContext context = new CsvEntityContextImpl();
    for (FieldMapping mapping : schema.getFields()) {
      try {
        mapping.translateFromCSVToObject(context, values, wrapped);
      } catch (MissingRequiredFieldException ex) {
        String verboseMessage = "line=" + line + ", context=" + context + ", json="
            + json + ", object=" + object;
        _log.error("missing required field; details:" + verboseMessage);
        throw new TransformSpecificationMissingArgumentException(verboseMessage,
            ex.getFieldName());
      }
    }
  }

  private Class<?> getEntityClassFromJsonSpec(String line, JSONObject objectSpec)
      throws JSONException, TransformSpecificationException,
      TransformSpecificationMissingArgumentException {
    if (objectSpec.has(ARG_FILE)) {
      String fileName = objectSpec.getString(ARG_FILE);
      EntitySchema schema = _schemaCache.getSchemaForFileName(fileName);
      if (schema == null) {
        throw new TransformSpecificationException("unknown file type: "
            + fileName, line);
      }
      return schema.getEntityClass();
    } else if (objectSpec.has(ARG_CLASS)) {
      return getEntityTypeForName(line, objectSpec.getString(ARG_CLASS));
    }
    return null;
  }

  private Class<?> getEntityTypeForName(String line, String name)
      throws TransformSpecificationException {

    Class<?> type = getClassForName(name);

    if (type != null)
      return type;

    for (String entityPackage : _entityPackages) {
      type = getClassForName(entityPackage + "." + name);
      if (type != null)
        return type;
    }

    throw new TransformSpecificationException("unknown class: " + name, line);
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

    Class<?> entityType = getEntityClassFromJsonSpec(line, match);
    if (entityType == null) {
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

  private Map<String, ValueSetter> getPropertyValueSettersFromJsonObject(
      Class<?> entityType, JSONObject obj, Set<String> propertiesToExclude)
      throws JSONException {
    Map<String, Object> map = getPropertyValuesFromJsonObject(obj,
        propertiesToExclude);
    Map<String, ValueSetter> setters = new HashMap<String, ValueSetter>();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String propertyName = entry.getKey();
      SingleFieldMapping mapping = _schemaCache.getFieldMappingForCsvFieldName(
          entityType, propertyName);
      if (mapping != null) {
        propertyName = mapping.getObjFieldName();
      }
      ValueSetter setter = createSetterForValue(entry.getValue());
      setters.put(propertyName, setter);
    }
    return setters;
  }

  private ValueSetter createSetterForValue(Object value) {
    String stringValue = value.toString();
    Matcher pathMatcher = _pathMatcher.matcher(stringValue);
    if (pathMatcher.matches()) {
      PropertyPathExpression expression = new PropertyPathExpression(
          pathMatcher.group(1));
      return new PropertyPathExpressionValueSetter(_transformer.getReader(),
          _schemaCache, _transformer.getDao(), expression);
    }
    Matcher replaceMatcher = _replaceMatcher.matcher(stringValue);
    if (replaceMatcher.matches()) {
      return new ReplaceValueSetter(replaceMatcher.group(1),
          replaceMatcher.group(2));
    }
    return new DeferredValueSetter(_transformer.getReader(), _schemaCache,
        _transformer.getDao(), value);
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

    private final Map<String, ValueSetter> _propertySetters;

    public EntitySourceImpl(Class<?> entityType,
        Map<String, ValueSetter> propertySetters) {
      _entityType = entityType;
      _propertySetters = propertySetters;
    }

    @Override
    public Object create() {
      Object instance = instantiate(_entityType);
      BeanWrapper wrapper = BeanWrapperFactory.wrap(instance);
      for (Map.Entry<String, ValueSetter> entry : _propertySetters.entrySet()) {
        String propertyName = entry.getKey();
        ValueSetter setter = entry.getValue();
        setter.setValue(wrapper, propertyName);
      }
      return instance;
    }
  }












//  private void configureCalendarUpdates(GtfsTransformer transformer, String path) {
//
//    if (path == null)
//      return;
//
//    try {
//      CalendarUpdateStrategy updateStrategy = new CalendarUpdateStrategy();
//
//      TripScheduleModificationFactoryBean factory = new TripScheduleModificationFactoryBean();
//      factory.setPath(path);
//
//      TripScheduleModificationStrategy modification = factory.createModificationStrategy();
//      updateStrategy.addModificationStrategy(modification);
//
//      transformer.addTransform(updateStrategy);
//
//    } catch (IOException ex) {
//      throw new IllegalStateException(ex);
//    }
//  }

  private void configureStopNameUpdates(GtfsTransformer transformer, String path) {

    if (path == null)
      return;

    try {
      StopNameUpdateFactoryStrategy factory = new StopNameUpdateFactoryStrategy();

      if (path.startsWith("http")) {
        GtfsTransformStrategy strategy = factory.createFromUrl(new URL(path));
        transformer.addTransform(strategy);
      } else {
        GtfsTransformStrategy strategy = factory.createFromFile(new File(path));
        transformer.addTransform(strategy);
      }
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }
}

package org.onebusaway.gtfs.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.csv.CsvEntityWriter;
import org.onebusaway.gtfs.csv.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.services.GtfsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GtfsWriter extends CsvEntityWriter {

  private final Logger _log = LoggerFactory.getLogger(GtfsWriter.class);

  public static final String KEY_CONTEXT = GtfsWriter.class.getName()
      + ".context";

  private List<Class<?>> _entityClasses = new ArrayList<Class<?>>();

  private Map<Class<?>, Comparator<?>> _entityComparators = new HashMap<Class<?>, Comparator<?>>();
  
  public GtfsWriter() {

    /**
     * Prep the Entity Schema Factories
     */
    _entityClasses.addAll(GtfsEntitySchemaFactory.getEntityClasses());
    _entityComparators.putAll(GtfsEntitySchemaFactory.getEntityComparators());
    DefaultEntitySchemaFactory schemaFactory = createEntitySchemaFactory();
    setEntitySchemaFactory(schemaFactory);
  }

  public List<Class<?>> getEntityClasses() {
    return _entityClasses;
  }
  
  public Map<Class<?>,Comparator<?>> getEntityComparators() {
    return _entityComparators;
  }

  public void run(GtfsDao dao) throws IOException {

    List<Class<?>> classes = getEntityClasses();

    for (Class<?> entityClass : classes) {
      _log.info("writing entities: " + entityClass.getName());
      Collection<?> entities = dao.getAllEntitiesForType(entityClass);
      entities = sortEntities(entityClass,entities);
      for (Object entity : entities)
        handleEntity(entity);
      flush();
    }

    close();
  }

  @SuppressWarnings("unchecked")
  private Collection<?> sortEntities(Class<?> entityClass, Collection<?> entities) {
    
    Comparator<Object> comparator = (Comparator<Object>) _entityComparators.get(entityClass);
    
    if( comparator == null)
      return entities;
    
    List<Object> sorted = new ArrayList<Object>();
    sorted.addAll(entities);
    Collections.sort(sorted, comparator);
    return sorted;
  }

  /****
   * Protected Methods
   ****/

  protected DefaultEntitySchemaFactory createEntitySchemaFactory() {
    return GtfsEntitySchemaFactory.createEntitySchemaFactory();
  }
}

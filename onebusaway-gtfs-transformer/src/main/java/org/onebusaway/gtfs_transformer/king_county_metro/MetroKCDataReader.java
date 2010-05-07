package org.onebusaway.gtfs_transformer.king_county_metro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.csv.CsvEntityReader;
import org.onebusaway.gtfs.csv.EntityHandler;
import org.onebusaway.gtfs.csv.schema.AnnotationDrivenEntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.impl.GenericDaoImpl;
import org.onebusaway.gtfs.services.GenericMutableDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetroKCDataReader extends CsvEntityReader {

  private static Logger _log = LoggerFactory.getLogger(MetroKCDataReader.class);

  private List<Class<?>> _entityClasses = new ArrayList<Class<?>>();

  private GenericMutableDao _dao = new GenericDaoImpl();

  public MetroKCDataReader() {

    DefaultEntitySchemaFactory schemaFactory = createEntitySchemaFactory();
    setEntitySchemaFactory(schemaFactory);

    addEntityHandler(new EntityHandlerImpl());
  }

  public List<Class<?>> getEntityClasses() {
    return _entityClasses;
  }
  
  public void setDao(GenericMutableDao dao) {
    _dao = dao;
  }
  
  public GenericMutableDao getDao() {
    return _dao;
  }

  public void run() throws IOException {
    List<Class<?>> classes = getEntityClasses();

    for (Class<?> entityClass : classes) {
      _log.info("reading entities: " + entityClass.getName());
      readEntities(entityClass);
    }
  }

  protected DefaultEntitySchemaFactory createEntitySchemaFactory() {

    DefaultEntitySchemaFactory schemaFactory = new DefaultEntitySchemaFactory();

    AnnotationDrivenEntitySchemaFactory entitySchemaFactory = new AnnotationDrivenEntitySchemaFactory();
    entitySchemaFactory.addPackageToScan("org.onebusaway.kcmetro2gtfs.model");
    schemaFactory.addFactory(entitySchemaFactory);

    return schemaFactory;
  }

  private class EntityHandlerImpl implements EntityHandler {

    @Override
    public void handleEntity(Object bean) {
      _dao.saveEntity(bean);
    }
  }
}

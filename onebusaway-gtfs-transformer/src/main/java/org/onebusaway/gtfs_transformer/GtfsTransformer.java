/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google Inc.
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
package org.onebusaway.gtfs_transformer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.impl.GenericMutableDaoWrapper;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsWriter;
import org.onebusaway.gtfs.services.GenericMutableDao;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_transformer.factory.TransformFactory;
import org.onebusaway.gtfs_transformer.services.GtfsEntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.SchemaUpdateStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GtfsTransformer {
  
  private static Logger _log = LoggerFactory.getLogger(GtfsTransformer.class);

  /*****************************************************************************
   * 
   ****************************************************************************/

  private List<File> _gtfsInputDirectories;

  private File _gtfsReferenceDirectory;

  private File _outputDirectory;

  private List<GtfsTransformStrategy> _transformStrategies = new ArrayList<GtfsTransformStrategy>();

  private List<GtfsEntityTransformStrategy> _entityTransformStrategies = new ArrayList<GtfsEntityTransformStrategy>();

  private List<SchemaUpdateStrategy> _outputSchemaUpdates = new ArrayList<SchemaUpdateStrategy>();

  private TransformContext _context = new TransformContext();

  private GtfsReader _reader = new GtfsReader();

  private GtfsReader _referenceReader = new GtfsReader();
  
  private GtfsWriter _writer = new GtfsWriter();

  private GtfsMutableRelationalDao _dao = new GtfsRelationalDaoImpl();

  private String _agencyId;

  private Map<String, Object> _parameters = new HashMap<>();

  private TransformFactory _transformFactory = new TransformFactory(this);

  public void setGtfsInputDirectory(File gtfsInputDirectory) {
    setGtfsInputDirectories(Arrays.asList(gtfsInputDirectory));
  }

  public void setGtfsReferenceDirectory(File referenceDirectory) {
    _gtfsReferenceDirectory = referenceDirectory;
  }

  public void setGtfsInputDirectories(List<File> paths) {
    _gtfsInputDirectories = paths;
  }

  public void setOutputDirectory(File outputDirectory) {
    _outputDirectory = outputDirectory;
  }

  public void addTransform(GtfsTransformStrategy strategy) {
    _transformStrategies.add(strategy);
  }

  public List<GtfsTransformStrategy> getTransforms() {
    return _transformStrategies;
  }

  public GtfsTransformStrategy getLastTransform() {
    if (_transformStrategies.isEmpty())
      return null;
    return _transformStrategies.get(_transformStrategies.size() - 1);
  }

  public void addEntityTransform(GtfsEntityTransformStrategy entityTransform) {
    _entityTransformStrategies.add(entityTransform);
  }

  public void addOutputSchemaUpdate(SchemaUpdateStrategy outputSchemaUpdate) {
    _outputSchemaUpdates.add(outputSchemaUpdate);
  }

  public void addParameter(String key, Object value) {
    _parameters.put(key, value);
  }

  public void setAgencyId(String agencyId) {
    _agencyId = agencyId;
  }

  public GtfsReader getReader() {
    return _reader;
  }

  public GtfsReader getReferenceReader() {
    return _referenceReader;
  }
  
  public GtfsWriter getWriter() {
    return _writer;
  }

  public GtfsRelationalDao getDao() {
    return _dao;
  }

  public TransformFactory getTransformFactory() {
    return _transformFactory;
  }

  public void run() throws Exception {

    if (_outputDirectory != null && !_outputDirectory.exists()
        && !_outputDirectory.getName().endsWith(".zip"))
      _outputDirectory.mkdirs();

    // copy over parameters
    for (String key: _parameters.keySet()) {
      _context.putParameter(key, _parameters.get(key));
    }

    readGtfs();
    if (_gtfsReferenceDirectory != null && _gtfsReferenceDirectory.exists()) {
      readReferenceGtfs();
    } else {
      _log.trace("reference GTFS not found, continuing");
    }

    _context.setDefaultAgencyId(_reader.getDefaultAgencyId());
    _context.setReader(_reader);


    updateGtfs();
    writeGtfs();
  }

  /****
   * Protected Methods
   ****/

  /****
   * Private Methods
   ****/

  private void readGtfs() throws IOException {

    GenericMutableDao dao = _dao;
    if (!_entityTransformStrategies.isEmpty())
      dao = new DaoInterceptor(_dao);

    _reader.setEntityStore(dao);

    if (_agencyId != null)
      _reader.setDefaultAgencyId(_agencyId);

    for (File path : _gtfsInputDirectories) {
      _log.info("reading gtfs from " + path);
      _reader.setInputLocation(path);
      _reader.run();
    }
  }

  private void readReferenceGtfs() throws IOException {
    _log.info("reading reference GTFS at " + _gtfsReferenceDirectory);
    GenericMutableDao dao = new GtfsRelationalDaoImpl();
    _referenceReader.setEntityStore(dao);

    if (_agencyId != null)
      _referenceReader.setDefaultAgencyId(_agencyId);

    _referenceReader.setInputLocation(_gtfsReferenceDirectory);
    _referenceReader.run();
    _context.setReferenceReader(_referenceReader);
  }


  private void updateGtfs() {
    for (GtfsTransformStrategy strategy : _transformStrategies) {
      _log.info("Running strategy {} ....", strategy.getName() );
      strategy.run(_context, _dao);
      _log.info("Strategy {} complete.", strategy.getName());
    }
  }

  private void writeGtfs() throws IOException {
    if (_outputDirectory == null) {
      return;
    }

    _writer.setOutputLocation(_outputDirectory);

    DefaultEntitySchemaFactory schemaFactory = new DefaultEntitySchemaFactory();
    schemaFactory.addFactory(GtfsEntitySchemaFactory.createEntitySchemaFactory());

    for (SchemaUpdateStrategy strategy : _outputSchemaUpdates)
      strategy.updateSchema(schemaFactory);

    _writer.setEntitySchemaFactory(schemaFactory);

    _writer.run(_dao);
  }

  private class DaoInterceptor extends GenericMutableDaoWrapper {

    public DaoInterceptor(GenericMutableDao source) {
      super(source);
    }

    @Override
    public void saveEntity(Object entity) {

      for (GtfsEntityTransformStrategy strategy : _entityTransformStrategies) {
        entity = strategy.transformEntity(_context, _dao, entity);
        if (entity == null)
          return;
      }

      super.saveEntity(entity);
    }
  }
}

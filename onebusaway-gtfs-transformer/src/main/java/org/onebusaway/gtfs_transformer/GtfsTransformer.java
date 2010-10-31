/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.gtfs_transformer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.csv.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsWriter;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.king_county_metro.MetroKCDao;
import org.onebusaway.gtfs_transformer.king_county_metro.MetroKCDaoImpl;
import org.onebusaway.gtfs_transformer.king_county_metro.MetroKCDataReader;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCBlockTrip;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCChangeDate;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCPatternPair;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCStop;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCStopTime;
import org.onebusaway.gtfs_transformer.king_county_metro.model.MetroKCTrip;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.SchemaUpdateStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class GtfsTransformer {

  /*****************************************************************************
   * 
   ****************************************************************************/

  private File _gtfsInputDirectory;

  private File _dataInputDirectory;

  private File _outputDirectory;

  private List<GtfsTransformStrategy> _transformStrategies = new ArrayList<GtfsTransformStrategy>();

  private List<SchemaUpdateStrategy> _outputSchemaUpdates = new ArrayList<SchemaUpdateStrategy>();

  private GtfsReader _reader = new GtfsReader();

  private GtfsMutableRelationalDao _dao = new GtfsRelationalDaoImpl();

  private MetroKCDao _metrokcDao = new MetroKCDaoImpl();

  private String _agencyId;

  public void setGtfsInputDirectory(File gtfsInputDirectory) {
    _gtfsInputDirectory = gtfsInputDirectory;
  }

  public void setDataInputDirectory(File dataInputDirectory) {
    _dataInputDirectory = dataInputDirectory;
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

  public void addOutputSchemaUpdate(SchemaUpdateStrategy outputSchemaUpdate) {
    _outputSchemaUpdates.add(outputSchemaUpdate);
  }

  public void setAgencyId(String agencyId) {
    _agencyId = agencyId;
  }

  public GtfsReader getReader() {
    return _reader;
  }

  public void run() throws Exception {

    if (!_outputDirectory.exists())
      _outputDirectory.mkdirs();

    System.out.println("GTFS Input Directory=" + _gtfsInputDirectory);
    if (_dataInputDirectory != null)
      System.out.println("KCM Data Input Directory=" + _dataInputDirectory);
    System.out.println("Output Directory=" + _outputDirectory);

    readData();
    readGtfs();
    udateGtfs();
    writeGtfs();
  }

  private void readGtfs() throws IOException {

    _reader.setInputLocation(_gtfsInputDirectory);
    _reader.setEntityStore(_dao);

    if (_agencyId != null)
      _reader.setDefaultAgencyId(_agencyId);

    _reader.run();
  }

  private void readData() throws IOException {

    if (_dataInputDirectory == null)
      return;

    MetroKCDataReader reader = new MetroKCDataReader();
    reader.setInputLocation(_dataInputDirectory);
    reader.setDao(_metrokcDao);

    List<Class<?>> entityClasses = reader.getEntityClasses();
    entityClasses.clear();
    entityClasses.add(MetroKCChangeDate.class);
    entityClasses.add(MetroKCStop.class);
    entityClasses.add(MetroKCTrip.class);
    entityClasses.add(MetroKCPatternPair.class);
    entityClasses.add(MetroKCStopTime.class);
    entityClasses.add(MetroKCBlockTrip.class);

    reader.run();
  }

  private void udateGtfs() {
    TransformContext context = new TransformContext();
    if (_agencyId != null)
      context.setDefaultAgencyId(_agencyId);
    context.setMetroKCDao(_metrokcDao);
    for (GtfsTransformStrategy strategy : _transformStrategies)
      strategy.run(context, _dao);
  }

  private void writeGtfs() throws IOException {
    GtfsWriter writer = new GtfsWriter();
    writer.setOutputLocation(_outputDirectory);

    DefaultEntitySchemaFactory schemaFactory = new DefaultEntitySchemaFactory();
    schemaFactory.addFactory(GtfsEntitySchemaFactory.createEntitySchemaFactory());

    for (SchemaUpdateStrategy strategy : _outputSchemaUpdates)
      strategy.updateSchema(schemaFactory);

    writer.setEntitySchemaFactory(schemaFactory);

    writer.run(_dao);
  }

}

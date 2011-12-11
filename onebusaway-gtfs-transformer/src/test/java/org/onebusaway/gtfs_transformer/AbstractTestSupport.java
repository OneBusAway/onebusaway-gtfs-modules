/**
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
package org.onebusaway.gtfs_transformer;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_transformer.factory.TransformFactory;

public class AbstractTestSupport {

  private GtfsTransformer _transformer = new GtfsTransformer();

  protected MockGtfs _gtfs;

  private File _outputPath;

  @Before
  public void before() throws IOException {
    _gtfs = MockGtfs.create();
    _outputPath = File.createTempFile("MockGtfs-", ".zip");
    _outputPath.delete();
    _outputPath.deleteOnExit();

    _transformer.setGtfsInputDirectory(_gtfs.getPath());
    _transformer.setOutputDirectory(_outputPath);
  }

  @After
  public void after() {
    _gtfs.getPath().delete();
    _outputPath.delete();
  }

  protected void addModification(String modification) {
    try {
      TransformFactory factory = _transformer.getTransformFactory();
      factory.addModificationsFromString(_transformer, modification);
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  protected GtfsRelationalDao transform() {
    try {
      _transformer.run();
      GtfsReader reader = new GtfsReader();
      reader.setInputLocation(_outputPath);
      GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
      reader.setEntityStore(dao);
      reader.run();
      return dao;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

}

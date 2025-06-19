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
package org.onebusaway.csv_entities;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileCsvInputSource implements CsvInputSource {

  private ZipFile _zipFile;

  public ZipFileCsvInputSource(ZipFile zipFile) {
    _zipFile = zipFile;
  }

  public boolean hasResource(String name) throws IOException {
    ZipEntry entry = _zipFile.getEntry(name);
    return entry != null;
  }

  public InputStream getResource(String name) throws IOException {
    ZipEntry entry = _zipFile.getEntry(name);
    return _zipFile.getInputStream(entry);
  }

  public void close() throws IOException {
    _zipFile.close();
  }
}

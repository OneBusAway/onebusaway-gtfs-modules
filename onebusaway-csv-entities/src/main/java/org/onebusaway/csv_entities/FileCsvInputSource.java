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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileCsvInputSource implements CsvInputSource {

  private final File _sourceDirectory;

  public FileCsvInputSource(File sourceDirectory) {
    _sourceDirectory = sourceDirectory;
  }

  public boolean hasResource(String name) {
    File file = new File(_sourceDirectory, name);
    return file.exists();
  }

  public InputStream getResource(String name) throws IOException {
    File file = new File(_sourceDirectory, name);
    return new FileInputStream(file);
  }

  public void close() throws IOException {}
}

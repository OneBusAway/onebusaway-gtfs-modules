/**
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.gtfs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class MockGtfs {

  private final File _directory;

  private MockGtfs(File directory) {
    _directory = directory;
  }

  public static MockGtfs create() throws IOException {
    File directory = File.createTempFile("MockGtfs-", "-tmp_directory");
    directory.delete();
    directory.mkdirs();
    return new MockGtfs(directory);
  }

  public File getDirectory() {
    return _directory;
  }

  public void putFile(String fileName, String content) throws IOException {
    File file = new File(_directory, fileName);
    Writer writer = new BufferedWriter(new FileWriter(file));
    writer.write(content);
    writer.close();
  }

  public void putDefaultAgencies() throws IOException {
    putFile("agency.txt", "agency_name,agency_url,agency_timezone\n"
        + "Agency,http://agency.gov/,America/New_York");
  }

  public void delete() {
    recursiveDelete(_directory);
  }

  private void recursiveDelete(File path) {
    if (path.isDirectory()) {
      for (File file : path.listFiles()) {
        recursiveDelete(file);
      }
    }
    path.delete();
  }
}

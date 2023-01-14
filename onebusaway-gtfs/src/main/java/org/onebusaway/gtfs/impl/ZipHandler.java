/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs.impl;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Zip file support.
 */
public class ZipHandler {

  public static final String ZIP_SCHEME = "jar:file:";

  private File zipFile;

  public ZipHandler(File zipFile) {
    this.zipFile = zipFile;
  }

  /**
   * read metadata files from a text file.
   * @param fileInZip
   * @return
   * @throws IOException
   */
  public String readTextFromFile(String fileInZip) throws IOException {
    final StringBuffer content = new StringBuffer();
    FileSystem zipFileSystem = null;
    try {
      URI uri = URI.create(ZIP_SCHEME + zipFile.getAbsolutePath());
      // java 7 introduced native support for zip files
      zipFileSystem = FileSystems.newFileSystem(uri, new HashMap<>());
      Path root = zipFileSystem.getPath(fileInZip);

      Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          content.append(Files.lines(file).collect(Collectors.joining(System.lineSeparator())));
          return FileVisitResult.TERMINATE;
        }
      });
      return content.toString();
    } finally {
      if (zipFileSystem != null) {
        zipFileSystem.close();
      }
    }
  }

  /**
   * write metatdata files to an existing zip file.
   * @param fileInZip
   * @param content
   * @throws IOException
   */
  public void writeTextToFile(String fileInZip, String content) throws IOException {
    FileSystem zipFileSystem = null;
    try {
      URI uri = URI.create(ZIP_SCHEME + zipFile.toURI().getPath());
      // java 7 introduced native support for zip files
      zipFileSystem = FileSystems.newFileSystem(uri, new HashMap<>());
      Path newZipEntry = zipFileSystem.getPath(fileInZip);
      Writer writer = Files.newBufferedWriter(newZipEntry, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
      writer.write(content);
      writer.close();
    } finally {
      if (zipFileSystem != null) {
        zipFileSystem.close();
      }
    }
  }
}

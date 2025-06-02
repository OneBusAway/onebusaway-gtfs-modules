/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.csv_entities.zip;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.onebusaway.csv_entities.CsvInputSource;

public class CommonsZipFileCsvInputSource implements CsvInputSource {

  private ZipFile _zipFile;
  private UrlByteChannelCache _cache;
  private UrlSeekableByteChannel _channel;
  private int _maxBytesPerSecond;
  
  public CommonsZipFileCsvInputSource(URL url, int chunkLength, int maxBytesPerSecond) throws IOException {
    this._cache = new UrlByteChannelCache(url, chunkLength);
    this._maxBytesPerSecond = maxBytesPerSecond;
    
    this._channel = new UrlSeekableByteChannel(64 * 1024, _cache);
    
    this._zipFile = ZipFile.builder().setSeekableByteChannel(_channel).get();
  }

  public boolean hasResource(String name) throws IOException {
    ZipArchiveEntry entry = _zipFile.getEntry(name);
    return entry != null;
  }

  public InputStream getResource(String name) throws IOException {
    ZipArchiveEntry entry = _zipFile.getEntry(name);
    
    long offset = entry.getDataOffset();
    long length = entry.getCompressedSize();
    
    try {
      _channel.transfer((int)offset, (int)length, _maxBytesPerSecond);
    } catch (InterruptedException e) {
      throw new IOException(e);
    }
    
    return _zipFile.getInputStream(entry);
  }

  public void close() throws IOException {
    _zipFile.close();
  }
}

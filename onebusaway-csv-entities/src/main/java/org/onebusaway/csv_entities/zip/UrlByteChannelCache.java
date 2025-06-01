package org.onebusaway.csv_entities.zip;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

import org.apache.commons.io.input.ThrottledInputStream;
import org.apache.commons.io.input.ThrottledInputStream.Builder;

/**
 * Byte-array cache for remote HTTP content. Sees the remote content as a number of segments which are
 * locked and downloaded individually. 
 */

public class UrlByteChannelCache {

  private static class Part {
    private volatile byte[] content;

    public void downloaded(byte[] content) {
      this.content = content;
    }

    public boolean isDownloaded() {
      return content != null;
    }

  }

  protected volatile int size = -1;
  protected URL url;
  protected int chunkLength;
  protected Part[] parts;

  public UrlByteChannelCache(URL url, int chunkLength) {
    this.url = url;
    this.chunkLength = chunkLength;
  }

  protected int getSize() throws IOException {
    URLConnection connection = openConnection();
    if(connection instanceof HttpURLConnection c) {
      c.setRequestMethod("HEAD");

      int responseCode = c.getResponseCode();
      if(responseCode == 200) {
        return c.getContentLength();
      } else {
        throw new IOException("Expected HTTP code 200, got " + responseCode);
      }
    }
    return connection.getContentLength();
  }

  public void ensureContentBytes(int position, int wanted) throws IOException {
    int startIndex = position / chunkLength;
    int endIndex = (position + wanted) / chunkLength;

    ensureContentIndex(startIndex, endIndex);
  }
  
  public boolean hasContentBytes(int position, int wanted) throws IOException {
    int startIndex = position / chunkLength;
    int endIndex = (position + wanted) / chunkLength;

    for(int i = startIndex; i <= endIndex; i++) {
      if(!parts[i].isDownloaded()) {
        return false;
      }
    }
    
    return true;
  }

  public void ensureContentIndex(int startIndex, int endIndex) throws IOException {

    int length = endIndex - startIndex + 1;

    if(length == 1 && parts[startIndex].isDownloaded()) { // optimization for most common cause
      return;
    }

    int currentStartIndex = startIndex;

    do {
      // greedy, request multiple parts per request
      // find start
      while(currentStartIndex < startIndex + length && parts[currentStartIndex].isDownloaded()) {
        currentStartIndex++;
      }

      if(currentStartIndex == startIndex + length) {
        break;
      }

      // find end
      int currentLength = 1;
      while(currentStartIndex + currentLength < startIndex + length && !parts[currentStartIndex + currentLength].isDownloaded()) {
        currentLength++;
      }

      if(currentLength == 0) {
        break;
      }

      InputStream inputStream = openInputStream((currentStartIndex * chunkLength), Math.min(size, (currentStartIndex + currentLength) * chunkLength) - 1);

      // directly create output byte arrays on-the-go
      byte[] buffer = new byte[16 * 1024];

      for(int i = currentStartIndex; i < currentStartIndex + currentLength; i++) {
        byte[] partContent = new byte[Math.min(chunkLength, size - currentStartIndex * chunkLength)];

        int index = 0;

        int read;
        do {
          read = inputStream.read(buffer, 0, Math.min(partContent.length - index, buffer.length));
          if(read == -1) {
            break;
          }

          System.arraycopy(buffer, 0, partContent, index, read);

          index += read;
        } while(index < partContent.length);

        parts[i].downloaded(partContent);
      }

      currentStartIndex = currentStartIndex + currentLength;
    } while(currentStartIndex < startIndex + length);

  }

  protected URLConnection openConnection() throws IOException {
    return url.openConnection();
  }

  protected InputStream openInputStream(int start, int end, int maxBytesPerSecond) throws IOException {
    InputStream is = openInputStream(start, end);
    if(maxBytesPerSecond == -1) {
      return is;
    }
    Builder builder = ThrottledInputStream.builder();
    builder.setInputStream(is);
    builder.setMaxBytesPerSecond(maxBytesPerSecond);
    return builder.get();
  }

  protected InputStream openInputStream(int start, int end) throws IOException {
    URLConnection connection = url.openConnection();
    if(connection instanceof HttpURLConnection c) {

      c.setRequestProperty("Range", "bytes=" + start +"-" + end);
      int responseCode = c.getResponseCode();
      if(responseCode == 200 || responseCode == 206) {
        return c.getInputStream();		
      } else {
        throw new IOException("Expected HTTP code 200, got " + responseCode);
      } 
    }
    InputStream inputStream = connection.getInputStream();
    if(inputStream instanceof FileInputStream fin) {
      fin.getChannel().position(start);
    } else {
      inputStream.skip(start);
    }
    return inputStream;
  }

  public int size() throws IOException {
    if(size == -1) {
      synchronized(this) {
        if(size == -1) {
          size = getSize();

          int parts = size / chunkLength;
          if(size % chunkLength != 0) {
            parts++;
          }

          this.parts = new Part[parts];
          for(int i = 0; i < parts; i++) {
            this.parts[i] = new Part();
          }
        }
      }
    }

    return size;
  }

  public int put(ByteBuffer buf, int position, int wanted) throws IOException {
    int startIndex = position / chunkLength;
    int endIndex = (position + wanted) / chunkLength;

    ensureContentIndex(startIndex, endIndex);

    int offest = position - startIndex * chunkLength;
    int length = Math.min(wanted, parts[startIndex].content.length - offest);

    buf.put(parts[startIndex].content, offest, length);

    return length;
  }

}

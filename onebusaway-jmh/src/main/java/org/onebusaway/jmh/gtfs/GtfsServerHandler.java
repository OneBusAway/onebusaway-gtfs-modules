package org.onebusaway.jmh.gtfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.input.ThrottledInputStream;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GtfsServerHandler implements HttpHandler {

  private final File file;
  private int bandwithInMegaBytesPerSecond;

  public GtfsServerHandler(File file, int bandwithInMegaBytesPerSecond) {
    this.file = file;
    this.bandwithInMegaBytesPerSecond = bandwithInMegaBytesPerSecond;
  }
  
  public void setBandwithInMegaBytesPerSecond(int bandwithInMegaBytesPerSecond) {
    this.bandwithInMegaBytesPerSecond = bandwithInMegaBytesPerSecond;
  }

  @Override
  public void handle(HttpExchange t) throws IOException {
    String requestMethod = t.getRequestMethod();
    boolean skipBody = requestMethod.equals("HEAD");

    Headers requestHeaders = t.getRequestHeaders();
    String range = requestHeaders.getFirst("Range");
    if(range == null) {
      Headers responseHeaders = t.getResponseHeaders();
      responseHeaders.set("Content-Length", Long.toString(file.length()));
      if(skipBody) {
        t.sendResponseHeaders(200, -1);
        t.close();
        return;
      }
      t.sendResponseHeaders(200, file.length());

      System.out.println("HTTP " + requestMethod + " whole file");
      
      OutputStream os = t.getResponseBody();
      InputStream in = new FileInputStream(file);
      try {
        if(bandwithInMegaBytesPerSecond != -1) {
          ThrottledInputStream.Builder builder = ThrottledInputStream.builder();
          builder.setInputStream(in);
          builder.setMaxBytesPerSecond(bandwithInMegaBytesPerSecond * 1000_000);
          in = builder.get();
        }
        
        byte[] buffer = new byte[16 * 1024];

        while(true) {
          int count = in.read(buffer);
          if(count <= 0) {
            break;
          }
          os.write(buffer, 0, count);
        }
      } finally {
        os.close();
        in.close();
      }
    } else {

      int equals = range.indexOf('=');
      int dash = range.indexOf('-');

      int start = Integer.parseInt(range.substring(equals + 1, dash)); // inclusive
      int end = Integer.parseInt(range.substring(dash + 1)) + 1; // exclusive

      int length = end - start;
      System.out.println("HTTP " + requestMethod + " " + start + "-" + (end -1));
      Headers responseHeaders = t.getResponseHeaders();
      responseHeaders.set("Content-Length", Long.toString(file.length()));
      if(skipBody) {
        t.sendResponseHeaders(200, -1);
        t.close();
        return;
      }
      t.sendResponseHeaders(200, length);

      OutputStream os = t.getResponseBody();
      try { 
        FileInputStream fin = new FileInputStream(file);
        fin.getChannel().position(start);

        InputStream in = fin;
        if(bandwithInMegaBytesPerSecond != -1) {
          ThrottledInputStream.Builder builder = ThrottledInputStream.builder();
          builder.setInputStream(fin);
          builder.setMaxBytesPerSecond(bandwithInMegaBytesPerSecond * 1000_000);
          in = builder.get();
        }          
        
        byte[] buffer = new byte[16 * 1024];

        while(length > 0) {
          int count = in.read(buffer, 0, Math.min(buffer.length, length));
          if(count <= 0) {
            break;
          }
          os.write(buffer, 0, count);

          length -= count;
        }
      } finally {
        os.close();
      }
    }
  }
}
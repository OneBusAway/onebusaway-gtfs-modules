package org.onebusaway.csv_entities.zip;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class UrlSeekableByteChannel implements SeekableByteChannel {

  private final AtomicBoolean closed = new AtomicBoolean();
  private int position;

  private UrlByteChannelCache cache;

  protected int chunkLength;

  protected byte[] zipArchiveEntryBuffer = new byte[] {};
  protected int zipArchiveEntryPosition = 0;
  protected AtomicInteger zipArchiveEntryChunks = new AtomicInteger();
  protected Thread zipArchiveEntryReaderThread;

  public UrlSeekableByteChannel(UrlByteChannelCache cache) {
    this(1024 * 1024, cache);
  }

  public UrlSeekableByteChannel(int chunkLength, UrlByteChannelCache cache) {
    this.chunkLength = chunkLength;
    this.cache = cache;
  }

  public final void readFully(InputStream in, byte[] b, int off, int len) throws IOException {
    Objects.checkFromIndexSize(off, len, b.length);
    int n = 0;
    while (n < len) {
      int count = in.read(b, off + n, len - n);
      if (count < 0)
        throw new EOFException();
      n += count;
    }
  }

  @Override
  public SeekableByteChannel position(long newPosition) throws IOException {
    ensureOpen();
    if (newPosition < 0L || newPosition > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Position has to be in range 0.. " + Integer.MAX_VALUE);
    }
    position = (int) newPosition;
    return this;
  }

  @Override
  public long position() {
    return position;
  }

  @Override
  public SeekableByteChannel truncate(long newSize) {
    throw new RuntimeException();
  }

  @Override
  public int read(ByteBuffer buf) throws IOException {

    ensureOpen();

    if(zipArchiveEntryPosition <= position && position < zipArchiveEntryPosition + zipArchiveEntryBuffer.length) {
      // within current buffer
      int wanted = buf.remaining();

      int possible;
      synchronized(this) {
        int limit = zipArchiveEntryPosition + chunkLength * zipArchiveEntryChunks.get();

        possible = limit - position;
        if(possible <= 0) {
          try {
            this.wait();
          } catch (InterruptedException e) {
            throw new IOException("Unable to read " + position + " -> " + wanted, e);
          }
          limit = zipArchiveEntryPosition + chunkLength * zipArchiveEntryChunks.get();

          possible = limit - position;
          if(possible <= 0) {
            throw new IOException("Unable to read " + position + " -> " + wanted);
          }
        }
      }

      int read = Math.min(wanted, possible);
      buf.put(zipArchiveEntryBuffer, position - zipArchiveEntryPosition, read);
      
      position += read;
      
      return read;

    } else {
      // random access from underlying cache
      // typically for reading headers / manifest
      
      int wanted = buf.remaining();
      int possible = cache.size() - position;
      if (possible <= 0) {
        return -1;
      }
      if (wanted > possible) {
        wanted = possible;
      }

      int read = cache.put(buf, position, wanted);

      position += read;
      return read;
    }
  }


  @Override
  public void close() {
    closed.set(true);
  }

  @Override
  public boolean isOpen() {
    return !closed.get();
  }

  @Override
  public int write(ByteBuffer b) throws IOException {
    throw new RuntimeException();
  }

  private void ensureOpen() throws ClosedChannelException {
    if (!isOpen()) {
      throw new ClosedChannelException();
    }
  }    

  @Override
  public long size() throws IOException {
    return cache.getSize();
  }

  public void transfer(int offset, int length, int maxBytesPerSecond) throws IOException, InterruptedException {
    if(zipArchiveEntryReaderThread != null) {
      zipArchiveEntryReaderThread.join();
    }

    if(cache.hasContentBytes(offset, length)) {
      System.out.println("Already have " + offset + " " + length);
      
      this.zipArchiveEntryBuffer = new byte[] {};
      return;
    }
    
    this.zipArchiveEntryChunks.set(0);
    this.zipArchiveEntryPosition = offset;
    this.zipArchiveEntryBuffer = new byte[length];

    zipArchiveEntryReaderThread = new Thread() {
      @Override
      public void run() {
        int chunks = 0;

        int len = length;
        int off = 0;
        try {
          System.out.println("Transfer whole file: " + offset + " -> " + length);
          try (InputStream is = cache.openInputStream(offset, offset + length - 1, maxBytesPerSecond)) {
            int n = 0;
            while (n < len) {
              int count = is.read(zipArchiveEntryBuffer, off + n, len - n);
              if (count < 0) {
                throw new EOFException();
              }
              n += count;

              if(chunks < n / chunkLength) {
                chunks = n / chunkLength;
                
                synchronized (UrlSeekableByteChannel.this) {
                  zipArchiveEntryChunks.set(chunks);
                  UrlSeekableByteChannel.this.notify();
                }
              }
            }
          }

          synchronized (UrlSeekableByteChannel.this) {
            zipArchiveEntryChunks.set(length / chunkLength + (length % chunkLength == 0 ? 0 : 1) );
            UrlSeekableByteChannel.this.notify();
          }

          System.out.println("Transferred " + offset + " " + length);
        } catch(Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
    zipArchiveEntryReaderThread.start();
  }
}

package org.onebusaway.jmh.gtfs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.input.ThrottledInputStream;
import org.onebusaway.csv_entities.zip.CommonsZipFileCsvInputSource;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GenericMutableDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Timeout;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.sun.net.httpserver.HttpServer;

@Fork(1)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.SingleShotTime)
@Warmup(time=10, timeUnit=TimeUnit.SECONDS, iterations=0)
@Measurement(time=200, timeUnit=TimeUnit.SECONDS, iterations=1)
@Timeout(timeUnit=TimeUnit.SECONDS, time=300)
public class GtfsDownloadParseBenchmark {

  @Param({"5", "10", "15", "20", "25", "30", "40", "50", "60", "80", "100", "125", "150", "200", "500"})
  public int megabytesPerSecond;

  @State(Scope.Benchmark)
  public static class ThreadState {

    private URL url;
    private GtfsServerHandler handler;
    private HttpServer server;

    public ThreadState()  {
      handler = new GtfsServerHandler(new File("./src/test/resources/rb_norway-aggregated-gtfs.zip"), -1);
    }

    @Setup(Level.Trial)
    public void setup() throws IOException {
      url = new URL("http://127.0.0.1:8000/gtfs.zip");
      
      server = HttpServer.create(new InetSocketAddress(8000), 0);
      server.createContext("/gtfs.zip", handler);
      server.setExecutor(null); // creates a default executor

      server.start();
    }

    @TearDown(Level.Trial)
    public void teardown(){
      server.stop(1);
    }
    
    public void setBandwithInMegaBytesPerSecond(int bandwithInMegaBytesPerSecond) {
      handler.setBandwithInMegaBytesPerSecond(bandwithInMegaBytesPerSecond);;
    }
  }


  @Benchmark
  public GtfsRelationalDao testTransferThenParse(ThreadState state) throws Exception {
    state.setBandwithInMegaBytesPerSecond(megabytesPerSecond);
    
    File file = File.createTempFile("gtfs-", ".zip");
    file.deleteOnExit();

    InputStream in = state.url.openStream();
    FileOutputStream fileOutputStream = new FileOutputStream(file);
    try {
      byte dataBuffer[] = new byte[16 * 1024];
      int bytesRead;
      while ((bytesRead = in.read(dataBuffer, 0, dataBuffer.length)) != -1) {
        fileOutputStream.write(dataBuffer, 0, bytesRead);
      }
      fileOutputStream.close();
      
      GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
      readGtfs(dao, file, "ENTUR");
      return dao;
    } finally {
      in.close();
      
      file.delete();
    }

  }

  @Benchmark
  public GtfsRelationalDao testParseDirectlyFromURL(ThreadState state) throws Exception {
    state.setBandwithInMegaBytesPerSecond(megabytesPerSecond);
    
    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    readGtfs(dao, state.url, "ENTUR", -1);
    return dao;
  }

  public static <T extends GenericMutableDao> void readGtfs(T entityStore,
      File resourcePath, String defaultAgencyId) throws IOException {

    GtfsReader reader = new GtfsReader();
    reader.setDefaultAgencyId(defaultAgencyId);

    reader.setInputLocation(resourcePath);

    reader.setEntityStore(entityStore);

    reader.run();
  }

  public static <T extends GenericMutableDao> void readGtfs(T entityStore,
      URL resourceUrl, String defaultAgencyId, int maxBytesPerSecond) throws IOException {

    GtfsReader reader = new GtfsReader();
    reader.setDefaultAgencyId(defaultAgencyId);

    reader.setInputSource(new CommonsZipFileCsvInputSource(resourceUrl, 1024 * 1024, maxBytesPerSecond));

    reader.setEntityStore(entityStore);

    reader.run();
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(GtfsDownloadParseBenchmark.class.getSimpleName())
        .result("jmh-result-" + Instant.now().toString() + ".json")
        .resultFormat(ResultFormatType.JSON)
        .build();
    new Runner(opt).run();
  }
}

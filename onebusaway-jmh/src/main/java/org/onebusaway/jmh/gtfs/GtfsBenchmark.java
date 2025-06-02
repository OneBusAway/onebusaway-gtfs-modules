package org.onebusaway.jmh.gtfs;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.onebusaway.csv_entities.zip.CommonsZipFileCsvInputSource;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Timeout;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(time=10, timeUnit=TimeUnit.SECONDS, iterations=0)
@Measurement(time=10, timeUnit=TimeUnit.SECONDS, iterations=1)
@Timeout(timeUnit=TimeUnit.SECONDS, time=10)
public class GtfsBenchmark {

  @State(Scope.Thread)
  public static class ThreadState {
    GtfsRelationalDaoImpl entityStore = new GtfsRelationalDaoImpl();
    GtfsReader reader = new GtfsReader();
    public ThreadState() {
      entityStore.setGenerateIds(true);
      
      reader.setEntityStore(entityStore);
      reader.setOverwriteDuplicates(true);
    }
  }

  @Benchmark
  public GtfsRelationalDao testParse(ThreadState state) throws Exception {
    return processFeedFromFile(new File("./src/main/resources/island-transit_20090312_0314"), "abcd", false, state.entityStore, state.reader);
  }
  
  @Benchmark
  public GtfsRelationalDao testParseURL(ThreadState state) throws Exception {
    return processFeedFromURL(new File("./src/main/resources/island-transit_20090312_0314").toURL(), "abcd", false, state.entityStore, state.reader, 10);
  }

  public static GtfsRelationalDao processFeedFromFile(
      File resourcePath, String agencyId,
      boolean internStrings, GtfsRelationalDaoImpl entityStore, GtfsReader reader
      ) throws IOException {

    reader.setDefaultAgencyId(agencyId);
    reader.setInternStrings(internStrings);

    reader.setInputLocation(resourcePath);
    
    try {
      reader.run();
      return entityStore;
    } finally {
      entityStore.clearAllCaches();
    }
  }

  public static GtfsRelationalDao processFeedFromURL(
      URL url, String agencyId,
      boolean internStrings, GtfsRelationalDaoImpl entityStore, GtfsReader reader, int maxBytesPerSecond
      ) throws IOException {

    reader.setDefaultAgencyId(agencyId);
    reader.setInternStrings(internStrings);
    
    reader.setInputSource(new CommonsZipFileCsvInputSource(url, 1024 * 1024, maxBytesPerSecond));
    
    try {
      reader.run();
      return entityStore;
    } finally {
      entityStore.clearAllCaches();
    }
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(GtfsBenchmark.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}

package org.onebusaway.jmh.gtfs;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
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

@Fork(10)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.SingleShotTime)
@Measurement(time=10, timeUnit=TimeUnit.SECONDS, iterations=1)
@Timeout(timeUnit=TimeUnit.SECONDS, time=1000)
public class GtfsSingleShotBenchmark {

  // note: generally simpler to benchmark larger gtfs feeds for this signle shot benchmark 
  private static final String directory = "./src/main/resources/island-transit_20090312_0314";
  
  @State(Scope.Thread)
  public static class ThreadState {
    GtfsReader reader = new GtfsReader();
    public ThreadState() {
    }
  }

  @Benchmark
  public GtfsRelationalDao testParse(ThreadState state) throws Exception {
    return processFeed(new File(directory), "abcd", false, state.reader);
  }

  public static GtfsRelationalDao processFeed(
      File resourcePath, String agencyId,
      boolean internStrings, GtfsReader reader
      ) throws IOException {

    GtfsRelationalDaoImpl entityStore = new GtfsRelationalDaoImpl();
    entityStore.setGenerateIds(true);

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

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(GtfsSingleShotBenchmark.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}

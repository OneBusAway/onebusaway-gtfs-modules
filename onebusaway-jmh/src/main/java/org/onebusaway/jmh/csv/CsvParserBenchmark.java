package org.onebusaway.jmh.csv;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.onebusaway.csv_entities.CSVLibrary;
import org.onebusaway.csv_entities.CSVListener;
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
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(time = 3, timeUnit = TimeUnit.SECONDS, iterations = 1)
@Measurement(time = 3, timeUnit = TimeUnit.SECONDS, iterations = 1)
@Timeout(timeUnit = TimeUnit.SECONDS, time = 10)
public class CsvParserBenchmark {

  @State(Scope.Thread)
  public static class ThreadState {
    private CSVLibrary csvLibrary = new CSVLibrary();

    private byte[] stopTimes;
    private byte[] trips;

    public ThreadState() {
      try {
        stopTimes = IOUtils.resourceToByteArray("/brown-county-flex/stop_times.txt");
        trips = IOUtils.resourceToByteArray("/turlock-fares-v2/trips.txt");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Benchmark
  public int testParseStopTimes(ThreadState state) throws Exception {
    int count = 0;
    for (int i = 0; i < 1; i++) {
      state.csvLibrary.parse(
          new ByteArrayInputStream(state.stopTimes),
          (CSVListener)
              line -> {
                if (line == null || line.isEmpty()) {
                  throw new RuntimeException();
                }
              });
      count++;
    }
    return count;
  }

  @Benchmark
  public int testParseTrips(ThreadState state) throws Exception {
    int count = 0;
    for (int i = 0; i < 1; i++) {
      state.csvLibrary.parse(
          new ByteArrayInputStream(state.trips),
          (CSVListener)
              line -> {
                if (line == null || line.isEmpty()) {
                  throw new RuntimeException();
                }
              });
      count++;
    }
    return count;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(CsvParserBenchmark.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}

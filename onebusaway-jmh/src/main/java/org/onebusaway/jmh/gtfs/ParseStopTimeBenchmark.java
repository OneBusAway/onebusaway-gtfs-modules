package org.onebusaway.jmh.gtfs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.mappings.StopTimeFieldMappingFactory;
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
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@Warmup(time=5, timeUnit=TimeUnit.SECONDS, iterations=1)
@Measurement(time=5, timeUnit=TimeUnit.SECONDS, iterations=1)
@Timeout(timeUnit=TimeUnit.SECONDS, time=5)
public class ParseStopTimeBenchmark {

  @State(Scope.Thread)
  public static class ThreadState {
    List<String> time = new ArrayList<>();
    public ThreadState() {
      
      time.add("00:00:00");
      time.add("-00:00:00");

      time.add("00:01:00");
      time.add("-00:01:00");

      time.add("01:01:00");
      time.add("-01:01:00");

      time.add("10:20:30");
      time.add("-10:20:30");

      time.add("100:15:13");
      time.add("-100:15:13");
    }
  }
  
  @Benchmark
  public long testStopTimeFieldMappingFactory(ThreadState state) throws Exception {
    long count = 0;
    for(String time : state.time) {
      count += StopTimeFieldMappingFactory.getStringAsSeconds(time); 
    }
    return count;
  }
  
  
  @Benchmark
  public long testLegacyRegexpStopTimeFieldMappingFactory(ThreadState state) throws Exception {
    long count = 0;
    for(String time : state.time) {
      count += LegacyRegexpStopTimeFieldMappingFactory.getStringAsSeconds(time); 
    }
    return count;
  }

  @Benchmark
  public long testLegacyParseIntStopTimeFieldMappingFactory(ThreadState state) throws Exception {
    long count = 0;
    for(String time : state.time) {
      count += LegacyParseIntStopTimeFieldMappingFactory.getStringAsSeconds(time); 
    }
    return count;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(ParseStopTimeBenchmark.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}

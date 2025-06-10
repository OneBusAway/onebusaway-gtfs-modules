package org.onebusaway.jmh.gtfs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.onebusaway.csv_entities.DelimitedTextParser;
import org.onebusaway.gtfs.serialization.mappings.StopTimeFieldMappingFactory;
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
    private List<String> time = new ArrayList<>();
    
    public ThreadState() {
      // use real data as input to the performance test
      try {
        BufferedReader r = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/brown-county-flex/stop_times.txt")));
        String line = null;
        DelimitedTextParser parser = new DelimitedTextParser(',');
        r.readLine(); // skip first line
        while ((line = r.readLine()) != null) {
          List<String> values = parser.parse(line);
          String string = values.get(values.size() - 6);
          if(string.length() > 0) {
            time.add(values.get(values.size() - 6));
          }
          string = values.get(values.size() - 7);
          if(string.length() > 0) {
            time.add(values.get(values.size() - 7));
          }
        }
        r.close();
        if(time.size() < 100) {
          throw new IllegalStateException();
        }
      } catch(Exception e) {
        throw new RuntimeException(e);
      }
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

package org.onebusaway.jmh.csv;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.onebusaway.csv_entities.DelimitedTextParser;
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
@Warmup(time=3, timeUnit=TimeUnit.SECONDS, iterations=1)
@Measurement(time=3, timeUnit=TimeUnit.SECONDS, iterations=1)
@Timeout(timeUnit=TimeUnit.SECONDS, time=10)
public class CsvLineParserBenchmark {

  private final LegacyDelimitedTextParser legacyDelimitedTextParser = new LegacyDelimitedTextParser(',');
  private final LegacyDelimitedTextParser skipWhitespaceLegacyDelimitedTextParser = new LegacyDelimitedTextParser(',');

  public CsvLineParserBenchmark() {
    skipWhitespaceLegacyDelimitedTextParser.setTrimInitialWhitespace(true);
  }
  
  @State(Scope.Thread)
  public static class ThreadState {
    private List<String> stopTimes = new ArrayList<>();
    private List<String> trips = new ArrayList<>();
    
    public ThreadState() {
      try {
        byte[] stopTimes = IOUtils.resourceToByteArray("/brown-county-flex/stop_times.txt");
        this.stopTimes = toLines(stopTimes);

        byte[] trips = IOUtils.resourceToByteArray("/turlock-fares-v2/trips.txt");
        this.trips = toLines(trips);

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    private List<String> toLines(byte[] stopTimes) throws IOException {
      BufferedReader stopTimesReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(stopTimes)));
      
      List<String> list = new ArrayList<>();
      
      String line;
      while ((line = stopTimesReader.readLine()) != null) {
        list.add(line);
      }
      stopTimesReader.close();
      return list;
    }
  }
  
  @Benchmark
  public int testParseStopTimes(ThreadState state) throws Exception {
    int count = 0;
    List<String> tokens = new ArrayList<>(22);
    
    for(String line : state.stopTimes) {
      DelimitedTextParser.parse(line, tokens);
      count += tokens.size();
      tokens.clear();
    }
    return count;
  }

  @Benchmark
  public int testParseTrips(ThreadState state) throws Exception {
    int count = 0;
    List<String> tokens = new ArrayList<>(20);
    for(String line : state.trips) {
      DelimitedTextParser.parse(line, tokens);
      count += tokens.size();
      tokens.clear();
    }
    return count;
  }
 
  @Benchmark
  public int testParseStopTimesLegacy(ThreadState state) throws Exception {
    int count = 0;
    for(int i = 0; i < 1; i++) {
      for(String line : state.stopTimes) {
        count += legacyDelimitedTextParser.parse(line).size();
      }
      count++;
    }
    return count;
  }

  @Benchmark
  public int testParseTripsLegacy(ThreadState state) throws Exception {
    int count = 0;
    for(int i = 0; i < 1; i++) {
      for(String line : state.trips) {
        count += legacyDelimitedTextParser.parse(line).size();
      }
      count++;
    }
    return count;
  }
  
  @Benchmark
  public int testParseStopTimesSkipWhitespaceLegacy(ThreadState state) throws Exception {
    int count = 0;
    for(int i = 0; i < 1; i++) {
      for(String line : state.stopTimes) {
        count += skipWhitespaceLegacyDelimitedTextParser.parse(line).size();
      }
      count++;
    }
    return count;
  }

  @Benchmark
  public int testParseTripsSkipWhitespaceLegacy(ThreadState state) throws Exception {
    int count = 0;
    for(int i = 0; i < 1; i++) {
      for(String line : state.trips) {
        count += skipWhitespaceLegacyDelimitedTextParser.parse(line).size();
      }
      count++;
    }
    return count;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(CsvLineParserBenchmark.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}

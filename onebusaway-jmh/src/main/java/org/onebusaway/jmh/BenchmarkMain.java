package org.onebusaway.jmh;

import java.time.Instant;

import org.onebusaway.jmh.csv.CsvParserBenchmark;
import org.onebusaway.jmh.gtfs.GtfsBenchmark;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkMain {

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(CsvParserBenchmark.class.getSimpleName())
        .include(GtfsBenchmark.class.getSimpleName())
        .result("jmh-result-" + Instant.now().toString() + ".json")
        .resultFormat(ResultFormatType.JSON)
        .build();
    new Runner(opt).run();
  }

}
package org.onebusaway.jmh.gtfs.shape;

import java.io.File;
import java.util.concurrent.TimeUnit;
import org.onebusaway.csv_entities.CsvInputSource;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GenericMutableDao;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Timeout;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Fork(value = 2)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.SingleShotTime)
@Measurement(time = 100, timeUnit = TimeUnit.SECONDS, iterations = 1)
@Timeout(timeUnit = TimeUnit.SECONDS, time = 1000)
public class ShapeSingleShotBenchmark {

  private static final String directory = "./onebusaway-jmh/src/main/resources/entur";

  @State(Scope.Thread)
  public static class ThreadState {
    GtfsReader reader = new GtfsReader();

    public ThreadState() {
      reader.setOverwriteDuplicates(true);

      GtfsDaoImpl entityStore = (GtfsDaoImpl) reader.getEntityStore();
      entityStore.setPackShapePoints(true);
      entityStore.setPackStopTimes(true);
    }
  }

  @Benchmark
  public Object testParse(ThreadState state) throws Exception {
    return processWithGtfsReader(
        new File(directory), "abcd", false, state.reader, ShapePoint.class);
  }

  @Benchmark
  public Object testParseStringInterning(ThreadState state) throws Exception {
    return processWithGtfsReader(new File(directory), "abcd", true, state.reader, ShapePoint.class);
  }

  @Benchmark
  public Object testParseLegacy(ThreadState state) throws Exception {
    return processWithGtfsReader(
        new File(directory), "abcd", false, state.reader, LegacyShapePoint.class);
  }

  @Benchmark
  public Object testParseLegacyStringInterning(ThreadState state) throws Exception {
    return processWithGtfsReader(
        new File(directory), "abcd", true, state.reader, LegacyShapePoint.class);
  }

  public static GtfsReader processWithEntityStore(
      File resourcePath,
      String agencyId,
      boolean internStrings,
      GenericMutableDao entityStore,
      Class<?> cls)
      throws Exception {
    GtfsReader reader = new GtfsReader();
    reader.setEntityStore(entityStore);
    return processWithGtfsReader(resourcePath, agencyId, internStrings, reader, cls);
  }

  public static GtfsReader processWithGtfsReader(
      File resourcePath, String agencyId, boolean internStrings, GtfsReader reader, Class<?> cls)
      throws Exception {

    reader.setDefaultAgencyId(agencyId);
    reader.setInternStrings(internStrings);

    reader.setInputLocation(resourcePath);

    CsvInputSource inputSource = reader.getInputSource();

    if (cls != null) {
      reader.readEntities(cls, inputSource);
    } else {
      reader.run(inputSource);
    }

    inputSource.close();

    return reader;
  }

  public static void main(String[] args) throws RunnerException {
    Options opt =
        new OptionsBuilder().include(ShapeSingleShotBenchmark.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}

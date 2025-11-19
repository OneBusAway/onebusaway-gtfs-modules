package org.onebusaway.gtfs.serialization.mappings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.onebusaway.csv_entities.CsvEntityContextImpl;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.gtfs.model.Timeframe;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;

class LocalTimeFieldMappingFactoryTest {

  private static final String CSV_START_TIME = "start_time";
  public static final LocalTime FOURTEEN_TWENTY_TWO = LocalTime.of(14, 22);
  private final FieldMapping _fieldMapping = buildFieldMapping();

  @ParameterizedTest
  @ValueSource(strings = {"14:22", "14:22:00", "14:22:00.000000"})
  void translateFromCSVToObject(String time) {
    Map<String, Object> csvValues = Map.of(CSV_START_TIME, time);
    var timeframe = new Timeframe();
    _fieldMapping.translateFromCSVToObject(
        new CsvEntityContextImpl(), csvValues, BeanWrapperFactory.wrap(timeframe));
    assertEquals(FOURTEEN_TWENTY_TWO, timeframe.getStartTime());
  }

  @Test
  void translateFromObjectToCSV() {
    var tf = new Timeframe();
    tf.setStartTime(LocalTime.of(18, 54));
    Map<String, Object> csvValues = new HashMap<>();

    _fieldMapping.translateFromObjectToCSV(
        new CsvEntityContextImpl(), BeanWrapperFactory.wrap(tf), csvValues);
    assertEquals("18:54:00", csvValues.get(CSV_START_TIME));
  }

  private static Stream<Arguments> localTimeCases() {
    return Stream.of(
        Arguments.of("14:22", FOURTEEN_TWENTY_TWO),
        Arguments.of("14:22:00", FOURTEEN_TWENTY_TWO),
        Arguments.of("14:22:00.0000", FOURTEEN_TWENTY_TWO),
        Arguments.of("00:00:00", LocalTime.MIN),
        Arguments.of("000:00:00", LocalTime.MIN),
        Arguments.of("0:00", LocalTime.MIN),
        Arguments.of("0:00:00", LocalTime.MIN));
  }

  @ParameterizedTest
  @MethodSource("localTimeCases")
  void parse(String time, LocalTime expected) {
    assertEquals(expected, LocalTimeFieldMappingFactory.parseLocalTime(time));
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "0.0", "-1:00", "25:12"})
  void failParse(String time) {
    assertThrows(Exception.class, () -> LocalTimeFieldMappingFactory.parseLocalTime(time));
  }

  private static FieldMapping buildFieldMapping() {
    var factory = new LocalTimeFieldMappingFactory();
    var schemaFactory = GtfsEntitySchemaFactory.createEntitySchemaFactory();
    return factory.createFieldMapping(
        schemaFactory, Timeframe.class, CSV_START_TIME, "startTime", LocalTime.class, false);
  }
}

package org.onebusaway.gtfs.serialization.mappings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.onebusaway.gtfs.serialization.mappings.StopTimeFieldMappingFactory.getSecondsAsString;
import static org.onebusaway.gtfs.serialization.mappings.StopTimeFieldMappingFactory.getStringAsSeconds;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.CsvEntityContextImpl;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;

class StopTimeFieldMappingFactoryTest {

  private static Stream<Arguments> csvMappingProvider() {
    return Stream.of(
            Arguments.of("1234:23:32", new Integer(1234 * 60 * 60 + 23 * 60 + 32))
    );
  }

  @ParameterizedTest
  @MethodSource("csvMappingProvider")
  void testCsvMapping(String timeStr, Integer expectedSeconds) {
    StopTimeFieldMappingFactory factory = new StopTimeFieldMappingFactory();
    DefaultEntitySchemaFactory schemaFactory = new DefaultEntitySchemaFactory();
    String propName = "time";
    FieldMapping mapping = factory.createFieldMapping(
            schemaFactory,
            Dummy.class, propName, propName, Integer.class, true
    );

    CsvEntityContext context = new CsvEntityContextImpl();

    Map<String, Object> csvValues = new HashMap<>();
    csvValues.put(propName, timeStr);

    Dummy obj = new Dummy();
    BeanWrapper wrapped = BeanWrapperFactory.wrap(obj);

    mapping.translateFromCSVToObject(context, csvValues, wrapped);

    assertEquals(expectedSeconds, obj.getTime());

    csvValues.clear();
    mapping.translateFromObjectToCSV(context, wrapped, csvValues);
    assertEquals(timeStr, csvValues.get(propName));
  }

  private static Stream<Arguments> stringAsSecondsCases() {
    return Stream.of(
            Arguments.of("00:00:00", 0),
            Arguments.of("-00:00:00", 0),
            Arguments.of("00:01:00", 60),
            Arguments.of("-00:01:00", 60),
            Arguments.of("01:01:00", 3660),
            Arguments.of("1:01:00", 3660),
            Arguments.of("-01:01:00", -3540),
            Arguments.of("10:20:30", 37230),
            Arguments.of("-10:20:30", -34770),
            Arguments.of("100:15:13", 360913),
            Arguments.of("-100:15:13", -359087)
    );
  }

  @ParameterizedTest
  @MethodSource("stringAsSecondsCases")
  void testGetStringAsSeconds(String input, int expected) {
    assertEquals(expected, getStringAsSeconds(input));
  }

  private static Stream<String> invalidCases() {
    return Stream.of(
            "", "000000", "00:00", "--00:00:00", "a0:00:00", "0a:00:00", "00:a0:00",
            "00:0a:00", "00:00:a0", "00:00:0a", "+0:00:00", "0+:00:00", "00:+0:00", "00:0+:00",
            "00:00:+0", "00:00:0+"
    );
  }

  @ParameterizedTest
  @MethodSource("invalidCases")
  void testInvalidTimesThrowException(String input) {
      assertThrows(InvalidStopTimeException.class, () -> getStringAsSeconds(input));
  }

  private static Stream<Arguments> secondsStringCases() {
    return Stream.of(
            Arguments.of(0, "00:00:00"),
            Arguments.of(60, "00:01:00"),
            Arguments.of(-60, "-01:59:00"),
            Arguments.of(3660, "01:01:00"),
            Arguments.of(-3540, "-01:01:00"),
            Arguments.of(37230, "10:20:30"),
            Arguments.of(-34770, "-10:20:30"),
            Arguments.of(360913, "100:15:13"),
            Arguments.of(-359087, "-100:15:13")
    );
  }

  @ParameterizedTest
  @MethodSource("secondsStringCases")
  void testGetSecondsAsString(int input, String expected) {
    assertEquals(expected, getSecondsAsString(input));
  }

  public static class Dummy {
    private Integer time;

    public void setTime(Integer time) {
      this.time = time;
    }

    public Integer getTime() {
      return time;
    }
  }

}

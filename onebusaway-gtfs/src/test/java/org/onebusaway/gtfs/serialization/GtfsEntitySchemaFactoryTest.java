package org.onebusaway.gtfs.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class GtfsEntitySchemaFactoryTest {

  @Test
  public void testGetEntityClassesHasNoDuplicates() {
    List<Class<?>> classes = GtfsEntitySchemaFactory.getEntityClasses();
    Set<Class<?>> seen = new HashSet<>();
    List<Class<?>> duplicates =
        classes.stream().filter(c -> !seen.add(c)).collect(Collectors.toList());
    assertEquals(List.of(), duplicates, "getEntityClasses() must not contain duplicate entries");
  }
}

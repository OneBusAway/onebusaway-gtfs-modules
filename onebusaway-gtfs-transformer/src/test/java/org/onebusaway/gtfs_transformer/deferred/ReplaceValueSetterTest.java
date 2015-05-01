package org.onebusaway.gtfs_transformer.deferred;

import static org.junit.Assert.*;

import org.junit.Test;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.model.Stop;

/**
 * Unit-test for {@link ReplaceValueSetter}.
 */
public class ReplaceValueSetterTest {

  @Test
  public void test() {
    ReplaceValueSetter setter = new ReplaceValueSetter("cats", "dogs");
    Stop stop = new Stop();
    stop.setName("I like cats.");
    setter.setValue(BeanWrapperFactory.wrap(stop), "name");
    assertEquals("I like dogs.", stop.getName());
  }
}

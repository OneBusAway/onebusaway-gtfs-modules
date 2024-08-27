/**
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_transformer.deferred;

import static  org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
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

/**
 * Copyright (C) 2011 Google Inc.
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
package org.onebusaway.gtfs_transformer.impl.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class AgencyAndIdConverterTest {

  private TransformContext _context = new TransformContext();
  private AgencyAndIdConverter _converter;

  @Before
  public void before() {
    _context.setDefaultAgencyId("1");
    _converter = new AgencyAndIdConverter(_context);
  }

  @Test
  public void testConvertFromString() {

    AgencyAndId id = (AgencyAndId) _converter.convert(AgencyAndId.class, "a_b");
    assertEquals("a", id.getAgencyId());
    assertEquals("b", id.getId());
  }

  @Test
  public void testConvertToString() {
    AgencyAndId id = new AgencyAndId("a", "b");
    String value = (String) _converter.convert(String.class, id);
    assertEquals("a_b", value);
  }

  @Test
  public void testNull() {
    Object value = _converter.convert(AgencyAndId.class, null);
    assertNull(value);
  }
}

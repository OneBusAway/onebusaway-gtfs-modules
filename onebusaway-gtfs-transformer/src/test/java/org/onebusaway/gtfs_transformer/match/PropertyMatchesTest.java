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
package org.onebusaway.gtfs_transformer.match;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.junit.Before;
import org.junit.Test;
import org.onebusaway.collections.PropertyPathExpression;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs_transformer.impl.converters.AgencyAndIdConverter;
import org.onebusaway.gtfs_transformer.services.TransformContext;

public class PropertyMatchesTest {

  private Agency _agency;

  private Route _route;

  @Before
  public void before() {
    _agency = new Agency();
    _agency.setId("metro");
    _agency.setName("Metro");

    _route = new Route();
    _route.setAgency(_agency);
    _route.setId(new AgencyAndId("metro", "r2"));
    _route.setShortName("2");
    _route.setLongName("The Two");
  }

  @Test
  public void testSimpleProperty() {

    Map<PropertyPathExpression, Object> propertyPathsAndValues = new HashMap<PropertyPathExpression, Object>();
    propertyPathsAndValues.put(new PropertyPathExpression("shortName"), "2");

    PropertyMatches m = new PropertyMatches(propertyPathsAndValues);
    assertTrue(m.isApplicableToObject(_route));
  }

  @Test
  public void testMultipleProperties() {

    Map<PropertyPathExpression, Object> propertyPathsAndValues = new HashMap<PropertyPathExpression, Object>();
    propertyPathsAndValues.put(new PropertyPathExpression("shortName"), "2");
    propertyPathsAndValues.put(new PropertyPathExpression("longName"),
        "The Two");

    PropertyMatches m = new PropertyMatches(propertyPathsAndValues);
    assertTrue(m.isApplicableToObject(_route));
  }

  @Test
  public void testMultiplePropertiesMismatch() {

    Map<PropertyPathExpression, Object> propertyPathsAndValues = new HashMap<PropertyPathExpression, Object>();
    propertyPathsAndValues.put(new PropertyPathExpression("shortName"), "2");
    propertyPathsAndValues.put(new PropertyPathExpression("longName"),
        "The Three");

    PropertyMatches m = new PropertyMatches(propertyPathsAndValues);
    assertFalse(m.isApplicableToObject(_route));
  }

  @Test
  public void testNestedProperty() {

    Map<PropertyPathExpression, Object> propertyPathsAndValues = new HashMap<PropertyPathExpression, Object>();
    propertyPathsAndValues.put(new PropertyPathExpression("id.id"), "2");

    PropertyMatches m = new PropertyMatches(propertyPathsAndValues);
    assertFalse(m.isApplicableToObject(_route));
  }

  @Test
  public void testNonStringProperty() {

    Map<PropertyPathExpression, Object> propertyPathsAndValues = new HashMap<PropertyPathExpression, Object>();
    propertyPathsAndValues.put(new PropertyPathExpression("agency"), _agency);

    PropertyMatches m = new PropertyMatches(propertyPathsAndValues);
    assertTrue(m.isApplicableToObject(_route));
  }

  @Test
  public void testPropertyTypeConversion() {

    TransformContext context = new TransformContext();

    ConvertUtils.register(new AgencyAndIdConverter(context), AgencyAndId.class);

    Map<PropertyPathExpression, Object> propertyPathsAndValues = new HashMap<PropertyPathExpression, Object>();
    propertyPathsAndValues.put(new PropertyPathExpression("id"), "metro_r2");

    PropertyMatches m = new PropertyMatches(propertyPathsAndValues);
    assertTrue(m.isApplicableToObject(_route));
  }
}

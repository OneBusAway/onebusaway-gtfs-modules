/**
 * Copyright (C) 2012 Google, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_merge.strategies;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

public class EntityMergeTestSupport {

  protected Map<String, Object> entityByRawId;

  @BeforeEach
  public void setup() {
    entityByRawId = new HashMap<>();
  }

  protected GtfsMergeContext context(
      GtfsRelationalDaoImpl source, GtfsRelationalDaoImpl target, String prefix) {
    return new GtfsMergeContext(source, target, prefix, entityByRawId);
  }
}

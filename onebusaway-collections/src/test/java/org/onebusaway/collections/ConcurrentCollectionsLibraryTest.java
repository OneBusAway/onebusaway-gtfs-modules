/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

public class ConcurrentCollectionsLibraryTest {

  @Test
  public void testAddToMapValueList() throws InterruptedException {

    ConcurrentMap<String, List<String>> m = new ConcurrentHashMap<>();

    List<AddOp> ops = new ArrayList<>();
    ops.add(new AddOp(m, "a", "1"));
    ops.add(new AddOp(m, "b", "1"));
    ops.add(new AddOp(m, "a", "2"));
    ops.add(new AddOp(m, "a", "1"));
    ops.add(new AddOp(m, "b", "2"));
    ops.add(new AddOp(m, "b", "3"));
    ops.add(new AddOp(m, "a", "3"));
    ops.add(new AddOp(m, "a", "3"));
    ops.add(new AddOp(m, "a", "4"));
    ops.add(new AddOp(m, "a", "5"));
    ops.add(new AddOp(m, "a", "6"));
    ops.add(new AddOp(m, "a", "7"));

    ExecutorService service = Executors.newFixedThreadPool(5);
    service.invokeAll(ops);

    List<String> values = m.get("a");
    assertEquals(7, values.size());
    assertTrue(values.contains("1"));
    assertTrue(values.contains("2"));
    assertTrue(values.contains("3"));
    assertTrue(values.contains("4"));
    assertTrue(values.contains("5"));
    assertTrue(values.contains("6"));
    assertTrue(values.contains("7"));
  }

  private static class AddOp implements Callable<String> {

    private ConcurrentMap<String, List<String>> _map;
    private String _key;
    private String _value;

    public AddOp(ConcurrentMap<String, List<String>> map, String key, String value) {
      _map = map;
      _key = key;
      _value = value;
    }

    @Override
    public String call() throws Exception {
      ConcurrentCollectionsLibrary.addToMapValueList(_map, _key, _value);
      return _value;
    }
  }
}

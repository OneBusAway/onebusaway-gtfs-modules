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
package org.onebusaway.gtfs.scripts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.gtfs.serialization.GtfsReader;

public class EntityCounterMain {
  public static void main(String[] args) throws IOException {

    if (args.length != 1) {
      System.err.println("usage: path/to/gtfs_feed");
      System.exit(-1);
    }

    GtfsReader reader = new GtfsReader();
    reader.setInputLocation(new File(args[0]));

    EntityCounter counter = new EntityCounter();
    reader.addEntityHandler(counter);

    reader.run();

    Map<Class<?>, Integer> counts = counter.getCounts();
    List<Class<?>> types = new ArrayList<Class<?>>(counts.keySet());
    Collections.sort(types, new ClassNameComparator());

    for (Class<?> type : types) System.out.println(type.getName() + " " + counts.get(type));
  }

  private static class EntityCounter implements EntityHandler {

    private Map<Class<?>, Integer> _counts = new HashMap<Class<?>, Integer>();

    public Map<Class<?>, Integer> getCounts() {
      return _counts;
    }

    @Override
    public void handleEntity(Object bean) {
      Class<? extends Object> type = bean.getClass();
      Integer count = _counts.get(type);
      if (count == null) count = 0;
      count++;
      _counts.put(type, count);
    }
  }

  private static class ClassNameComparator implements Comparator<Class<?>> {
    @Override
    public int compare(Class<?> o1, Class<?> o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }
}

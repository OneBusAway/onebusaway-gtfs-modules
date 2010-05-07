package org.onebusaway.gtfs.scripts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.csv.EntityHandler;
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

    for (Class<?> type : types)
      System.out.println(type.getName() + " " + counts.get(type));
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
      if (count == null)
        count = 0;
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

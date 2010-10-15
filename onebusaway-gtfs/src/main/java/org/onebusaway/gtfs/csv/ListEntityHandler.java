package org.onebusaway.gtfs.csv;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ListEntityHandler<T> implements EntityHandler, Iterable<T> {

  private List<T> _values = new ArrayList<T>();

  public List<T> getValues() {
    return _values;
  }

  @Override
  public Iterator<T> iterator() {
    return _values.iterator();
  }

  /****
   * {@link EntityHandler} Interface
   ****/

  @SuppressWarnings("unchecked")
  @Override
  public void handleEntity(Object bean) {
    _values.add((T) bean);
  }
}

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

/**
 * A simple, mutable double-precision range class for tracking a min and max value.
 *
 * @author bdferris
 */
public class Range {

  private double _min = Double.POSITIVE_INFINITY;

  private double _max = Double.NEGATIVE_INFINITY;

  public Range() {}

  public Range(double v) {
    addValue(v);
  }

  public Range(double from, double to) {
    addValue(from);
    addValue(to);
  }

  public void addValue(double value) {
    _min = Math.min(_min, value);
    _max = Math.max(_max, value);
  }

  public void setMin(double value) {
    _min = value;
    _max = Math.max(_max, value);
  }

  public void setMax(double value) {
    _min = Math.min(_min, value);
    _max = value;
  }

  public double getMin() {
    return _min;
  }

  public double getMax() {
    return _max;
  }

  public double getRange() {
    return _max - _min;
  }

  public boolean isEmpty() {
    return _min > _max;
  }

  @Override
  public String toString() {
    return _min + " " + _max;
  }
}

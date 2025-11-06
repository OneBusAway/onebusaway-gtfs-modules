package org.onebusaway.gtfs_merge;

import org.apache.commons.lang3.builder.ToStringStyle;

class MergerStyle extends ToStringStyle {

  public static final ToStringStyle MULTI_LINE_STYLE = new MergerStyle();

  /**
   * Constructs a new instance.
   *
   * <p>Use the static constant rather than instantiating.
   */
  MergerStyle() {
    this.setContentStart("[");
    this.setFieldSeparator(System.lineSeparator() + "  ");
    this.setFieldSeparatorAtStart(true);
    this.setContentEnd(System.lineSeparator() + "]");
    this.setUseShortClassName(true);
    this.setUseIdentityHashCode(false);
  }

  /**
   * Ensure Singleton after serialization.
   *
   * @return the singleton
   */
  private Object readResolve() {
    return MULTI_LINE_STYLE;
  }
}

package org.onebusaway.gtfs_merge;

import static org.junit.Assert.assertEquals;

import org.apache.commons.cli.Option;
import org.junit.Test;
import org.onebusaway.gtfs_merge.strategies.EDuplicateDetectionStrategy;
import org.onebusaway.gtfs_merge.strategies.StopMergeStrategy;

public class OptionHandlerTest {
  /**
   * Test that an identity argument is properly applied
   */
  @Test
  public void testIdentity() {
    StopMergeStrategy strategy = new StopMergeStrategy();
    OptionHandler handler = new OptionHandler();
    Option option = new Option(GtfsMergerMain.ARG_RENAME_DUPLICATES, true, "");
    handler.handleOption(option, strategy);

    assertEquals(EDuplicateDetectionStrategy.IDENTITY,
        strategy.getDuplicateDetectionStrategy());
  }
}

package org.onebusaway.gtfs_merge.strategies;

import java.io.IOException;

import org.junit.Test;
import org.onebusaway.gtfs.services.MockGtfs;

public class IntegrationTest {

  @Test
  public void go() throws IOException {
    MockGtfs gtfsA = MockGtfs.create();
    gtfsA.putAgencies(1);
    System.out.println("go");
  }
}

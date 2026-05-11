package org.onebusaway.gtfs.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.impl.FileSupport;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Location;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

public class LocationsGeoJSONRoundTripTest {

  private FileSupport _support = new FileSupport();
  private File _tmpDirectory;

  @BeforeEach
  public void setup() throws IOException {
    _tmpDirectory = File.createTempFile("LocationsGeoJSONRoundTripTest-", "-tmp");
    if (_tmpDirectory.exists()) _support.deleteFileRecursively(_tmpDirectory);
    _tmpDirectory.mkdirs();
    _support.markForDeletion(_tmpDirectory);
  }

  @AfterEach
  public void teardown() {
    _support.cleanup();
  }

  @Test
  public void roundTripToDirectory() throws IOException {
    File input = GtfsTestData.getPierceTransitFlex();
    GtfsRelationalDao original = readFeed(input, "1");

    Collection<Location> originalLocations = original.getAllLocations();
    assertTrue(originalLocations.size() > 0, "Expected at least one Location in test feed");

    GtfsWriter writer = new GtfsWriter();
    writer.setOutputLocation(_tmpDirectory);
    writer.run(original);

    GtfsRelationalDao reread = readFeed(_tmpDirectory, "1");

    assertLocationsEqual(originalLocations, reread);
  }

  @Test
  public void roundTripToZip() throws IOException {
    File input = GtfsTestData.getPierceTransitFlex();
    GtfsRelationalDao original = readFeed(input, "1");

    Collection<Location> originalLocations = original.getAllLocations();
    assertTrue(originalLocations.size() > 0, "Expected at least one Location in test feed");

    File zipOut = new File(_tmpDirectory, "output.zip");
    GtfsWriter writer = new GtfsWriter();
    writer.setOutputLocation(zipOut);
    writer.run(original);

    GtfsRelationalDao reread = readFeed(zipOut, "1");

    assertLocationsEqual(originalLocations, reread);
  }

  private static void assertLocationsEqual(
      Collection<Location> originalLocations, GtfsRelationalDao reread) {
    assertEquals(originalLocations.size(), reread.getAllLocations().size());
    for (Location expected : originalLocations) {
      Location actual = reread.getEntityForId(Location.class, expected.getId());
      assertEquals(expected.getId().getId(), actual.getId().getId());
      assertEquals(expected.getName(), actual.getName());
      assertEquals(expected.getDescription(), actual.getDescription());
      assertEquals(expected.getUrl(), actual.getUrl());
      assertEquals(expected.getZoneId(), actual.getZoneId());
      assertEquals(expected.getGeometry(), actual.getGeometry());
    }
  }

  private static GtfsRelationalDao readFeed(File path, String defaultAgencyId) throws IOException {
    GtfsReader reader = new GtfsReader();
    reader.setDefaultAgencyId(defaultAgencyId);
    reader.setInputLocation(path);
    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    reader.setEntityStore(dao);
    reader.run();
    return dao;
  }
}

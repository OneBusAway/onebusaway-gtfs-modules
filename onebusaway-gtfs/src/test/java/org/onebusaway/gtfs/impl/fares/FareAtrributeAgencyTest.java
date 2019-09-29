package org.onebusaway.gtfs.impl.fares;

import org.junit.Test;
import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.FareAttribute;
import org.onebusaway.gtfs.serialization.GtfsReader;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class FareAtrributeAgencyTest {


	@Test
	public void testAgenciesOnBartGtfs() throws Exception {
		GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
		GtfsReader gtfsReader = new GtfsReader();
		gtfsReader.setEntityStore(dao);
		gtfsReader.setInputLocation(GtfsTestData.getBartGtfs());
		gtfsReader.run();


		final Collection<FareAttribute> fareAttributes = dao.getAllFareAttributes();
		final Set<String> agencyIdsInFareAttr = fareAttributes.stream().map(FareAttribute::getAgencyId).collect(Collectors.toSet());
		boolean isContainsBothExpectedAgencies = agencyIdsInFareAttr.contains("AirBART") && agencyIdsInFareAttr.contains("BART");
		if (!isContainsBothExpectedAgencies) {
			throw new Exception("Does not contain one of the expected agencies in the fare attributes");
		}

	}
}

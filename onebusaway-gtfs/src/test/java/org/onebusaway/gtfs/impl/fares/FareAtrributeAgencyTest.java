/**
 * Copyright (C) 2019 Dor Rud <dor.rud132@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

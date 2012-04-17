/**
 * Copyright (C) 2011 Geno Roupsky <geno@masconsult.eu>
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
package org.onebusaway.gtfs.serialization.mappings;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.CsvEntityContextImpl;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;

public class StopTimeFieldMappingFactoryTest {

	@Test
	public void test() {

		StopTimeFieldMappingFactory factory = new StopTimeFieldMappingFactory();
		DefaultEntitySchemaFactory schemaFactory = new DefaultEntitySchemaFactory();
		String propName = "time";
		FieldMapping mapping = factory.createFieldMapping(schemaFactory,
				Dummy.class, propName, propName, Integer.class, true);

		CsvEntityContext context = new CsvEntityContextImpl();

		Map<String, Object> csvValues = new HashMap<String, Object>();
		csvValues.put(propName, "1234:23:32");

		Dummy obj = new Dummy();
		BeanWrapper wrapped = BeanWrapperFactory.wrap(obj);

		mapping.translateFromCSVToObject(context, csvValues, wrapped);

		assertEquals(new Integer(1234 * 60 * 60 + 23 * 60 + 32), obj.getTime());

		csvValues.clear();
		mapping.translateFromObjectToCSV(context, wrapped, csvValues);
		assertEquals("1234:23:32", csvValues.get(propName));
	}

	public static class Dummy {
		private Integer time;

		public void setTime(Integer time) {
			this.time = time;
		}

		public Integer getTime() {
			return time;
		}
	}

}

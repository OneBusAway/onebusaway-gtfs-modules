/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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

package org.onebusaway.csv_entities;

import org.onebusaway.csv_entities.schema.DecimalFieldMappingFactory;
import org.onebusaway.csv_entities.schema.DecimalFieldMappingFactory.NumberFormatAnnotation;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;

@CsvFields(filename = "test_beans")
public class OptionalFieldTestBean {

    private static final int DEFAULT_VALUE = -999;

    @CsvField(optional = true)
    private int intValue = DEFAULT_VALUE;

    @CsvField(optional = true, mapping = DecimalFieldMappingFactory.class)
    @NumberFormatAnnotation("0.00")
    private double doubleValue = DEFAULT_VALUE;

    public boolean isIntValueSet() {
        return intValue != DEFAULT_VALUE;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(final int intValue) {
        this.intValue = intValue;
    }

    public void clearIntValue() {
        this.intValue = DEFAULT_VALUE;
    }

    public boolean isDoubleValueSet() {
        return doubleValue != DEFAULT_VALUE;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public void clearDoubleValue() {
        this.doubleValue = DEFAULT_VALUE;
    }

}

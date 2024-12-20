/**
 * Copyright (C) 2024 Sound Transit
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.exceptions.CsvEntityException;
import org.onebusaway.csv_entities.schema.AbstractFieldMapping;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.csv_entities.schema.FieldMappingFactory;

public class FareAmountFieldMappingFactory implements FieldMappingFactory {
  

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory,
      Class<?> entityType, String csvFieldName, String objFieldName,
      Class<?> objFieldType, boolean required) {

    return new FareAmountFieldMapping(entityType, csvFieldName, objFieldName, required);
  }
  
  private static class FareAmountFieldMapping extends AbstractFieldMapping {

    public FareAmountFieldMapping(Class<?> entityType, String csvFieldName,
                                   String objFieldName, boolean required) {
      super(entityType, csvFieldName, objFieldName, required);
    }

    @Override
    public void translateFromObjectToCSV(CsvEntityContext context,
        BeanWrapper object, Map<String, Object> csvValues) {

        String currencyString = (String) object.getPropertyValue("currency");
        Currency currency = Currency.getInstance(currencyString);
        Float amount = (Float) object.getPropertyValue(_objFieldName);

        DecimalFormat formatter = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        formatter.setCurrency(currency);

        // remove "$", "¥", "₹" and other currency symbols from the output
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setCurrencySymbol("");
        formatter.setDecimalFormatSymbols(symbols);
        formatter.setMaximumFractionDigits(currency.getDefaultFractionDigits());

        csvValues.put(_csvFieldName, formatter.format(amount));
    }

    @Override
    public void translateFromCSVToObject(CsvEntityContext context, Map<String, Object> csvValues, BeanWrapper object)
        throws CsvEntityException {

        if (isMissingAndOptional(csvValues))
          return;
  
        Object value = csvValues.get(_csvFieldName);

        Float amount = (float) value;
        object.setPropertyValue(_objFieldName, amount);
    }
  }
}

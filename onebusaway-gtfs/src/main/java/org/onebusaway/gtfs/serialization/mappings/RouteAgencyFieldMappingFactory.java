package org.onebusaway.gtfs.serialization.mappings;

import java.util.Map;

import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.schema.AbstractFieldMapping;
import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.EntitySchemaFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.csv_entities.schema.FieldMappingFactory;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.serialization.GtfsReaderContext;

/**
 * Responsible for setting the {@link Route#setAgency(Agency)} from a csv
 * "agency_id" field in "routes.txt" and vice-versa.
 * 
 * @author bdferris
 * @see Route#setAgency(Agency)
 */
public class RouteAgencyFieldMappingFactory implements FieldMappingFactory {

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory,
      Class<?> entityType, String csvFieldName, String objFieldName,
      Class<?> objFieldType, boolean required) {

    return new RouteAgencyFieldMapping(entityType, csvFieldName, objFieldName,
        Agency.class, required);
  }

  private class RouteAgencyFieldMapping extends AbstractFieldMapping {

    public RouteAgencyFieldMapping(Class<?> entityType, String csvFieldName,
        String objFieldName, Class<?> objFieldType, boolean required) {
      super(entityType, csvFieldName, objFieldName, required);
    }

    public void translateFromCSVToObject(CsvEntityContext context,
        Map<String, Object> csvValues, BeanWrapper object) {

      GtfsReaderContext ctx = (GtfsReaderContext) context.get(GtfsReader.KEY_CONTEXT);
      String agencyId = (String) csvValues.get(_csvFieldName);

      if (isMissing(csvValues))
        agencyId = ctx.getDefaultAgencyId();

      agencyId = ctx.getTranslatedAgencyId(agencyId);

      Agency agency = null;

      for (Agency testAgency : ctx.getAgencies()) {
        if (testAgency.getId().equals(agencyId)) {
          agency = testAgency;
          break;
        }
      }
      if (agency == null)
        throw new AgencyNotFoundForRouteException(Route.class,
            object.getWrappedInstance(Route.class), agencyId);

      object.setPropertyValue(_objFieldName, agency);
    }

    public void translateFromObjectToCSV(CsvEntityContext context,
        BeanWrapper object, Map<String, Object> csvValues) {

      Agency agency = (Agency) object.getPropertyValue(_objFieldName);

      if (isOptional() && agency == null)
        return;

      csvValues.put(_csvFieldName, agency.getId());
    }

  }
}

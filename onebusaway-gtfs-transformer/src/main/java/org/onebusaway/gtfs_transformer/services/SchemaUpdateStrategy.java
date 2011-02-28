package org.onebusaway.gtfs_transformer.services;

import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;

public interface SchemaUpdateStrategy {
  public void updateSchema(DefaultEntitySchemaFactory factory);
}

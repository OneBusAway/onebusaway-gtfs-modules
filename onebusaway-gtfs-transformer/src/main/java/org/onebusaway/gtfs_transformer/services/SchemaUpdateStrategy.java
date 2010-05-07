package org.onebusaway.gtfs_transformer.services;

import org.onebusaway.gtfs.csv.schema.DefaultEntitySchemaFactory;

public interface SchemaUpdateStrategy {
  public void updateSchema(DefaultEntitySchemaFactory factory);
}

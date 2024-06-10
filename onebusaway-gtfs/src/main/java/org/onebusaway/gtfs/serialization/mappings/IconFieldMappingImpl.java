package org.onebusaway.gtfs.serialization.mappings;

public class IconFieldMappingImpl extends EntityFieldMappingImpl{
    public IconFieldMappingImpl(Class<?> entityType, String csvFieldName, String objFieldName, Class<?> objFieldType, boolean required) {
        super(entityType, csvFieldName, objFieldName, objFieldType, required);
    }
}

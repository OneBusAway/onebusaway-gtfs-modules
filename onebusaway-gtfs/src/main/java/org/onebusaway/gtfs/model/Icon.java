package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;

/**
 * GTFS Extension representing icon configuration data.
 */
@CsvFields(filename = "icons.txt", required = false, prefix = "icon_")
public final class Icon extends IdentityBean<AgencyAndId>{
    private static final long serialVersionUID = 1L;

    @CsvField(mapping = DefaultAgencyIdFieldMappingFactory.class)
    private AgencyAndId id;
    @CsvField(optional = true)
    private String description;
    @CsvField
    private String url;

    @Override
    public AgencyAndId getId() {
        return id;
    }

    @Override
    public void setId(AgencyAndId id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

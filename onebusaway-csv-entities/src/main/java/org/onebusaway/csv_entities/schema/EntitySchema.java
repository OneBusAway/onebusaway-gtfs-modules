/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.csv_entities.schema;

import java.util.ArrayList;
import java.util.List;

public class EntitySchema extends BaseEntitySchema {

  private final String _filename;

  private final boolean _required;

  private List<ExtensionEntitySchema> _extensions = new ArrayList<ExtensionEntitySchema>();

  public EntitySchema(Class<?> entityClass, String filename, boolean required) {
    super(entityClass);
    _filename = filename;
    _required = required;
  }

  public EntitySchema(EntitySchema schema) {
    super(schema);
    _filename = schema._filename;
    _required = schema._required;
    _extensions = new ArrayList<ExtensionEntitySchema>(schema._extensions);
  }

  public String getFilename() {
    return _filename;
  }

  public boolean isRequired() {
    return _required;
  }

  public List<ExtensionEntitySchema> getExtensions() {
    return _extensions;
  }

  public void addExtension(ExtensionEntitySchema extension) {
    _extensions.add(extension);
  }
}

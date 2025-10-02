/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2013 Google, Inc.
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
import java.util.Collection;
import java.util.List;
import org.onebusaway.csv_entities.schema.beans.CsvEntityMappingBean;

public class DefaultEntitySchemaFactory extends AbstractEntitySchemaFactoryImpl {

  private List<BeanDefinitionSource> _sources = new ArrayList<>();

  public void addBean(CsvEntityMappingBean bean) {
    _sources.add(new CsvEntityMappingBeanSource(bean));
  }

  public void addFactory(ListableCsvMappingFactory factory) {
    _sources.add(new ListableCsvMappingFactorySource(factory));
  }

  /****
   * {@link AbstractEntitySchemaFactoryImpl} Interface
   ****/

  @Override
  protected void processBeanDefinitions() {
    for (BeanDefinitionSource source : _sources) source.processBeanDefinitions();
  }

  private interface BeanDefinitionSource {
    public void processBeanDefinitions();
  }

  private class CsvEntityMappingBeanSource implements BeanDefinitionSource {

    private CsvEntityMappingBean _bean;

    public CsvEntityMappingBeanSource(CsvEntityMappingBean bean) {
      _bean = bean;
    }

    public void processBeanDefinitions() {
      registerBeanDefinition(_bean);
    }
  }

  private class ListableCsvMappingFactorySource implements BeanDefinitionSource {

    private ListableCsvMappingFactory _factory;

    public ListableCsvMappingFactorySource(ListableCsvMappingFactory factory) {
      _factory = factory;
    }

    public void processBeanDefinitions() {
      Collection<CsvEntityMappingBean> beans = _factory.getEntityMappings();
      for (CsvEntityMappingBean bean : beans) registerBeanDefinition(bean);
    }
  }
}

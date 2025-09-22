/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org> Copyright (C) 2012 Google, Inc.
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.onebusaway.csv_entities.HasExtensions;
import org.onebusaway.csv_entities.exceptions.EntityInstantiationException;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFieldNameConvention;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.csv_entities.schema.beans.CsvEntityMappingBean;
import org.onebusaway.csv_entities.schema.beans.CsvFieldMappingBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEntitySchemaFactoryImpl
    implements EntitySchemaFactory, ListableCsvMappingFactory {

  private static final Logger _log = LoggerFactory.getLogger(AbstractEntitySchemaFactoryImpl.class);

  private boolean _initialized = false;

  private Map<Class<?>, CsvEntityMappingBean> _mappingBeansByClass =
      new HashMap<Class<?>, CsvEntityMappingBean>();

  private Map<Class<?>, List<Class<?>>> _extensionsByClass =
      new HashMap<Class<?>, List<Class<?>>>();

  private Map<Class<?>, EntitySchema> _schemasByClass = new HashMap<Class<?>, EntitySchema>();

  /**
   * It can be useful to support the reading and writing of additional custom fields for a
   * particular entity type without the need to modify the base entity type directly. We support
   * this capability through "extensions": additional extension entity types, defining their own
   * custom fields, that are associated with a base entity type and then processed along with the
   * base entity when reading and writing data. Every time a base entity is read, an extension
   * entity is also read, and associated with the base entity via the {@link HasExtensions}
   * interface, which the base entity must implement in order to support extensions.
   *
   * @param type the base schema entity type to extend
   * @param extensionType the extension type, with additional fields to read and write
   */
  public void addExtension(Class<? extends HasExtensions> type, Class<?> extensionType) {
    List<Class<?>> extensionTypes = _extensionsByClass.get(type);
    if (extensionTypes == null) {
      extensionTypes = new ArrayList<Class<?>>();
      _extensionsByClass.put(type, extensionTypes);
    }
    extensionTypes.add(extensionType);

    _schemasByClass.remove(type);
  }

  /****
   * {@link ListableCsvMappingFactory} Interface
   ****/

  public Collection<CsvEntityMappingBean> getEntityMappings() {
    initialize();
    return new ArrayList<CsvEntityMappingBean>(_mappingBeansByClass.values());
  }

  /****
   * {@link EntitySchemaFactory} Interface
   ****/

  public EntitySchema getSchema(Class<?> entityClass) {

    initialize();

    EntitySchema schema = _schemasByClass.get(entityClass);

    if (schema == null) {
      schema = createSchemaForEntityClass(entityClass);
      _schemasByClass.put(entityClass, schema);
    }

    return schema;
  }

  /****
   * Protected Methods
   ****/

  protected abstract void processBeanDefinitions();

  protected void registerBeanDefinition(CsvEntityMappingBean bean) {
    CsvEntityMappingBean existingBean = _mappingBeansByClass.get(bean.getType());
    if (existingBean != null) {
      CsvEntityMappingBean merged = new CsvEntityMappingBean(bean.getType());
      mergeBeans(existingBean, merged);
      mergeBeans(bean, merged);
      bean = merged;
    }
    _mappingBeansByClass.put(bean.getType(), bean);
  }

  protected void applyCsvFieldsAnnotationToBean(
      Class<?> entityClass, CsvEntityMappingBean entityBean) {

    CsvFields csvFields = entityClass.getAnnotation(CsvFields.class);

    if (csvFields != null) {
      entityBean.setFilename(csvFields.filename());
      if (!csvFields.prefix().equals("")) entityBean.setPrefix(csvFields.prefix());
      if (csvFields.required()) entityBean.setRequired(csvFields.required());
      String[] fieldsInOrder = csvFields.fieldOrder();
      if (fieldsInOrder.length != 0) {
        for (String fieldInOrder : fieldsInOrder) entityBean.addFieldInOrder(fieldInOrder);
      }
      if (csvFields.fieldNameConvention() != CsvFieldNameConvention.UNSPECIFIED) {
        entityBean.setFieldNameConvention(csvFields.fieldNameConvention());
      }
    }
  }

  protected void applyCsvFieldAnnotationToBean(Field field, CsvFieldMappingBean fieldBean) {
    CsvField csvField = field.getAnnotation(CsvField.class);

    if (csvField != null) {
      if (!csvField.name().equals("")) fieldBean.setName(csvField.name());
      if (csvField.ignore()) fieldBean.setIgnore(csvField.ignore());
      if (csvField.optional()) fieldBean.setOptional(csvField.optional());
      if (csvField.alwaysIncludeInOutput()) {
        fieldBean.setAlwaysIncludeInOutput(csvField.alwaysIncludeInOutput());
      }
      if (csvField.order() != Integer.MAX_VALUE) fieldBean.setOrder(csvField.order());
      if (!csvField.defaultValue().isEmpty()) {
        fieldBean.setDefaultValue(csvField.defaultValue());
      }

      Class<? extends FieldMappingFactory> mapping = csvField.mapping();
      if (!mapping.equals(FieldMappingFactory.class)) {
        try {
          FieldMappingFactory factory = mapping.getDeclaredConstructor().newInstance();
          fieldBean.setMapping(factory);
        } catch (Exception ex) {
          throw new EntityInstantiationException(mapping, ex);
        }
      }
    }
  }

  /****
   * Private Methods
   ****/

  private void initialize() {
    if (!_initialized) {
      processBeanDefinitions();
      _initialized = true;
    }
  }

  private void mergeBeans(CsvEntityMappingBean source, CsvEntityMappingBean target) {
    if (source.isFilenameSet()) target.setFilename(source.getFilename());
    if (source.isPrefixSet()) target.setPrefix(source.getPrefix());
    if (source.isRequiredSet()) target.setRequired(source.isRequired());
    if (source.isAutoGenerateSchemaSet())
      target.setAutoGenerateSchema(source.isAutoGenerateSchema());

    List<String> fieldsInOrder = source.getFieldsInOrder();
    if (!fieldsInOrder.isEmpty()) target.setFieldsInOrder(fieldsInOrder);

    for (FieldMapping mapping : source.getAdditionalFieldMappings())
      target.addAdditionalFieldMapping(mapping);

    Map<Field, CsvFieldMappingBean> sourceFields = source.getFields();
    Map<Field, CsvFieldMappingBean> targetFields = target.getFields();
    for (Map.Entry<Field, CsvFieldMappingBean> entry : sourceFields.entrySet()) {
      Field sourceField = entry.getKey();
      CsvFieldMappingBean sourceFieldBean = entry.getValue();
      CsvFieldMappingBean targetFieldBean = targetFields.get(sourceField);
      if (targetFieldBean == null) targetFieldBean = sourceFieldBean;
      else mergeFields(sourceFieldBean, targetFieldBean);
      targetFields.put(sourceField, targetFieldBean);
    }
  }

  private void mergeFields(CsvFieldMappingBean source, CsvFieldMappingBean target) {
    if (source.isNameSet()) target.setName(source.getName());
    if (source.isIgnoreSet()) target.setIgnore(target.isIgnore());
    if (source.isMappingSet()) target.setMapping(source.getMapping());
    if (source.isOptionalSet()) target.setOptional(source.isOptional());
    if (source.isAlwaysIncludeInOutput()) {
      target.setAlwaysIncludeInOutput(source.isAlwaysIncludeInOutput());
    }
    if (source.isOrderSet()) target.setOrder(source.getOrder());
    if (source.getDefaultValue() != null) {
      target.setDefaultValue(source.getDefaultValue());
    }
  }

  private EntitySchema createSchemaForEntityClass(Class<?> entityClass) {
    CsvEntityMappingBean mappingBean = getMappingBeanForEntityType(entityClass);

    String name = getEntityClassAsEntityName(entityClass);
    if (mappingBean.isFilenameSet()) name = mappingBean.getFilename();

    boolean required = false;
    if (mappingBean.isRequiredSet()) required = mappingBean.isRequired();

    EntitySchema schema = new EntitySchema(entityClass, name, required);

    fillSchemaForEntityClass(entityClass, mappingBean, schema);

    List<String> fieldsInOrder = mappingBean.getFieldsInOrder();
    if (!fieldsInOrder.isEmpty()) schema.setFieldsInOrder(fieldsInOrder);

    List<Class<?>> extensionTypes = _extensionsByClass.get(entityClass);
    if (extensionTypes != null) {
      for (Class<?> extensionType : extensionTypes) {
        CsvEntityMappingBean extensionMappingBean = getMappingBeanForEntityType(extensionType);
        ExtensionEntitySchema extensionSchema = new ExtensionEntitySchema(extensionType);
        fillSchemaForEntityClass(extensionType, extensionMappingBean, extensionSchema);
        schema.addExtension(extensionSchema);
      }
    }

    return schema;
  }

  private CsvEntityMappingBean getMappingBeanForEntityType(Class<?> entityClass) {
    CsvEntityMappingBean mappingBean = _mappingBeansByClass.get(entityClass);
    if (mappingBean == null) {
      mappingBean = new CsvEntityMappingBean(entityClass);
      applyCsvFieldsAnnotationToBean(entityClass, mappingBean);
    }
    return mappingBean;
  }

  private void fillSchemaForEntityClass(
      Class<?> entityClass, CsvEntityMappingBean mappingBean, BaseEntitySchema schema) {
    Map<Field, CsvFieldMappingBean> existingFieldBeans = mappingBean.getFields();
    List<FieldMapping> fieldMappings = new ArrayList<FieldMapping>();

    String prefix = "";
    if (mappingBean.isPrefixSet()) prefix = mappingBean.getPrefix();

    CsvFieldNameConvention fieldNameConvention = CsvFieldNameConvention.UNSPECIFIED;
    if (mappingBean.getFieldNameConvention() != null)
      fieldNameConvention = mappingBean.getFieldNameConvention();
    if (fieldNameConvention == CsvFieldNameConvention.UNSPECIFIED)
      fieldNameConvention = CsvFieldNameConvention.UNDERSCORE;

    boolean autoGenerateSchema = true;
    if (mappingBean.isAutoGenerateSchemaSet())
      autoGenerateSchema = mappingBean.isAutoGenerateSchema();

    if (autoGenerateSchema) {
      Set<Field> remainingFields = new LinkedHashSet<Field>();
      for (Field field : entityClass.getDeclaredFields()) {
        remainingFields.add(field);
      }
      // We add known fields first so that we can maintain field order.
      for (Map.Entry<Field, CsvFieldMappingBean> entry : existingFieldBeans.entrySet()) {
        Field field = entry.getKey();
        if (!remainingFields.remove(field)) {
          _log.warn("field found in mapping but not in class: " + field);
          continue;
        }
        addFieldMapping(
            entityClass, prefix, fieldNameConvention, field, entry.getValue(), fieldMappings);
      }
      // We add any remaining fields next.
      for (Field field : remainingFields) {
        CsvFieldMappingBean fieldMappingBean = new CsvFieldMappingBean(field);
        applyCsvFieldAnnotationToBean(field, fieldMappingBean);

        // Ignore static or final fields
        boolean ignore = (field.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) != 0;
        if (ignore) fieldMappingBean.setIgnore(ignore);

        addFieldMapping(
            entityClass, prefix, fieldNameConvention, field, fieldMappingBean, fieldMappings);
      }
    }

    for (FieldMapping fieldMapping : mappingBean.getAdditionalFieldMappings())
      fieldMappings.add(fieldMapping);

    List<FieldMapping> sortableMappings = new ArrayList<FieldMapping>();
    List<FieldMapping> unsortableMappings = new ArrayList<FieldMapping>();
    for (FieldMapping fieldMapping : fieldMappings) {
      if (fieldMapping.getOrder() == Integer.MAX_VALUE) {
        unsortableMappings.add(fieldMapping);
      } else {
        sortableMappings.add(fieldMapping);
      }
    }
    if (!sortableMappings.isEmpty()) {
      Collections.sort(sortableMappings, new FieldMappingComparator());
      fieldMappings.clear();
      fieldMappings.addAll(sortableMappings);
      fieldMappings.addAll(unsortableMappings);
    }

    for (FieldMapping mapping : fieldMappings) schema.addField(mapping);

    List<EntityValidator> validators = new ArrayList<EntityValidator>();
    validators.addAll(mappingBean.getValidators());

    Collections.sort(validators, new ValidatorComparator());

    for (EntityValidator validator : validators) schema.addValidator(validator);
  }

  private void addFieldMapping(
      Class<?> entityClass,
      String prefix,
      CsvFieldNameConvention fieldNameConvention,
      Field field,
      CsvFieldMappingBean fieldMappingBean,
      List<FieldMapping> fieldMappings) {
    if (fieldMappingBean.isIgnoreSet() && fieldMappingBean.isIgnore()) return;
    FieldMapping mapping =
        getFieldMapping(entityClass, field, fieldMappingBean, prefix, fieldNameConvention);
    fieldMappings.add(mapping);
  }

  private FieldMapping getFieldMapping(
      Class<?> entityClass,
      Field field,
      CsvFieldMappingBean fieldMappingBean,
      String prefix,
      CsvFieldNameConvention fieldNameConvention) {

    FieldMapping mapping = null;

    String objFieldName = field.getName();
    Class<?> objFieldType = field.getType();

    String csvFieldName =
        prefix + getObjectFieldNameAsCSVFieldName(objFieldName, fieldNameConvention);
    boolean required = true;

    if (fieldMappingBean.isOptionalSet()) required = !fieldMappingBean.isOptional();

    if (fieldMappingBean.isNameSet()) csvFieldName = fieldMappingBean.getName();

    if (fieldMappingBean.isMappingSet()) {
      FieldMappingFactory factory = fieldMappingBean.getMapping();
      mapping =
          factory.createFieldMapping(
              this, entityClass, csvFieldName, objFieldName, objFieldType, required);
    }

    if (mapping == null) {
      DefaultFieldMapping m =
          new DefaultFieldMapping(entityClass, csvFieldName, objFieldName, objFieldType, required);

      mapping = m;
    }

    if (mapping instanceof AbstractFieldMapping fm) {
      if (fieldMappingBean.isOrderSet()) fm.setOrder(fieldMappingBean.getOrder());
      if (fieldMappingBean.isAlwaysIncludeInOutputSet()) {
        fm.setAlwaysIncludeInOutput(fieldMappingBean.isAlwaysIncludeInOutput());
      }
      if (fieldMappingBean.getDefaultValue() != null) {
        fm.setDefaultValue(fieldMappingBean.getDefaultValue());
      }

      try {
        String name = field.getName();
        String isFieldSet =
            "is" + Character.toUpperCase(name.charAt(0)) + name.substring(1) + "Set";

        Method method = entityClass.getMethod(isFieldSet);
        if (method != null
            && (method.getReturnType() == Boolean.class
                || method.getReturnType() == Boolean.TYPE)) {
          fm.setIsSetMethod(method);
        }
      } catch (Exception ex) {
        // We ignore this
      }
    }

    return mapping;
  }

  private String getEntityClassAsEntityName(Class<?> entityClass) {
    String name = entityClass.getName();
    int index = name.lastIndexOf(".");
    if (index != -1) name = name.substring(index + 1);
    return name;
  }

  private String getObjectFieldNameAsCSVFieldName(
      String fieldName, CsvFieldNameConvention fieldNameConvention) {

    if (fieldNameConvention == CsvFieldNameConvention.CAMEL_CASE) return fieldName;

    if (fieldNameConvention == CsvFieldNameConvention.CAPITALIZED_CAMEL_CASE) {
      return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    StringBuilder b = new StringBuilder();
    boolean wasUpperCase = false;

    for (int i = 0; i < fieldName.length(); i++) {
      char c = fieldName.charAt(i);
      boolean isUpperCase = Character.isUpperCase(c);
      if (isUpperCase) c = Character.toLowerCase(c);
      if (isUpperCase && !wasUpperCase) b.append('_');
      b.append(c);
      wasUpperCase = isUpperCase;
    }

    return b.toString();
  }

  private static class FieldMappingComparator implements Comparator<FieldMapping> {
    public int compare(FieldMapping o1, FieldMapping o2) {
      return o1.getOrder() - o2.getOrder();
    }
  }

  private static class ValidatorComparator implements Comparator<EntityValidator> {
    public int compare(EntityValidator o1, EntityValidator o2) {
      return o1.getOrder() - o2.getOrder();
    }
  }
}

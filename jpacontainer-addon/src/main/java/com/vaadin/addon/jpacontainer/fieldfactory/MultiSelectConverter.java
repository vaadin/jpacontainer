/**
 * Copyright 2009-2013 Oy Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vaadin.addon.jpacontainer.fieldfactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.ManyToMany;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.addon.jpacontainer.metadata.EntityClassMetadata;
import com.vaadin.addon.jpacontainer.metadata.MetadataFactory;
import com.vaadin.addon.jpacontainer.metadata.PropertyMetadata;
import com.vaadin.v7.data.util.converter.Converter;
import com.vaadin.v7.ui.AbstractSelect;

public class MultiSelectConverter<T> implements
        Converter<Collection<Object>, Collection<T>> {

    private final AbstractSelect select;
    private Boolean owningSide;
    private String mappedBy;

    public MultiSelectConverter(AbstractSelect select) {
        this.select = select;
    }

    @SuppressWarnings("unchecked")
    private EntityContainer<T> getContainer() {
        return (EntityContainer<T>) select.getContainerDataSource();
    }

    @Override
    public Collection<Object> convertToPresentation(Collection<T> value,
            Class<? extends Collection<Object>> targetType, Locale locale)
            throws com.vaadin.v7.data.util.converter.Converter.ConversionException {
        // Value here is a collection of entities, should be transformed to a
        // collection (set) of identifier
        // TODO, consider creating a cached value

        if (value == null || value.isEmpty()) {
            try {
                return createNewCollectionForType(getPropertyDataSource()
                        .getType());
            } catch (Exception e) {
                throw new ConversionException(e);
            }
        }

        HashSet<Object> identifiers = new HashSet<Object>();
        for (T entity : value) {
            Object identifier = getContainer().getEntityProvider()
                    .getIdentifier(entity);
            identifiers.add(identifier);
        }
        return identifiers;
    }

    @Override
    public Collection<T> convertToModel(Collection<Object> value,
            Class<? extends Collection<T>> targetType, Locale locale)
            throws com.vaadin.v7.data.util.converter.Converter.ConversionException {

        // NOTE, this currently works properly only if equals and hashcode
        // methods have been implemented correctly (both depending on identifier
        // of the entity)
        // TODO create a filter that has a workaround for invalid
        // equals/hashCode

        // formattedValue here is a set of identifiers.
        // We will modify the existing collection of entities to contain
        // corresponding entities
        Collection<Object> idset = value;

        Collection<T> modelValue = (Collection<T>) getPropertyDataSource()
                .getValue();

        if (modelValue == null) {
            try {
                modelValue = createNewCollectionForType(getPropertyDataSource()
                        .getType());
            } catch (Exception e) {
                throw new ConversionException(e);
            }
        }

        if (idset == null || idset.isEmpty()) {
            modelValue.clear();
            return modelValue;
        }

        HashSet<T> orphaned = new HashSet<T>(modelValue);

        // Add those that did not exist do not exist already + remove them from
        // orphaned collection
        for (Object id : idset) {
            EntityItem<T> item = getContainer().getItem(id);
            T entity = item.getEntity();
            if (!modelValue.contains(entity)) {
                modelValue.add(entity);
                addBackReference(entity);
            }
            orphaned.remove(entity);
        }

        // remove orphanded
        for (T entity : orphaned) {
            modelValue.remove(entity);
            removeBackReference(entity);
        }

        if (!isOwningSide()) {
            // refresh the item as modifying back references may also have
            // changed the collections, without this we'd get concurrent
            // modification exception.

            // FIXME: when verifying a field using this converter this following
            // line causes a value change event on that field, which causes all
            // kinds of shit which ultimately causes an exception causing the
            // validation to fail with a validation error message.
            // getPropertyDataSource().getItem().refresh();
        }
        return modelValue;
    }

    private EntityItemProperty getPropertyDataSource() {
        return (EntityItemProperty) select.getPropertyDataSource();
    }

    private void removeBackReference(T entity) {
        if (!isOwningSide()) {
            EntityItemProperty itemProperty = getBackReferenceItemProperty(entity);
            Collection c = (Collection) itemProperty.getValue();
            c.remove(getPropertyDataSource().getItem().getEntity());
            itemProperty.setValue(c);
        }
    }

    private EntityItemProperty getBackReferenceItemProperty(T entity) {
        EntityItem item = getContainer().getItem(
                getContainer().getEntityProvider().getIdentifier(entity));
        EntityItemProperty itemProperty = item.getItemProperty(mappedBy);
        return itemProperty;
    }

    private void addBackReference(T entity) {
        if (!isOwningSide()) {
            EntityItemProperty itemProperty = getBackReferenceItemProperty(entity);
            Collection c = (Collection) itemProperty.getValue();
            c.add(getPropertyDataSource().getItem().getEntity());
            itemProperty.setValue(c);
        }
    }

    /**
     * Checks if the manytomany relation is owned by this side of the property.
     * As a side effect detects the name of the owner property if the relation
     * is owned by the other side.
     * 
     * @return false if bidirectional connection and the mapping has a mappedBy
     *         parameter.
     */
    private boolean isOwningSide() {
        if (owningSide == null) {
            Class<?> entityClass = getPropertyDataSource().getItem()
                    .getContainer().getEntityClass();
            EntityClassMetadata<?> entityClassMetadata = MetadataFactory
                    .getInstance().getEntityClassMetadata(entityClass);
            PropertyMetadata property = entityClassMetadata
                    .getProperty(getPropertyDataSource().getPropertyId());
            ManyToMany annotation = property.getAnnotation(ManyToMany.class);
            if (annotation.mappedBy() != null
                    && !annotation.mappedBy().isEmpty()) {
                owningSide = Boolean.FALSE;
                mappedBy = annotation.mappedBy();
                return owningSide;
            }
            owningSide = Boolean.TRUE;
        }
        return owningSide;
    }

    static Collection createNewCollectionForType(Class<?> type)
            throws InstantiationException, IllegalAccessException {
        if (type.isInterface()) {
            if (type == Set.class) {
                return new HashSet();
            } else if (type == List.class) {
                return new ArrayList();
            } else {
                throw new RuntimeException(
                        "Couldn't instantiate a collection for property.");
            }
        } else {
            return (Collection) type.newInstance();
        }
    }

    @Override
    public Class<Collection<T>> getModelType() {
        return getPropertyDataSource().getType();
    }

    @Override
    public Class<Collection<Object>> getPresentationType() {
        return getPropertyDataSource().getType();
    }

}

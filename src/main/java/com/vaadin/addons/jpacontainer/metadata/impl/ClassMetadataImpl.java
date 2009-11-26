/*
 * JPAContainer
 * Copyright (C) 2009 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.metadata.impl;

import com.vaadin.addons.jpacontainer.metadata.ClassMetadata;
import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata;
import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata.AccessType;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.persistence.Version;

/**
 * Default implementation of {@link ClassMetadata}.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public final class ClassMetadataImpl<T> implements ClassMetadata<T> {

    private final String entityName;

    private final Class<T> entityClass;

    private final Map<String, PropertyMetadata> properties;

    private final Collection<PropertyMetadata> idProperties;

    private final PropertyMetadata versionProperty;

    ClassMetadataImpl(String entityName, Class<T> entityClass,
            Map<String, PropertyMetadata> properties,
            Collection<PropertyMetadata> idProperties) {
        this.entityName = entityName;
        this.entityClass = entityClass;
        this.properties = Collections.unmodifiableMap(properties);
        this.idProperties = Collections.unmodifiableCollection(idProperties);
        PropertyMetadata verP = null;
        for (PropertyMetadata pm : properties.values()) {
            if (pm.getAnnotation(Version.class) != null) {
                verP = pm;
                break;
            }
        }
        versionProperty = verP;
    }

    @Override
    public String getEntityName() {
        return entityName;
    }

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    @Override
    public Collection<PropertyMetadata> getMappedProperties() {
        return properties.values();
    }

    @Override
    public PropertyMetadata getMappedProperty(String propertyName) {
        return properties.get(propertyName);
    }

    @Override
    public boolean hasVersionProperty() {
        return versionProperty != null;
    }

    @Override
    public PropertyMetadata getVersionProperty() {
        return versionProperty;
    }

    @Override
    public boolean hasIdentifierProperty() {
        return idProperties.size() == 1;
    }

    @Override
    public PropertyMetadata getIdentifierProperty() {
        return !hasIdentifierProperty() ? null : idProperties.iterator().next();
    }

    @Override
    public boolean hasEmbeddedIdentifier() {
        return idProperties.size() > 1;
    }

    @Override
    public Collection<PropertyMetadata> getEmbeddedIdentifierProperties() {
        if (hasEmbeddedIdentifier()) {
            return idProperties;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Object getPropertyValue(T object,
            PropertyMetadata property) throws IllegalArgumentException {
        try {
            PropertyMetadataImpl prop = (PropertyMetadataImpl) property;
            if (prop.getAccessType() == AccessType.FIELD) {
                prop.field.setAccessible(true);
                return prop.field.get(object);
            } else {
                return prop.getter.invoke(object);
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    "Unsupported PropertyMetadata implementation", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot access the field value",
                    e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Cannot access the getter", e);
        }
    }

    @Override
    public void setPropertyValue(T object, PropertyMetadata property,
            Object value) throws IllegalArgumentException {
        try {
            PropertyMetadataImpl prop = (PropertyMetadataImpl) property;
            if (prop.getAccessType() == AccessType.FIELD) {
                prop.field.setAccessible(true);
                prop.field.set(object, value);
            } else {
                prop.setter.invoke(object, value);
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    "Unsupported PropertyMetadata implementation", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot access the field value",
                    e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Cannot access the setter", e);
        }
    }
}

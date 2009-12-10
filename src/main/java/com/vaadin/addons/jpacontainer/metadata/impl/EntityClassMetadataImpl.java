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

import com.vaadin.addons.jpacontainer.metadata.EntityClassMetadata;
import com.vaadin.addons.jpacontainer.metadata.NestedPropertyMetadata;
import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * Default implementation of {@link EntityClassMetadata}.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public final class EntityClassMetadataImpl extends ClassMetadataImpl
        implements EntityClassMetadata {

    private final String entityName;

    private PropertyMetadata versionProperty;

    private PropertyMetadata identifierProperty;

    private Collection<NestedPropertyMetadata> embeddedIdProperties;

    public EntityClassMetadataImpl(Class<?> mappedClass, String entityName) {
        super(mappedClass);
        assert entityName != null : "entityName must not be null";
        this.entityName = entityName;
    }

    void setIdentifierProperty(String name) {
        assert name != null : "name must not be null";
        identifierProperty = getMappedProperty(name);
        /*
         * Do some simple validation
         */
        if (identifierProperty == null) {
            throw new IllegalArgumentException("No such property: " + name);
        } else if (identifierProperty.getAnnotation(Id.class) == identifierProperty.
                getAnnotation(EmbeddedId.class)) {
            identifierProperty = null;
            throw new IllegalArgumentException("Property " + name
                    + " does not have the Id nor the EmbeddedId annotation");
        } else if (identifierProperty.isEmbedded() && identifierProperty.
                getAnnotation(
                EmbeddedId.class) == null) {
            identifierProperty = null;
            throw new IllegalArgumentException(
                    "Property " + name
                    + " is embededed but does not have the EmbeddedId annotation");
        }

        if (identifierProperty.isEmbedded()) {
            embeddedIdProperties = new HashSet<NestedPropertyMetadata>();
            for (PropertyMetadata pm : getMappedProperties()) {
                if (pm instanceof NestedPropertyMetadata && ((NestedPropertyMetadata) pm).
                        getParentProperty() == identifierProperty) {
                    embeddedIdProperties.add((NestedPropertyMetadata) pm);
                }
            }
            embeddedIdProperties = Collections.unmodifiableCollection(
                    embeddedIdProperties);
        } else {
            embeddedIdProperties = Collections.emptyList();
        }
    }

    void setVersionProperty(String name) {
        assert name != null : "name must not be null";
        versionProperty = getMappedProperty(name);
        /*
         * Do some simple validation
         */
        if (versionProperty == null) {
            throw new IllegalArgumentException("No such property: " + name);
        } else if (versionProperty.getAnnotation(Version.class) == null) {
            versionProperty = null;
            throw new IllegalArgumentException("Property " + name
                    + " does not have the Version annotation");
        }
    }

    @Override
    public String getEntityName() {
        return entityName;
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
        return identifierProperty != null;
    }

    @Override
    public PropertyMetadata getIdentifierProperty() {
        return identifierProperty;
    }

    @Override
    public boolean hasEmbeddedIdentifier() {
        return hasIdentifierProperty() && identifierProperty.isEmbedded();
    }

    @Override
    public Collection<NestedPropertyMetadata> getEmbeddedIdentifierProperties() {
        return embeddedIdProperties;
    }
}

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
import com.vaadin.addons.jpacontainer.metadata.NestedPropertyMetadata;
import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata;
import java.lang.annotation.Annotation;

/**
 * Default implementation of {@link NestedPropertyMetadata}.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public final class NestedPropertyMetadataImpl implements NestedPropertyMetadata {

    private final PropertyMetadata actualProperty;

    private final PropertyMetadata parentProperty;

    NestedPropertyMetadataImpl(PropertyMetadata actualProperty,
            PropertyMetadata parentProperty) {
        assert actualProperty != null : "actualProperty must not be null";
        assert parentProperty != null : "parentProperty must not be null";
        assert parentProperty != actualProperty :
                "parentProperty and actualProperty cannot be the same";
        this.actualProperty = actualProperty;
        this.parentProperty = parentProperty;
    }

    @Override
    public PropertyMetadata getActualProperty() {
        return actualProperty;
    }

    @Override
    public PropertyMetadata getParentProperty() {
        return parentProperty;
    }

    @Override
    public AccessType getAccessType() {
        return actualProperty.getAccessType();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return actualProperty.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return actualProperty.getAnnotations();
    }

    @Override
    public String getName() {
        return parentProperty.getName() + "." + actualProperty.getName();
    }

    @Override
    public ClassMetadata getOwner() {
        return parentProperty.getOwner();
    }

    @Override
    public Class<?> getType() {
        return actualProperty.getType();
    }

    @Override
    public ClassMetadata getTypeMetadata() {
        return actualProperty.getTypeMetadata();
    }

    @Override
    public boolean isCollection() {
        return actualProperty.isCollection();
    }

    @Override
    public boolean isEmbedded() {
        return actualProperty.isEmbedded();
    }

    @Override
    public boolean isReference() {
        return actualProperty.isReference();
    }
}

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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.metadata;

import java.io.Serializable;
import java.lang.annotation.Annotation;

/**
 * Interface defining the metadata of a persistent property. Persistent
 * properties can be embedded. For example, if the entity class has an
 * embedded property named <code>address</code> and the address class has
 * a property named <code>street</code>, the metadata for the entity class
 * will include a property named <code>address.street</code> which has the
 * <code>embedded</code> flag set to true.
 *
 * @author Petter Holmström (IT Mill)
 */
public interface PropertyMetadata extends Serializable {

    // TODO Improve the documentation
    /**
     * The name of the property.
     */
    public String getName();

    /**
     * The type of the property.
     */
    public Class<?> getType();

    /**
     * If the property is embedded.
     */
    public boolean isEmbedded();

    /**
     * If the property is a reference or not.
     */
    public boolean isReference();

    /**
     * If the property is a collection or not.
     */
    public boolean isCollection();

    /**
     * How the property is accessed (as a JavaBean property or as a field).
     */
    public AccessType getAccessType();

    /**
     * The annotations of the property.
     */
    public Annotation[] getAnnotations();

    /**
     * TODO Document me!
     *
     * @param <T>
     * @param annotationClass
     * @return
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    /**
     * Enumeration defining the property access types.
     */
    public enum AccessType {

        /**
         * The property is accessed as a JavaBean property using getters and setters.
         */
        METHOD,
        /**
         * The property is accessed directly as a field.
         */
        FIELD
    }
}

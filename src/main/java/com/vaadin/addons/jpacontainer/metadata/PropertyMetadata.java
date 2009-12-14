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
 * This interface represents the metadata of a property. If the property
 * is transient, this is an ordinary JavaBean property consisting of a getter method
 * and optionally a setter method. If the property is persistent, additional
 * information is provided by the {@link PersistentPropertyMetadata} interface.
 *
 * @see ClassMetadata
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface PropertyMetadata extends Serializable {

    /**
     * The name of the property.
     */
    public String getName();

    /**
     * The type of the property.
     */
    public Class<?> getType();

    /**
     * The annotations of the property, if any.
     *
     * @see #getAnnotation(java.lang.Class) 
     */
    public Annotation[] getAnnotations();

    /**
     * Gets the annotation of the specified annotation class, if available.
     *
     * @see #getAnnotations()
     * @see Class#getAnnotation(java.lang.Class) 
     * @param annotationClass the annotation class.
     * @return the annotation, or null if not found.
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    /**
     * Returns whether the property is writable or not. Persistent properties
     * are always writable. Transient properties (i.e. JavaBean properties) are
     * only writable if they have a setter method.
     *
     * @return true if the property is writable, false if it is not.
     */
    public boolean isWritable();
}

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

    /**
     * Gets the name of the property.
     */
    public String getName();

    /**
     * Gets the type of the property.
     */
    public Class<?> getType();

    /**
     * Returns whether this property is embedded or not. An embedded property is not
     * a member of the entity class itself, but a member of a member.
     * <p>
     * An example of an embedded property is <code>address.street</code>. You will
     * not be able to access the <code>street</code> property directly from the entity class.
     * Rather, you have to access the <code>address</code> property first, from which
     * you will then be able to access the <code>street</code> property.
     *
     * @see javax.persistence.Embeddable
     * @see javax.persistence.Embedded
     */
    public boolean isEmbedded();

    /**
     * Returns whether this property is a reference or not.
     * 
     * @see javax.persistence.OneToOne
     * @see javax.persistence.ManyToOne
     */
    public boolean isReference();

    /**
     * Returns whether this property is a collection or not.
     *
     * @see javax.persistence.OneToMany
     * @see javax.persistence.ManyToMany
     */
    public boolean isCollection();

    /**
     * Returns the way the property value is accessed (as a JavaBean property or as a field).
     */
    public AccessType getAccessType();

    /**
     * Gets the annotations of the property. If the access type is {@link AccessType#FIELD}, these
     * are the annotations of the field. If the access type is {@link AccessType#METHOD}, these
     * are the annotations of the getter method.
     *
     * @see #getAnnotation(java.lang.Class) 
     */
    public Annotation[] getAnnotations();

    /**
     * Gets the annotation of the specified annotation class, if available.
     *
     * @see #getAnnotations() tA
     * @see Class#getAnnotation(java.lang.Class) 
     * @param annotationClass the annotation class.
     * @return the annotation, or null if not found.
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    /**
     * Enumeration defining the property access types.
     *
     * @author Petter Holmström (IT Mill)
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

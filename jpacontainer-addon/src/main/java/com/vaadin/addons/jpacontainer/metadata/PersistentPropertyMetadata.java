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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * An extended version of {@link PropertyMetadata} that provides additional
 * information about persistent properties.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class PersistentPropertyMetadata extends PropertyMetadata {

    /**
     * Enumeration defining the property kind.
     *
     * @author Petter Holmström (IT Mill)
     */
    public enum PropertyKind {

        /**
         * The property is embedded.
         * 
         * @see javax.persistence.Embeddable
         * @see javax.persistence.Embedded
         */
        EMBEDDED,
        /**
         * The property is a reference.
         * 
         * @see javax.persistence.OneToOne
         * @see javax.persistence.ManyToOne
         */
        REFERENCE,
        /**
         * The property is a collection.
         * 
         * @see javax.persistence.OneToMany
         * @see javax.persistence.ManyToMany
         */
        COLLECTION,
        /**
         * The property is of a simple datatype.
         */
        SIMPLE
    }

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
    private final PropertyKind propertyKind;
    private final ClassMetadata<?> typeMetadata;
    final Field field;

    /**
     * Creates a new instance of <code>PersistentPropertyMetadata</code>.
     *
     * @param name the name of the property (must not be null).
     * @param type the type of the property (must not be null).
     * @param propertyKind the kind of the property, must be either {@link PropertyKind#COLLECTION} or {@link PropertyKind#SIMPLE}.
     * @param field the field that can be used to access the property (must not be null).
     */
    PersistentPropertyMetadata(String name, Class<?> type, PropertyKind propertyKind, Field field) {
        super(name, type, null, null);
        assert propertyKind == PropertyKind.COLLECTION || propertyKind == PropertyKind.SIMPLE : "propertyKind must be COLLECTION or SIMPLE";
        assert field != null : "field must not be null";
        this.propertyKind = propertyKind;
        this.typeMetadata = null;
        this.field = field;
    }

    /**
     * Creates a new instance of <code>PersistentPropertyMetadata</code>.
     *
     * @param name the name of the property (must not be null).
     * @param type type type of the property (must not be null).
     * @param propertyKind the kind of the property, must be either {@link PropertyKind#COLLECTION} or {@link PropertyKind#SIMPLE}.
     * @param getter the getter method that can be used to read the property value (must not be null).
     * @param setter the setter method that can be used to set the property value (must not be null).
     */
    PersistentPropertyMetadata(String name, Class<?> type, PropertyKind propertyKind, Method getter, Method setter) {
        super(name, type, getter, setter);
        assert propertyKind == PropertyKind.COLLECTION || propertyKind == PropertyKind.SIMPLE : "propertyKind must be COLLECTION or SIMPLE";
        assert getter != null : "getter must not be null";
        assert setter != null : "setter must not be null";
        this.propertyKind = propertyKind;
        this.typeMetadata = null;
        this.field = null;
    }

    /**
     * Creates a new instance of <code>PersistentPropertyMetadata</code>.
     *
     * @param name the name of the property (must not be null).
     * @param type the type metadata of the property (must not be null).
     * @param propertyKind the kind of the property, must be either {@link PropertyKind#REFERENCE} or {@link PropertyKind#EMBEDDED}.
     * @param field the field that can be used to access the property (must not be null).
     */
    PersistentPropertyMetadata(String name, ClassMetadata<?> type, PropertyKind propertyKind, Field field) {
        super(name, type.getMappedClass(), null, null);
        assert type != null : "type must not be null";
        assert propertyKind == PropertyKind.REFERENCE || propertyKind == PropertyKind.EMBEDDED : "propertyKind must be REFERENCE or EMBEDDED";
        assert field != null : "field must not be null";
        this.propertyKind = propertyKind;
        this.typeMetadata = type;
        this.field = field;
    }

    /**
     * Creates a new instance of <code>PersistentPropertyMetadata</code>.
     *
     * @param name the name of the property  (must not be null).
     * @param type the type metadata of the property (must not be null).
     * @param propertyKind the kind of the property, must be either {@link PropertyKind#REFERENCE} or {@link PropertyKind#EMBEDDED}.
     * @param getter the getter method that can be used to read the property value (must not be null).
     * @param setter the setter method that can be used to set the property value (must not be null).
     */
    PersistentPropertyMetadata(String name, ClassMetadata<?> type, PropertyKind propertyKind, Method getter, Method setter) {
        super(name, type.getMappedClass(), getter, setter);
        assert type != null : "type must not be null";
        assert propertyKind == PropertyKind.REFERENCE || propertyKind == PropertyKind.EMBEDDED : "propertyKind must be REFERENCE or EMBEDDED";
        assert getter != null : "getter must not be null";
        assert setter != null : "setter must not be null";
        this.propertyKind = propertyKind;
        this.typeMetadata = type;
        this.field = null;
    }

    /**
     * The metadata of the property type, if it is embedded or a reference.
     * Otherwise, this method returns null.
     *
     * @see #getPropertyKind() 
     */
    public ClassMetadata<?> getTypeMetadata() {
        return typeMetadata;
    }

    /**
     * The kind of the property.
     */
    public PropertyKind getPropertyKind() {
        return propertyKind;
    }

    /**
     * The way the property value is accessed (as a JavaBean property or as a field).
     */
    public AccessType getAccessType() {
        return field != null ? AccessType.FIELD : AccessType.METHOD;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if (field != null) {
            return field.getAnnotation(annotationClass);
        } else {
            return super.getAnnotation(annotationClass);
        }
    }

    @Override
    public Annotation[] getAnnotations() {
        if (field != null) {
            return field.getAnnotations();
        } else {
            return super.getAnnotations();
        }
    }

    /**
     * Persistent properties are always writable.
     * <p>
     * {@inheritDoc }.
     */
    @Override
    public boolean isWritable() {
        return true; //field != null || super.isWritable();
    }
}

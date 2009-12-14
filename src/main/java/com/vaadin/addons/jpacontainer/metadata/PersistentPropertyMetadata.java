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

/**
 * An extended version of {@link PropertyMetadata} that provides additional
 * information about persistent properties.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface PersistentPropertyMetadata extends PropertyMetadata {

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

    /**
     * The metadata of the property type, if it is embedded or a reference.
     * Otherwise, this method returns null.
     *
     * @see #getType()
     * @see #isEmbedded()
     * @see #isReference()
     */
    public ClassMetadata<?> getTypeMetadata();

    /**
     * The way the property value is accessed (as a JavaBean property or as a field).
     */
    public AccessType getAccessType();

    /**
     * Returns whether this property is an embedded property or not.
     *
     * @see javax.persistence.Embeddable
     * @see javax.persistence.Embedded
     * @see #getTypeMetadata()
     */
    public boolean isEmbedded();

    /**
     * Returns whether this property is a reference or not.
     *
     * @see javax.persistence.OneToOne
     * @see javax.persistence.ManyToOne
     * @see #getTypeMetadata()
     */
    public boolean isReference();

    /**
     * Returns whether this property is a collection or not.
     *
     * @see javax.persistence.OneToMany
     * @see javax.persistence.ManyToMany
     */
    public boolean isCollection();
}

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

import javax.persistence.Embeddable;

/**
 * Interface representing a nested property. Nested properties can be used
 * to make it easier to access properties that are owned by {@link Embeddable}
 * classes. Instead of having to first access the {@link Embedded} property of
 * the entity class and then the property, a nested property of the entity class
 * can be accessed directly.
 * <p>
 * For example, if an entity class has an embedded property named <code>address</code>
 * and we want to access the <code>street</code> property, we can access it by
 * using the <code>address.street</code> nested property.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface NestedPropertyMetadata extends PropertyMetadata {

    // TODO The documentation in this interface could be improved.

    /**
     * The actual property that this nested property represents. This property
     * is owned by the embeddable class, whereas the nested property is owned
     * by the class that contains the embedded property.
     */
    public PropertyMetadata getActualProperty();

    /**
     * The embedded property from which this nested property is accessed.
     */
    public PropertyMetadata getParentProperty();
}

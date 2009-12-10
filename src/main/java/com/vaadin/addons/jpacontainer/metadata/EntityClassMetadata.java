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

import java.util.Collection;
import javax.persistence.Entity;

/**
 * An extended version of {@link ClassMetadata} that is designed for
 * classes annotated with the {@link Entity} annotation.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface EntityClassMetadata extends ClassMetadata {

    /**
     * The name of the entity.
     */
    public String getEntityName();

    /**
     * If the entity has a version property or  not.
     *
     * @see #getVersionProperty()
     */
    public boolean hasVersionProperty();

    /**
     * Gets the version property, if it exists.
     *
     * @see #hasVersionProperty() 
     * @return the version property metadata, or null if not available.
     */
    public PropertyMetadata getVersionProperty();

    /**
     * If the entity has an identifier property or not.
     * 
     * @see #getIdentifierProperty()
     * @see #hasEmbeddedIdentifier()
     * @see #getEmbeddedIdentifierProperties() 
     */
    public boolean hasIdentifierProperty();

    /**
     * Gets the identifier property, if it exists. If {@link #hasEmbeddedIdentifier() } returns true,
     * this property is the embedded identifier. The nested properties of the embedded identifier
     * can be accessed using the {@link #getEmbeddedIdentifierProperties() } method.
     *
     * @see #hasIdentifierProperty()
     * @see #hasEmbeddedIdentifier() 
     * @return the identifier property metadata, or null if not available.
     */
    public PropertyMetadata getIdentifierProperty();

    /**
     * If the entity has an embedded identifier. This property cannot be
     * true unless {@link #hasIdentifierProperty() } also returns true.
     *
     * @see #getEmbeddedIdentifierProperties() 
     */
    public boolean hasEmbeddedIdentifier();

    /**
     * The nested properties that constitute the embedded identifier.
     *
     * @see #hasEmbeddedIdentifier() 
     * @return the embedded identifier properties, or an empty collection if
     * the entity does not have an embedded identifier.
     */
    public Collection<NestedPropertyMetadata> getEmbeddedIdentifierProperties();
}

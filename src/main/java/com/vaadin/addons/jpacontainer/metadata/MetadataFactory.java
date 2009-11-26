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
package com.vaadin.addons.jpacontainer.metadata;

/**
 * Interface that defines a factory for {@link ClassMetadata} implementations.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
*/
public interface MetadataFactory {

    /**
     * Extracts the class metadata from <code>entityClass</code>.
     * 
     * @param entityClass the entity class (must not be null).
     * @return the class metadata.
     * @throws IllegalArgumentException if no metadata could be extracted.
     */
    public <T> ClassMetadata<T> getClassMetadata(Class<T> entityClass)
            throws IllegalArgumentException;
}

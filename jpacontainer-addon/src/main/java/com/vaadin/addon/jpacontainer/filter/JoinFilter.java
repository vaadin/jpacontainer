/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addon.jpacontainer.filter;

/**
 * This interface defines a filter that performs a join on a specified property
 * and then applies additional filters on the properties of the joined property.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface JoinFilter extends CompositeFilter {

    /**
     * Gets the property that should be joined.
     * @return the property name (never null).
     */
    public String getJoinProperty();

}

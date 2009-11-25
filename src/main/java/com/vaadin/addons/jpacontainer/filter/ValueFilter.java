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
package com.vaadin.addons.jpacontainer.filter;

/**
 * Interface for property filters that need some kind of value
 * to perform the filtering (e.g. greater than or equals). Note, that
 * the value should never be null. If null values are required,
 * use {@link IsNullFilter} instead.
 *
 * @author Petter Holmström (IT Mill)
 */
public interface ValueFilter extends PropertyFilter {

    /**
     * Gets the filter value.
     * 
     * @return the value (must not be null).
     */
    public Object getValue();
}

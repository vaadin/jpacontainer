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
 * Interface for property filters that need some kind of criterion value. Examples
 * of this kind of filter include equals, greater than and less than.
 *
 * Note, that the value should never be null, as this could lead to strange situations
 * (e.g. a filter checking if a property is greater than or equal to null).
 *
 * @see IsNullFilter
 * @see IsNotNullFilter
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface ValueFilter extends PropertyFilter {

	/**
	 * Gets the filter value.
	 * 
	 * @return the value (never null).
	 */
	public Object getValue();

	/**
	 * Gets the name of the QL parameter name that is used in the generated QL
	 * and that should be replaced with the filter value when the query is
	 * executed. Note, that the name should not begin with a colon, the subclasses
	 * will take care of prepending the colon when they generate QL.
	 * 
	 * @return the QL parameter name (never null).
	 */
	public String getQLParameterName();
}

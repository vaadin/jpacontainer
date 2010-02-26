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
package com.vaadin.addons.jpacontainer.filter;

/**
 * Interface for property filters that need an interval to perform the filtering
 * (e.g. between).
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface IntervalFilter extends PropertyFilter {

	/**
	 * Gets the starting point of the interval.
	 * 
	 * @return the starting point (never null).
	 */
	public Object getStartingPoint();

	/**
	 * Gets the name of the QL parameter name that should be replaced with the
	 * starting point when the query is being executed.
	 * 
	 * @return the QL parameter name (never null)
	 */
	public String getStartingPointQLParameterName();

	/**
	 * Gets the ending point of the interval.
	 * 
	 * @return the ending point (never null).
	 */
	public Object getEndingPoint();

	/**
	 * Gets the name of the QL parameter name that should be replaced with the
	 * ending point when the query is being executed.
	 * 
	 * @return the QL parameter name (never null).
	 */
	public String getEndingPointQLParameterName();

	/**
	 * Returns whether the starting point should be included in the interval or
	 * not.
	 * 
	 * @return true if the starting point should be included, false otherwise.
	 */
	public boolean isStartingPointIncluded();

	/**
	 * Returns whether the ending point should be included in the interval or
	 * not.
	 * 
	 * @return true if the ending point should be included, false otherwise.
	 */
	public boolean isEndingPointIncluded();
}

/*
 * JPAContainer
 * Copyright (C) 2010-2011 Oy Vaadin Ltd
 *
 * This program is available both under Commercial Vaadin Add-On
 * License 2.0 (CVALv2) and under GNU Affero General Public License (version
 * 3 or later) at your option.
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and CVALv2 along with this program.  If not, see
 * <http://www.gnu.org/licenses/> and <http://vaadin.com/license/cval-2.0>.
 */
package com.vaadin.addon.jpacontainer.filter;

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

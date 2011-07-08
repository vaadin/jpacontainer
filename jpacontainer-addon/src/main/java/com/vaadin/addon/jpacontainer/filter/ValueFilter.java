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

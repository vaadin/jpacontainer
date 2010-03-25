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
 * Filter that compares the filtered (String)-property to the filter value using
 * a binary comparison operator. When using the <code>like</code> operator, the
 * precent-sign (%) may be used as wildcard.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class StringComparisonFilter extends AbstractStringFilter {

	private static final long serialVersionUID = 1867603856077373807L;

	private String operator;

	protected StringComparisonFilter(Object propertyId, String value,
			boolean caseSensitive, String operator) {
		super(propertyId, caseSensitive ? value : value.toUpperCase(), caseSensitive);
		assert operator != null : "operator must not be null";
		this.operator = operator;
	}

	/**
	 * Gets the operator that is used for comparison (=, &lt;, &gt;, &lt;=, &gt;=, LIKE).
	 */
	public String getOperator() {
		return operator;
	}

	public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor) {
		String s;
		if (isCaseSensitive()) {
			s = "(%s %s :%s)";
		} else {
			s = "(upper(%s) %s :%s)";
		}
		return String.format(s,
				propertyIdPreprocessor.process(getPropertyId()), operator,
				getQLParameterName());
	}
}

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
 * Filter that matches the items to a value using a binary comparison operator
 * <code>(=, >=, <=, <, >)</code>.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class ComparisonFilter extends AbstractValueFilter {

	private static final long serialVersionUID = -2350703732907111829L;
	private String operator;

	protected ComparisonFilter(Object propertyId, Object value, String operator) {
		super(propertyId, value);
		assert operator != null : "operator must not be null";
		this.operator = operator;
	}

	/**
	 * Gets the operator that is used for comparison.
	 */
	public String getOperator() {
		return operator;
	}

	public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor) {
		return String.format("(%s %s :%s)", propertyIdPreprocessor
				.process(getPropertyId()), operator, getQLParameterName());
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj)
				&& ((ComparisonFilter) obj).operator.equals(operator);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + 7 * operator.hashCode();
	}
}

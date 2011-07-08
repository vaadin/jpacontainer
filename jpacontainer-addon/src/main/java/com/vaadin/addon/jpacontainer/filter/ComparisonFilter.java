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

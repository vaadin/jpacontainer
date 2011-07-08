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

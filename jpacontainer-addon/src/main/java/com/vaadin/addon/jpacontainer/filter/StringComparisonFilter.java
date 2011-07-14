/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

/**
 * Filter that compares the filtered (String)-property to the filter value using
 * a binary comparison operator. When using the <code>like</code> operator, the
 * precent-sign (%) may be used as wildcard.
 * 
 * @author Petter Holmström (Vaadin Ltd)
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

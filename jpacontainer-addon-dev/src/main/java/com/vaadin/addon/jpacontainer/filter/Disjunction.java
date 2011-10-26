/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

import com.vaadin.addon.jpacontainer.Filter;
import java.util.Iterator;
import java.util.List;

/**
 * A filter that groups other filters together in a single disjunction (A or B
 * or C...).
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class Disjunction extends AbstractJunction {

	private static final long serialVersionUID = 6790289192146486002L;

	protected Disjunction(Filter[] filters) {
		super(filters);
	}

	protected Disjunction(List<Filter> filters) {
		super(filters);
	}

	public String toQLString() {
		return toQLString(PropertyIdPreprocessor.DEFAULT);
	}

	public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor) {
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		for (Iterator<Filter> it = getFilters().iterator(); it.hasNext();) {
			sb.append(it.next().toQLString(propertyIdPreprocessor));
			if (it.hasNext()) {
				sb.append(" or ");
			}
		}
		sb.append(")");
		return sb.toString();
	}
}

/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

import com.vaadin.addon.jpacontainer.Filter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Filter that negates another filter.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class Negation implements CompositeFilter {

	private static final long serialVersionUID = -8799310376244240397L;
	private Filter filter;
	private List<Filter> filterList = new LinkedList<Filter>();

	protected Negation(Filter filter) {
		assert filter != null : "filter must not be null";
		this.filter = filter;
		this.filterList.add(filter);
		this.filterList = Collections.unmodifiableList(this.filterList);
	}

	public String toQLString() {
		return toQLString(PropertyIdPreprocessor.DEFAULT);
	}

	public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor) {
		return String.format("(not %s)", filter
				.toQLString(propertyIdPreprocessor));
	}

	public List<Filter> getFilters() {
		return filterList;
	}

	@Override
	public boolean equals(Object obj) {
		return obj.getClass() == getClass()
				&& ((Negation) obj).filter.equals(this.filter);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() + 7 * filter.hashCode();
	}
}

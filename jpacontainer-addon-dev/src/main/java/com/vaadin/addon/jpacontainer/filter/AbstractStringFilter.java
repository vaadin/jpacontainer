/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

/**
 * Abstract base class for filters that filter string properties.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public abstract class AbstractStringFilter extends AbstractValueFilter {

	private static final long serialVersionUID = -4588708261107069904L;

	private boolean caseSensitive;

	protected AbstractStringFilter(Object propertyId, String value,
			boolean caseSensitive) {
		super(propertyId, value);
		this.caseSensitive = caseSensitive;
	}

	/**
	 * Returns whether the filter should be case sensitive or not.
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj)
				&& ((AbstractStringFilter) obj).caseSensitive == caseSensitive;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + 7 * new Boolean(caseSensitive).hashCode();
	}
}

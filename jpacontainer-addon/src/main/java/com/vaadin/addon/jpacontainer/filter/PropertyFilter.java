/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

import com.vaadin.addon.jpacontainer.Filter;

/**
 * Interface to be implemented by filters that are applied to a single property.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public interface PropertyFilter extends Filter {

	/**
	 * Gets the ID of the property that this filter is applied to.
	 * 
	 * @return the property ID (never null).
	 */
	public Object getPropertyId();
}

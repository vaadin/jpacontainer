/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

import java.util.List;

import com.vaadin.addon.jpacontainer.Filter;

/**
 * Interface to be implemented by all filters that are composed of at least one
 * other filter.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public interface CompositeFilter extends Filter {

    /**
     * Gets a list of all the filters included in this composite filter.
     * 
     * @return an unmodifiable list of filters (never null).
     */
    public List<Filter> getFilters();
}

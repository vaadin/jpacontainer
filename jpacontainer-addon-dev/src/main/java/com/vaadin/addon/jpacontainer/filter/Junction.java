/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

import com.vaadin.addon.jpacontainer.Filter;

/**
 * A filter that groups other filters together using some associative logical
 * operator.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public interface Junction extends CompositeFilter {

    /**
     * Adds <code>filter</code> to the end of the list of filters. If it has
     * already been added, it will be added again.
     * 
     * @param filter
     *            the filter to add (must not be null).
     * @return <code>this</code>, to allow chaining.
     */
    public Junction add(Filter filter);

    /**
     * Removes <code>filter</code> from the list of filters. If it has been
     * added more than once, only the first occurence will be remoed. If it has
     * never been added, nothing happens.
     * 
     * @param filter
     *            the filter to remove (must not be null).
     * @return <code>this</code>, to allow chaining.
     */
    public Junction remove(Filter filter);
}

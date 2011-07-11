/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

/**
 * This interface defines a filter that performs a join on a specified property
 * and then applies additional filters on the properties of the joined property.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface JoinFilter extends CompositeFilter {

    /**
     * Gets the property that should be joined.
     * @return the property name (never null).
     */
    public String getJoinProperty();

}

/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

/**
 * Interface for property filters that need an interval to perform the filtering
 * (e.g. between).
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public interface IntervalFilter extends PropertyFilter {

	/**
	 * Gets the starting point of the interval.
	 * 
	 * @return the starting point (never null).
	 */
	public Object getStartingPoint();

	/**
	 * Gets the name of the QL parameter name that should be replaced with the
	 * starting point when the query is being executed.
	 * 
	 * @return the QL parameter name (never null)
	 */
	public String getStartingPointQLParameterName();

	/**
	 * Gets the ending point of the interval.
	 * 
	 * @return the ending point (never null).
	 */
	public Object getEndingPoint();

	/**
	 * Gets the name of the QL parameter name that should be replaced with the
	 * ending point when the query is being executed.
	 * 
	 * @return the QL parameter name (never null).
	 */
	public String getEndingPointQLParameterName();

	/**
	 * Returns whether the starting point should be included in the interval or
	 * not.
	 * 
	 * @return true if the starting point should be included, false otherwise.
	 */
	public boolean isStartingPointIncluded();

	/**
	 * Returns whether the ending point should be included in the interval or
	 * not.
	 * 
	 * @return true if the ending point should be included, false otherwise.
	 */
	public boolean isEndingPointIncluded();
}

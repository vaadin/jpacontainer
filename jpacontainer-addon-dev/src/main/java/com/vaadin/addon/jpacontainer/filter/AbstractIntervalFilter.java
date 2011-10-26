/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

import java.util.Random;

/**
 * Abstract implementation of {@link IntervalFilter}.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public abstract class AbstractIntervalFilter extends AbstractPropertyFilter
		implements IntervalFilter {

	private static final long serialVersionUID = 2153303161748313563L;
	private Object endingPoint;
	private boolean endingPointIncluded;
	private String endingPointQLParameter;
	private Object startingPoint;
	private boolean startingPointIncluded;
	private String startingPointQLParameter;

	protected AbstractIntervalFilter(Object propertyId, Object startingPoint,
			boolean startingPointIncluded, Object endingPoint,
			boolean endingPointIncluded) {
		super(propertyId);
		assert startingPoint != null : "startingPoint must not be null";
		assert endingPoint != null : "endingPoint must not be null";
		int rnd = Math.abs(new Random().nextInt());
		this.startingPoint = startingPoint;
		this.startingPointIncluded = startingPointIncluded;
		this.startingPointQLParameter = propertyId.toString() + "_start" + rnd;
		this.endingPoint = endingPoint;
		this.endingPointIncluded = endingPointIncluded;
		this.endingPointQLParameter = propertyId.toString() + "_end" + rnd;
	}

	public Object getEndingPoint() {
		return endingPoint;
	}

	public String getEndingPointQLParameterName() {
		return endingPointQLParameter;
	}

	public Object getStartingPoint() {
		return startingPoint;
	}

	public String getStartingPointQLParameterName() {
		return startingPointQLParameter;
	}

	public boolean isEndingPointIncluded() {
		return endingPointIncluded;
	}

	public boolean isStartingPointIncluded() {
		return startingPointIncluded;
	}

	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			AbstractIntervalFilter o = (AbstractIntervalFilter) obj;
			return o.endingPointIncluded == endingPointIncluded
					&& o.startingPointIncluded == startingPointIncluded
					&& o.endingPoint.equals(endingPoint)
					&& o.startingPoint.equals(startingPoint);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + 7
				* new Boolean(endingPointIncluded).hashCode() + 11
				* new Boolean(startingPointIncluded).hashCode() + 3
				* endingPoint.hashCode() + 5 * startingPoint.hashCode();
	}
}

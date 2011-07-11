/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

/**
 * Filter that includes all items for which the filtered property is inside a
 * specified interval.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class BetweenFilter extends AbstractIntervalFilter {

	private static final long serialVersionUID = 837583599358231719L;

	protected BetweenFilter(Object propertyId, Object startingPoint,
			boolean startingPointIncluded, Object endingPoint,
			boolean endingPointIncluded) {
		super(propertyId, startingPoint, startingPointIncluded, endingPoint,
				endingPointIncluded);
	}

	public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor) {
		String propId = propertyIdPreprocessor.process(getPropertyId());
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		sb.append(propId);
		if (isStartingPointIncluded()) {
			sb.append(" >= ");
		} else {
			sb.append(" > ");
		}
		sb.append(":");
		sb.append(getStartingPointQLParameterName());
		sb.append(" and ");
		sb.append(propId);
		if (isEndingPointIncluded()) {
			sb.append(" <= ");
		} else {
			sb.append(" < ");
		}
		sb.append(":");
		sb.append(getEndingPointQLParameterName());
		sb.append(")");
		return sb.toString();
	}
}

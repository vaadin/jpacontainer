/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

/**
 * Filter that includes all items for which the filtered property is outside a
 * specified interval.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class OutsideFilter extends AbstractIntervalFilter {

    private static final long serialVersionUID = -1307015635957630428L;

    protected OutsideFilter(Object propertyId, Object startingPoint,
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
            sb.append(" <= ");
        } else {
            sb.append(" < ");
        }
        sb.append(":");
        sb.append(getStartingPointQLParameterName());
        sb.append(" or ");
        sb.append(propId);
        if (isEndingPointIncluded()) {
            sb.append(" >= ");
        } else {
            sb.append(" > ");
        }
        sb.append(":");
        sb.append(getEndingPointQLParameterName());
        sb.append(")");
        return sb.toString();
    }
}

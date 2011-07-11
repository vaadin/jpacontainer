/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

/**
 * Filter that includes all items for which the filtered property is null.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class IsNullFilter extends AbstractPropertyFilter {

	private static final long serialVersionUID = 519303493666084738L;

	protected IsNullFilter(Object propertyId) {
		super(propertyId);
	}

	public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor) {
		return String.format("(%s is null)", propertyIdPreprocessor
				.process(getPropertyId()));
	}
}

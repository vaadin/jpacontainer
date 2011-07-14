/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

/**
 * Filter that includes all items for which the filtered property is not null.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class IsNotNullFilter extends AbstractPropertyFilter {

	private static final long serialVersionUID = 5876273494734509375L;

	protected IsNotNullFilter(Object propertyId) {
		super(propertyId);
	}

	public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor) {
		return String.format("(%s is not null)", propertyIdPreprocessor
				.process(getPropertyId()));
	}
}

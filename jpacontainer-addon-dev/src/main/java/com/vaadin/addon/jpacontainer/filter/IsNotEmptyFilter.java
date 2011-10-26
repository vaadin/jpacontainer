/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

/**
 * Filter that includes all items for which the filtered property is not empty.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class IsNotEmptyFilter extends AbstractPropertyFilter {

	private static final long serialVersionUID = 8713324928561145012L;

	protected IsNotEmptyFilter(Object propertyId) {
		super(propertyId);
	}

	public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor) {
		return String.format("(%s is not empty)", propertyIdPreprocessor
				.process(getPropertyId()));
	}
}

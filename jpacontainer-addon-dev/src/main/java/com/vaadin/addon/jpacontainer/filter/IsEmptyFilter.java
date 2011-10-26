/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

/**
 * Filter that includes all items for which the filtered property is empty.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class IsEmptyFilter extends AbstractPropertyFilter {

	private static final long serialVersionUID = -421332437947826260L;

	protected IsEmptyFilter(Object propertyId) {
		super(propertyId);
	}

	public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor) {
		return String.format("(%s is empty)", propertyIdPreprocessor
				.process(getPropertyId()));
	}
}

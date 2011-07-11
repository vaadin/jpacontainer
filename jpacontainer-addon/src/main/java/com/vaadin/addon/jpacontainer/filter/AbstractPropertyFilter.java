/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

/**
 * Abstract base class for {@link PropertyFilter}s. Subclasses should implement
 * {@link #toQLString(com.vaadin.addon.jpacontainer.Filter.PropertyIdPreprocessor) }
 * .
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public abstract class AbstractPropertyFilter implements PropertyFilter {

	private static final long serialVersionUID = -9084459778211844263L;
	private Object propertyId;

	protected AbstractPropertyFilter(Object propertyId) {
		assert propertyId != null : "propertyId must not be null";
		this.propertyId = propertyId;
	}

	public Object getPropertyId() {
		return propertyId;
	}

	public String toQLString() {
		return toQLString(PropertyIdPreprocessor.DEFAULT);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		return obj.getClass() == getClass()
				&& ((AbstractPropertyFilter) obj).propertyId.equals(propertyId);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() + 7 * propertyId.hashCode();
	}
}

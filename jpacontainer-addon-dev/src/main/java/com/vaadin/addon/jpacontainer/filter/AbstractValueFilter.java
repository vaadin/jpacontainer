/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.filter;

import java.util.Random;

/**
 * Abstract implementation of {@link ValueFilter}.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public abstract class AbstractValueFilter extends AbstractPropertyFilter
		implements ValueFilter {

	private static final long serialVersionUID = -1583391990217077287L;

	private Object value;

	private String qlParameterName;

	protected AbstractValueFilter(Object propertyId, Object value) {
		super(propertyId);
		assert value != null : "value must not be null";
		this.value = value;
		this.qlParameterName = propertyId.toString().replace('.', '_')
				+ Math.abs(new Random().nextInt());
	}

	public Object getValue() {
		return value;
	}

	public String getQLParameterName() {
		return qlParameterName;
	}

	// qlParameterName is not included in equality check, as it is randomly
	// generated.

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj)
				&& ((AbstractValueFilter) obj).value.equals(value);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + 7 * value.hashCode();
	}

}

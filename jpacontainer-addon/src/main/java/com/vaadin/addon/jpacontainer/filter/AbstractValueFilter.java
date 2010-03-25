/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addon.jpacontainer.filter;

import java.util.Random;

/**
 * Abstract implementation of {@link ValueFilter}.
 * 
 * @author Petter Holmström (IT Mill)
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

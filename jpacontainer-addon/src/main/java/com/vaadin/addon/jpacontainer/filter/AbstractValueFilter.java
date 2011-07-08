/*
 * JPAContainer
 * Copyright (C) 2010-2011 Oy Vaadin Ltd
 *
 * This program is available both under Commercial Vaadin Add-On
 * License 2.0 (CVALv2) and under GNU Affero General Public License (version
 * 3 or later) at your option.
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and CVALv2 along with this program.  If not, see
 * <http://www.gnu.org/licenses/> and <http://vaadin.com/license/cval-2.0>.
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

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
package com.vaadin.addons.jpacontainer.filter;

import com.vaadin.addons.jpacontainer.Filter;
import java.util.Iterator;
import java.util.List;

/**
 * A filter that groups other filters together in a single conjunction (A and B
 * and C...).
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class Conjunction extends AbstractJunction {

	private static final long serialVersionUID = -7762904141209202934L;

	protected Conjunction(Filter[] filters) {
		super(filters);
	}

	protected Conjunction(List<Filter> filters) {
		super(filters);
	}

	public String toQLString() {
		return toQLString(PropertyIdPreprocessor.DEFAULT);
	}

	public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor) {
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		for (Iterator<Filter> it = getFilters().iterator(); it.hasNext();) {
			sb.append(it.next().toQLString(propertyIdPreprocessor));
			if (it.hasNext()) {
				sb.append(" and ");
			}
		}
		sb.append(")");
		return sb.toString();
	}
}

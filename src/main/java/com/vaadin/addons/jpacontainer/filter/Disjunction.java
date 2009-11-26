/*
 * JPAContainer
 * Copyright (C) 2009 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.filter;

import java.util.Iterator;

/**
 * A filter that groups other filters together in a single disjunction (A or B or C...).
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
*/
public class Disjunction extends AbstractJunction {

    protected Disjunction(Filter[] filters) {
        super(filters);
    }

    @Override
    public String toQLString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        for (Iterator<Filter> it = getFilters().iterator(); it.hasNext();) {
            sb.append(it.next().toQLString());
            if (it.hasNext()) {
                sb.append(" or ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}

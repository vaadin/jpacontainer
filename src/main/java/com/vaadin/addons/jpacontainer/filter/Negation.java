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

/**
 * Filter that negates another filter.
 *
 * @author Petter Holmström (IT Mill)
 */
public class Negation implements Filter {

    private Filter filter;

    protected Negation(Filter filter) {
        assert filter != null : "filter must not be null";
        this.filter = filter;
    }

    @Override
    public String toQLString() {
        return String.format("(not %s)", filter.toQLString());
    }
}

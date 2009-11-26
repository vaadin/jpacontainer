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

import java.util.Random;

/**
 * Abstract implementation of {@link ValueFilter} that constructs the QL paremter name
 * by appending a random integer to the property ID.
 *
 * @author Petter Holmström (IT Mill)
 */
public abstract class AbstractValueFilter extends AbstractPropertyFilter
        implements ValueFilter {

    private Object value;

    private String qlParameterName;

    protected AbstractValueFilter(Object propertyId, Object value) {
        super(propertyId);
        assert value != null : "value must not be null";
        this.value = value;
        this.qlParameterName = propertyId.toString() + new Random().nextInt();
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getQLParameterName() {
        return qlParameterName;
    }
}

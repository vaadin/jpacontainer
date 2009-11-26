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
 * Abstract base class for {@link PropertyFilter}s. Subclasses should implement
 * {@link #toQLString(com.vaadin.addons.jpacontainer.filter.PropertyFilter.PropertyIdPreprocessor) }.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public abstract class AbstractPropertyFilter implements PropertyFilter {

    private Object propertyId;

    private PropertyIdPreprocessor defaultPreprocessor = new PropertyIdPreprocessor() {

        @Override
        public String process(Object propertyId) {
            assert propertyId != null : "propertyId must not be null";
            return propertyId.toString();
        }
    };

    protected AbstractPropertyFilter(Object propertyId) {
        assert propertyId != null : "propertyId must not be null";
        this.propertyId = propertyId;
    }

    @Override
    public Object getPropertyId() {
        return propertyId;
    }

    @Override
    public String toQLString() {
        return toQLString(defaultPreprocessor);
    }
}

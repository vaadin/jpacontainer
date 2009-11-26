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
 * Interface to be implemented by filters that are applied to a single property.
 * 
 * @author Petter Holmström (IT Mill)
 */
public interface PropertyFilter extends Filter {

    /**
     * Interface to be implemented by all property ID preprocessors.
     *
     * @see PropertyFilter#toQLString(com.vaadin.addons.jpacontainer.filter.PropertyFilter.PropertyIdPreprocessor)
     * @author Petter Holmström (IT Mill)
     */
    public static interface PropertyIdPreprocessor {

        /**
         * Processes <code>propertyId</code> so that it can be
         * used in a QL string.
         *
         * @param propertyId the property ID to process (must not be null).
         * @return the processed property ID.
         */
        public String process(Object propertyId);
    }

    /**
     * Gets the ID of the property that this filter is applied to.
     *
     * @return the property ID (never null).
     */
    public Object getPropertyId();

    /**
     * Returns the same as {@link Filter#toQLString()}, but preprocesses
     * the property ID before it is used in the query.
     *
     * @param propertyIdPreprocessor the property ID preprocessor to use (must not be null).
     * @return the preprocessed QL string (never null).
     */
    public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor);
}

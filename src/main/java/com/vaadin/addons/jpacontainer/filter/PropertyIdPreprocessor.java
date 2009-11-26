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
 * Interface to be implemented by all property ID preprocessors. These
 * are used to support using e.g. aliases in QL-queries.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface PropertyIdPreprocessor {

    /**
     * A property ID preprocessor that returns the String-representation of the property ID without
     * any other processing.
     */
    public static PropertyIdPreprocessor DEFAULT = new PropertyIdPreprocessor() {

        @Override
        public String process(Object propertyId) {
            assert propertyId != null : "propertyId must not be null";
            return propertyId.toString();
        }
    };

    /**
     * Processes <code>propertyId</code> so that it can be
     * used in a QL string.
     *
     * @param propertyId the property ID to process (must not be null).
     * @return the processed property ID.
     */
    public String process(Object propertyId);
}

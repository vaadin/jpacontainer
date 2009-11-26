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
 * Filter that includes all items for which the filtered property is outside
 * a specified interval.
 *
 * @author Petter Holmström (IT Mill)
 */
public class OutsideFilter extends AbstractIntervalFilter {

    protected OutsideFilter(Object propertyId, Object startingPoint,
            boolean startingPointIncluded, Object endingPoint,
            boolean endingPointIncluded) {
        super(propertyId, startingPoint, startingPointIncluded, endingPoint,
                endingPointIncluded);
    }

    @Override
    public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor) {
        String propId = propertyIdPreprocessor.process(getPropertyId());
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        sb.append(propId);
        if (isStartingPointIncluded()) {
            sb.append(" <= ");
        } else {
            sb.append(" < ");
        }
        sb.append(":");
        sb.append(getStartingPointQLParameterName());
        sb.append(" or ");
        sb.append(propId);
        if (isEndingPointIncluded()) {
            sb.append(" >= ");
        } else {
            sb.append(" > ");
        }
        sb.append(":");
        sb.append(getEndingPointQLParameterName());
        sb.append(")");
        return sb.toString();
    }
}

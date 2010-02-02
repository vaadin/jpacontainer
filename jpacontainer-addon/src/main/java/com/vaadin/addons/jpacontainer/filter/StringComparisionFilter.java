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
 * Filter that compares the filtered (String)-property to the filter value using
 * a binary comparision operator. When using the <code>like</code> operator, the precent-sign (%)
 * may be used as wildcard.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class StringComparisionFilter extends AbstractStringFilter {

    private String operator;

    protected StringComparisionFilter(Object propertyId, String value,
            boolean caseSensitive, String operator) {
        super(propertyId, value, caseSensitive);
        assert operator != null : "operator must not be null";
        this.operator = operator;
    }

    /**
     * Gets the operator that is used for comparision.
     */
    public String getOperator() {
        return operator;
    }

    @Override
    public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor) {
        String s;
        if (isCaseSensitive()) {
            s = "(%s %s :%s)";
        } else {
            s = "(upper(%s) %s upper(:%s))";
        }
        return String.format(s, propertyIdPreprocessor.process(
                getPropertyId()), operator, getQLParameterName());
    }
}

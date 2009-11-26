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
 * Abstract implementation of {@link IntervalFilter}.
 *
 * @author Petter Holmström (IT Mill)
 */
public abstract class AbstractIntervalFilter extends AbstractPropertyFilter
        implements IntervalFilter {

    private Object endingPoint;

    private boolean endingPointIncluded;

    private String endingPointQLParameter;

    private Object startingPoint;

    private boolean startingPointIncluded;

    private String startingPointQLParameter;

    protected AbstractIntervalFilter(Object propertyId, Object startingPoint,
            boolean startingPointIncluded, Object endingPoint,
            boolean endingPointIncluded) {
        super(propertyId);
        assert startingPoint != null : "startingPoint must not be null";
        assert endingPoint != null : "endingPoint must not be null";
        int rnd = new Random().nextInt();
        this.startingPoint = startingPoint;
        this.startingPointIncluded = startingPointIncluded;
        this.startingPointQLParameter = propertyId.toString() + "_start" + rnd;
        this.endingPoint = endingPoint;
        this.endingPointIncluded = endingPointIncluded;
        this.endingPointQLParameter = propertyId.toString() + "_end" + rnd;
    }

    @Override
    public Object getEndingPoint() {
        return endingPoint;
    }

    @Override
    public String getEndingPointQLParameterName() {
        return endingPointQLParameter;
    }

    @Override
    public Object getStartingPoint() {
        return startingPoint;
    }

    @Override
    public String getStartingPointQLParameterName() {
        return startingPointQLParameter;
    }

    @Override
    public boolean isEndingPointIncluded() {
        return endingPointIncluded;
    }

    @Override
    public boolean isStartingPointIncluded() {
        return startingPointIncluded;
    }
}

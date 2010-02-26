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
package com.vaadin.addons.jpacontainer.demo.util;

import java.util.Date;
import org.apache.commons.lang.time.DateFormatUtils;

/**
 * Utility class for working with dates.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class DateUtils {

    /**
     * Returns true if <code>date1</code> and <code>date2</code> are both null
     * or represent the same day.
     */
    public static boolean isSameDayOrNull(Date date1, Date date2) {
        if (date1 == date2) {
            return true;
        } else if (date1 != null && date2 != null) {
            return org.apache.commons.lang.time.DateUtils.isSameDay(date1, date2);
        } else {
            return false;
        }
    }

    /**
     * Calculates the hash code based on the day of <code>date</code> only.
     * @param date the date whose hash code should be calculated (may be null).
     * @return the hash code.
     */
    public static int sameDayHashCode(Date date) {
        if (date == null) {
            return 0;
        } else {
            return DateFormatUtils.format(date, "yyyy-MM-dd").hashCode();
        }
    }
}

/*
 * JPAContainer
 * Copyright (C) 2010-2011 Oy Vaadin Ltd
 *
 * This program is available both under Commercial Vaadin Add-On
 * License 2.0 (CVALv2) and under GNU Affero General Public License (version
 * 3 or later) at your option.
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and CVALv2 along with this program.  If not, see
 * <http://www.gnu.org/licenses/> and <http://vaadin.com/license/cval-2.0>.
 */
package com.vaadin.addon.jpacontainer.demo.util;

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

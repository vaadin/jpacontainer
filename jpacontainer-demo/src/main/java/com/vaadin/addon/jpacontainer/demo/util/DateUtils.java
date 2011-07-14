/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.demo.util;

import java.util.Date;
import org.apache.commons.lang.time.DateFormatUtils;

/**
 * Utility class for working with dates.
 *
 * @author Petter Holmström (Vaadin Ltd)
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

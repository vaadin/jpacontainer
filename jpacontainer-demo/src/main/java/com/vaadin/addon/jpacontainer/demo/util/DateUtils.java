/**
 * Copyright 2009-2013 Oy Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
JPAContainer
Copyright (C) 2009-2011 Oy Vaadin Ltd

This program is available under GNU Affero General Public License (version
3 or later at your option).

See the file licensing.txt distributed with this software for more
information about licensing.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

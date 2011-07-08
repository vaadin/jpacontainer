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
package com.vaadin.addon.jpacontainer.filter;

/**
 * Filter that includes all items for which the filtered property is outside a
 * specified interval.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class OutsideFilter extends AbstractIntervalFilter {

	private static final long serialVersionUID = -1307015635957630428L;

	protected OutsideFilter(Object propertyId, Object startingPoint,
			boolean startingPointIncluded, Object endingPoint,
			boolean endingPointIncluded) {
		super(propertyId, startingPoint, startingPointIncluded, endingPoint,
				endingPointIncluded);
	}

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

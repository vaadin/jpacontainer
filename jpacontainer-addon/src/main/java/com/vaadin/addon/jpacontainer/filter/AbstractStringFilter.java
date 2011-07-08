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
 * Abstract base class for filters that filter string properties.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public abstract class AbstractStringFilter extends AbstractValueFilter {

	private static final long serialVersionUID = -4588708261107069904L;

	private boolean caseSensitive;

	protected AbstractStringFilter(Object propertyId, String value,
			boolean caseSensitive) {
		super(propertyId, value);
		this.caseSensitive = caseSensitive;
	}

	/**
	 * Returns whether the filter should be case sensitive or not.
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj)
				&& ((AbstractStringFilter) obj).caseSensitive == caseSensitive;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + 7 * new Boolean(caseSensitive).hashCode();
	}
}

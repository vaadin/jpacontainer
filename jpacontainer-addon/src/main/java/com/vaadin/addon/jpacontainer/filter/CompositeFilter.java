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

import com.vaadin.addon.jpacontainer.Filter;
import java.util.List;

/**
 * Interface to be implemented by all filters that are composed of at least one
 * other filter.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface CompositeFilter extends Filter {

	/**
	 * Gets a list of all the filters included in this composite filter.
	 * 
	 * @return an unmodifiable list of filters (never null).
	 */
	public List<Filter> getFilters();
}

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
package com.vaadin.addon.jpacontainer;

import java.io.Serializable;

/**
 * Interface defining a filter that can be used to filter the entities of an
 * {@link AdvancedFilterable} container.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface Filter extends Serializable {

	/**
	 * Constructs a QL-criteria string for the filter. The returned string is
	 * surrounded by curved brackets.
	 * 
	 * @return the QL-string (never null).
	 */
	public String toQLString();

	/**
	 * Returns the same as {@link Filter#toQLString()}, but preprocesses any
	 * property IDs before they are used in the query.
	 * 
	 * @param propertyIdPreprocessor
	 *            the property ID preprocessor to use (must not be null).
	 * @return the preprocessed QL string (never null).
	 */
	public String toQLString(PropertyIdPreprocessor propertyIdPreprocessor);

	/**
	 * Interface to be implemented by all property ID preprocessors. These are
	 * used to support using e.g. aliases in QL-queries.
	 * 
	 * @author Petter Holmström (IT Mill)
	 * @since 1.0
	 */
	public interface PropertyIdPreprocessor {

		/**
		 * A property ID preprocessor that returns the String-representation of
		 * the property ID without any other processing.
		 */
		public static PropertyIdPreprocessor DEFAULT = new PropertyIdPreprocessor() {

			public String process(Object propertyId) {
				assert propertyId != null : "propertyId must not be null";
				return propertyId.toString();
			}
		};

		/**
		 * Processes <code>propertyId</code> so that it can be used in a QL
		 * string.
		 * 
		 * @param propertyId
		 *            the property ID to process (must not be null).
		 * @return the processed property ID.
		 */
		public String process(Object propertyId);
	}
}

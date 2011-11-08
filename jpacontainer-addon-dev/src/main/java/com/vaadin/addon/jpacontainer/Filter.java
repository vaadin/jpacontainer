/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer;

import java.io.Serializable;

/**
 * Interface defining a filter that can be used to filter the entities of an
 * {@link AdvancedFilterable} container.
 * 
 * @author Petter Holmström (Vaadin Ltd)
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
     * @author Petter Holmström (Vaadin Ltd)
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

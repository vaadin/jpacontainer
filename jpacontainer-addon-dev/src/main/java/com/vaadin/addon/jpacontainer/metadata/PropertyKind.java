package com.vaadin.addon.jpacontainer.metadata;

/**
 * Enumeration defining the property kind.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 */
public enum PropertyKind {

    /**
     * The property is embedded.
     * 
     * @see javax.persistence.Embeddable
     * @see javax.persistence.Embedded
     */
    EMBEDDED,
    /**
     * The property is a reference.
     * 
     * @see javax.persistence.OneToOne
     * @see javax.persistence.ManyToOne
     */
    REFERENCE,
    /**
     * The property is a collection.
     * 
     * @see javax.persistence.OneToMany
     * @see javax.persistence.ManyToMany
     */
    COLLECTION,
    /**
     * The property is of a simple datatype.
     */
    SIMPLE,
    /**
     * The property is not persistent property.
     */
    NONPERSISTENT
}
/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer;

import java.io.Serializable;

/**
 * Listener interface to be implemented by classes that want to be notified when
 * the contents of a {@link EntityProvider} is changed (e.g. entities are added
 * or removed).
 * 
 * @see EntityProviderChangeNotifier
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public interface EntityProviderChangeListener<T> extends Serializable {

    /**
     * Notifies the client that <code>event</code> has occurred.
     * 
     * @param event
     *            the occurred event (never null).
     */
    public void entityProviderChange(EntityProviderChangeEvent<T> event);
}

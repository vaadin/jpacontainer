package com.vaadin.addon.jpacontainer.util;

import java.lang.reflect.Array;
import java.util.Collection;

public class CollectionUtil {

    /**
     * Converts a typed {@link Collection} to a typed array.
     * 
     * @param type
     *            the type class
     * @param collection
     *            the collection to convert to an array.
     * @return the array.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Class<T> type, Collection<T> collection) {
        return collection.toArray((T[]) Array.newInstance(type,
                collection.size()));
    }
}

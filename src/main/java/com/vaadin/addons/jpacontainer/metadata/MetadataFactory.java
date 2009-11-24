/*
 * JPAContainer
 * Copyright (C) 2009 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.metadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class that all factories of {@link ClassMetadata}-implementations
 * should extend. Once extended, {@link #registerFactory(java.lang.String, java.lang.Class) } should
 * be called to make the new factory available to the application.
 *
 * @author Petter Holmström (IT Mill)
 */
public abstract class MetadataFactory {

    /**
     * Extracts the class metadata from <code>entityClass</code>.
     * 
     * @param entityClass the entity class (must not be null).
     * @return the class metadata.
     * @throws IllegalArgumentException if no metadata could be extracted.
     */
    public abstract <T> ClassMetadata<T> getClassMetadata(Class<T> entityClass)
            throws IllegalArgumentException;

    private static final Map<String, MetadataFactory> factoryMap =
            new HashMap<String, MetadataFactory>();

    /**
     * Gets the factory for the specified string key.
     *
     * @param key the string that is used to identify the factory (must not be null).
     * @return the factory (never null).
     * @throws IllegalArgumentException if no factory could be found.
     */
    public static final MetadataFactory getFactory(String key) throws
            IllegalArgumentException {
        assert key != null : "key must not be null";
        MetadataFactory factory = factoryMap.get(key);
        if (factory == null) {
            throw new IllegalArgumentException("Factory not found");
        } else {
            return factory;
        }
    }

    /**
     * Gets the default factory.
     * 
     * @return the factory (never null).
     */
    public static final MetadataFactory getDefaultFactory() {
        try {
            return getFactory("default");
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("No default factory registered");
        }
    }

    /**
     * Registers a new factory.
     *
     * @param key the key string (must not be null).
     * @param factoryClass the factory class (must not be null).
     */
    protected static final void registerFactory(String key,
            Class<? extends MetadataFactory> factoryClass) {
        assert key != null : "key must not be null";
        assert factoryClass != null : "factoryClass must not be null";
        try {
            MetadataFactory factory = factoryClass.newInstance();
            factoryMap.put(key, factory);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not instantiate factory",
                    e);
        }
    }

    /**
     * Unregisters a factory.
     *
     * @param key the key string (must not be null).
     */
    protected static final void unregisterFactory(String key) {
        assert key != null : "key must not be null";
        factoryMap.remove(key);
    }
}

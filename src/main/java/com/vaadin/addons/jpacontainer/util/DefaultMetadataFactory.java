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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.util;

import com.vaadin.addons.jpacontainer.metadata.MetadataFactory;
import com.vaadin.addons.jpacontainer.metadata.impl.ClassMetadataImplFactory;

/**
 * Utility class that provides access to the default {@link MetadataFactory} implementation
 * that is used by all other JPAContainer classes.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public final class DefaultMetadataFactory {

    private static MetadataFactory factory = new ClassMetadataImplFactory();

    private DefaultMetadataFactory() {
        // NOP
    }

    /**
     * Gets the metadata factory instance.
     * 
     * @return the factory instance (never null).
     */
    public static MetadataFactory getInstance() {
        return factory;
    }
}

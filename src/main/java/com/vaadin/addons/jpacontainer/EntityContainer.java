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
package com.vaadin.addons.jpacontainer;

import com.vaadin.addons.jpacontainer.filter.AdvancedFilterable;
import com.vaadin.addons.jpacontainer.metadata.ClassMetadata;
import com.vaadin.data.Container;

/**
 * This interface defines a container for entities, i.e. objects that
 * are stored in some kind of persistence storage.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface EntityContainer<T> extends Container.Sortable,
        AdvancedFilterable, Container.ItemSetChangeNotifier {

    /**
     * Gets the entity class meta data of the entities contained in this 
     * container.
     *
     * @return the entity meta data (never null).
     */
    public ClassMetadata<T> getEntityClassMetadata();

    /**
     * Returns whether the container is read only or writable.
     *
     * @return true if read only, false if writable.
     */
    public boolean isReadOnly();

    /**
     * Changes the read only state of the container, if possible.
     *
     * @param readOnly true to make the container read only, false to make it writable.
     * @throws UnsupportedOperationException if the read only state cannot be changed.
     */
    public void setReadOnly(boolean readOnly) throws
            UnsupportedOperationException;
}

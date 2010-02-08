/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
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

import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Validator.InvalidValueException;
import java.util.List;

/**
 * TODO Document me!
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
final class BufferedContainerDelegate<T> {

    /**
     * Creates a new <code>BufferedContainerDelegate</code> for the specified container.
     *
     * @param container the <code>JPAContainer</code> (must not be null).
     */
    BufferedContainerDelegate(JPAContainer<T> container) {
    }

    public List<Object> getAddedItemIds() {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<Object> getDeletedItemIds() {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<Object> getUpdatedItemIds() {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not implemented");
    }

    public T getAddedEntity(Object itemId) {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not implemented");
    }

    public T getUpdatedEntity(Object itemId) {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isAdded(Object itemId) {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isDeleted(Object itemId) {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isUpdated(Object itemId) {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isModified() {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not implemented");
    }

    public void commit() throws SourceException, InvalidValueException {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not implemented");
    }

    public void discard() throws SourceException {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not implemented");
    }

    public Object addEntity(T entity) {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not implemented");
    }

    public void deleteItem(Object itemId) {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not implemented");
    }

    public void updateEntity(T entity) {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not implemented");
    }
}

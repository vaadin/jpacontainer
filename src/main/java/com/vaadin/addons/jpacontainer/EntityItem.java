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

import com.vaadin.addons.jpacontainer.metadata.EntityClassMetadata;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import java.util.Collection;

/**
 * TODO Implement me!
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class EntityItem<T> implements Item {

    public class EntityItemProperty implements Property {

        @Override
        public Class<?> getType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getValue() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isReadOnly() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setReadOnly(boolean newStatus) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setValue(Object newValue) throws ReadOnlyException,
                ConversionException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    /**
     * TODO Document me!
     * 
     * @param metadata
     * @param entity
     */
    public EntityItem(EntityClassMetadata<T> metadata, T entity) {
    }

    /**
     * <strong>This functionality is not supported by this implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public boolean addItemProperty(Object id, Property property) throws
            UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Property getItemProperty(Object id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<?> getItemPropertyIds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * <strong>This functionality is not supported by this implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public boolean removeItemProperty(Object id) throws
            UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * @return
     */
    public boolean isModified() {
        return false;
    }
}

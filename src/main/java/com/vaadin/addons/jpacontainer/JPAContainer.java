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

import com.vaadin.addons.jpacontainer.filter.CompositeFilter;
import com.vaadin.addons.jpacontainer.filter.Filters;
import com.vaadin.addons.jpacontainer.metadata.EntityClassMetadata;
import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import java.util.Collection;

/**
 * Implementation of {@link EntityContainer} that uses an {@link EntityProvider}
 * to fetch the items. A {@link MutableEntityProvider} can be used
 * to make the container writable.<p>
 * As the data source is responsible for sorting the items, new items
 * cannot be added to a specific location in the list. Rather, the implementation
 * decides where to put new items.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class JPAContainer<T> extends AbstractContainer
        implements EntityContainer {

    // TODO Improve documentation of JPAContainer
    
    private EntityClassMetadata entityClassMetadata;

    private EntityProvider<T> dataSource;

    private boolean readOnly = false;

    /**
     * Creates a new <code>JPAContainer</code>.
     *
     * @param entityClassMetadata the class metadata of the entities that this container will handle (must not be null).
     * @param dataSource the data source from which to fetch the entities (must not be null).
     */
    public JPAContainer(EntityClassMetadata entityClassMetadata,
            EntityProvider<T> dataSource) {
        assert entityClassMetadata != null :
                "entityClassMetadata must not be null";
        assert dataSource != null : "dataSource must not be null";
        this.entityClassMetadata = entityClassMetadata;
        this.dataSource = dataSource;

        if (entityClassMetadata.hasEmbeddedIdentifier()) {
            // TODO Add support for embedded identifiers
            throw new IllegalArgumentException(
                    "Embedded identifiers are currently not supported!");
        }
    }

    /**
     * Gets the data source of this container.
     * 
     * @return the data source (never null).
     */
    protected EntityProvider<T> getDataSource() {
        return dataSource;
    }

    @Override
    public Item getItem(Object itemId) {
        T entity = getDataSource().getEntity(itemId);
        if (entity != null) {
            // TODO I think we need a separate EntityItem class.
            return new BeanItem(entity);
        } else {
            return null;
        }
    }

    /**
     * Returns the applied filters as a conjuction.
     * 
     * @return the conjunction, or null if there are no applied filters.
     */
    protected CompositeFilter getAppliedFiltersAsConjunction() {
        if (getAppliedFilters().isEmpty()) {
            return null;
        } else {
            return Filters.and(getAppliedFilters());
        }
    }

    @Override
    public Collection<?> getItemIds() {
        return getDataSource().getEntityIdentifiers(
                getAppliedFiltersAsConjunction());
    }

    @Override
    public Object firstItemId() {
        return getDataSource().getFirstEntityIdentifier(
                getAppliedFiltersAsConjunction(), getSortBy());
    }

    @Override
    public boolean isFirstId(Object itemId) {
        assert itemId != null : "itemId must not be null";
        Object firstItemId = firstItemId();
        return firstItemId != null ? firstItemId.equals(itemId) : false;
    }

    @Override
    public Object lastItemId() {
        return getDataSource().getLastEntityIdentifier(
                getAppliedFiltersAsConjunction(), getSortBy());
    }

    @Override
    public boolean isLastId(Object itemId) {
        assert itemId != null : "itemId must not be null";
        Object lastItemId = lastItemId();
        return lastItemId != null ? lastItemId.equals(itemId) : false;
    }

    @Override
    public Object nextItemId(Object itemId) {
        return getDataSource().getNextEntityIdentifier(itemId,
                getAppliedFiltersAsConjunction(), getSortBy());
    }

    @Override
    public Object prevItemId(Object itemId) {
        return getDataSource().getPreviousEntityIdentifier(itemId,
                getAppliedFiltersAsConjunction(), getSortBy());
    }

    @Override
    public int size() {
        return new Long(getDataSource().getEntityCount(getAppliedFiltersAsConjunction())).intValue();
    }

    @Override
    public boolean containsId(Object itemId) {
        return getDataSource().containsEntity(itemId,
                getAppliedFiltersAsConjunction());
    }

    /**
     * <strong>This method is not supported by this implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public boolean addContainerProperty(Object propertyId,
            Class<?> type, Object defaultValue) throws
            UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object addItem() throws UnsupportedOperationException {
        checkReadOnly();
        // TODO Implement addItem
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * <strong>This method is not supported by this implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        assert itemId != null : "itemId must not be null";
        assert propertyId != null : "propertyId must not be null";
        Item item = getItem(itemId);
        return item == null ? null : item.getItemProperty(propertyId);
    }

    @Override
    public Class<?> getType(Object propertyId) {
        assert propertyId != null : "propertyId must not be null";
        PropertyMetadata pm = getEntityClassMetadata().getMappedProperty(propertyId.
                toString());
        return pm == null ? null : pm.getType();
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        // TODO Implement removeAllItems
        checkReadOnly();
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * <strong>This method is not supported by this implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public boolean removeContainerProperty(Object propertyId) throws
            UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeItem(Object itemId) throws
            UnsupportedOperationException {
        checkReadOnly();
        // TODO Implement removeItem
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * <strong>This method is not supported by this implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public Object addItemAfter(Object previousItemId) throws
            UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * <strong>This method is not supported by this implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public Item addItemAfter(Object previousItemId, Object newItemId) throws
            UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityClassMetadata getEntityClassMetadata() {
        return entityClassMetadata;
    }

    private void checkReadOnly() throws UnsupportedOperationException {
        if (isReadOnly()) {
            throw new UnsupportedOperationException("Container is read only");
        }
    }

    @Override
    public boolean isReadOnly() {
        return !(getDataSource() instanceof MutableEntityProvider)
                || readOnly;
    }

    @Override
    public void setReadOnly(boolean readOnly) throws
            UnsupportedOperationException {
        if (getDataSource() instanceof MutableEntityProvider) {
            this.readOnly = readOnly;
        }
        throw new UnsupportedOperationException(
                "Data source is not mutable");
    }
}

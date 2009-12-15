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

import com.vaadin.addons.jpacontainer.filter.util.AdvancedFilterableSupport;
import com.vaadin.addons.jpacontainer.metadata.EntityClassMetadata;
import com.vaadin.addons.jpacontainer.metadata.MetadataFactory;
import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
public class JPAContainer<T> implements EntityContainer<T> {

    private EntityProvider<T> entityProvider;
    private AdvancedFilterableSupport filterSupport = new AdvancedFilterableSupport();
    private LinkedList<ItemSetChangeListener> listeners;
    private EntityClassMetadata<T> entityClassMetadata;

    /**
     * 
     * @param entityClass
     */
    public JPAContainer(Class<T> entityClass) {
        assert entityClass != null : "entityClass must not be null";
        this.entityClassMetadata = getClassMetadataFactory().getEntityClassMetadata(entityClass);
    }

    /**
     *
     * @return
     */
    protected MetadataFactory getClassMetadataFactory() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     *
     * @return
     */
    protected EntityClassMetadata<T> getEntityClassMetadata() {
        return entityClassMetadata;
    }

    @Override
    public void addListener(ItemSetChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (listeners == null) {
            listeners = new LinkedList<ItemSetChangeListener>();
        }
        listeners.add(listener);
    }

    @Override
    public void removeListener(ItemSetChangeListener listener) {
        if (listener != null && listeners != null) {
            listeners.remove(listener);
        }
    }

    @Override
    public T addEntity(T entity) throws UnsupportedOperationException, IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addNestedContainerProperty(String nestedProperty) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class<T> getEntityClass() {
        return getEntityClassMetadata().getMappedClass();
    }

    @Override
    public EntityProvider<T> getEntityProvider() {
        return entityProvider;
    }

    @Override
    public boolean isReadOnly() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setEntityProvider(EntityProvider<T> entityProvider) {
        assert entityProvider != null : "entityProvider must not be null";
        this.entityProvider = entityProvider;
    }

    @Override
    public void setReadOnly(boolean readOnly) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<?> getSortableContainerPropertyIds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object firstItemId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isFirstId(Object itemId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isLastId(Object itemId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object lastItemId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object nextItemId(Object itemId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object prevItemId(Object itemId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) throws UnsupportedOperationException {
        // This functionality is not supported
        throw new UnsupportedOperationException();
    }

    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsId(Object itemId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<?> getContainerPropertyIds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Item getItem(Object itemId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<?> getItemIds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class<?> getType(Object propertyId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addFilter(Filter filter) throws IllegalArgumentException {
        filterSupport.addFilter(filter);
    }

    @Override
    public void applyFilters() {
        filterSupport.applyFilters();
    }

    @Override
    public List<Filter> getAppliedFilters() {
        return filterSupport.getAppliedFilters();
    }

    @Override
    public Collection<Object> getFilterablePropertyIds() {
        return filterSupport.getFilterablePropertyIds();
    }

    @Override
    public List<Filter> getFilters() {
        return filterSupport.getFilters();
    }

    @Override
    public boolean hasUnappliedFilters() {
        return filterSupport.hasUnappliedFilters();
    }

    @Override
    public boolean isApplyFiltersImmediately() {
        return filterSupport.isApplyFiltersImmediately();
    }

    @Override
    public boolean isFilterable(Object propertyId) {
        return filterSupport.isFilterable(propertyId);
    }

    @Override
    public void removeAllFilters() {
        filterSupport.removeAllFilters();
    }

    @Override
    public void removeFilter(Filter filter) {
        filterSupport.removeFilter(filter);
    }

    @Override
    public void setApplyFiltersImmediately(boolean applyFiltersImmediately) {
        filterSupport.setApplyFiltersImmediately(applyFiltersImmediately);
    }

    @Override
    public Object addItemAt(int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Item addItemAt(int index, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getIdByIndex(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int indexOfId(Object itemId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void commit() throws SourceException, InvalidValueException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void discard() throws SourceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isModified() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isReadThrough() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isWriteThrough() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setReadThrough(boolean readThrough) throws SourceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setWriteThrough(boolean writeThrough) throws SourceException, InvalidValueException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

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

import com.vaadin.addons.jpacontainer.filter.Filters;
import com.vaadin.addons.jpacontainer.filter.util.AdvancedFilterableSupport;
import com.vaadin.addons.jpacontainer.metadata.EntityClassMetadata;
import com.vaadin.addons.jpacontainer.metadata.MetadataFactory;
import com.vaadin.addons.jpacontainer.util.PropertyList;
import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import java.util.Collection;
import java.util.Collections;
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
    private AdvancedFilterableSupport filterSupport;
    private LinkedList<ItemSetChangeListener> listeners;
    private EntityClassMetadata<T> entityClassMetadata;
    private List<SortBy> sortByList;
    private PropertyList<T> propertyList;
    private boolean readOnly = false;

    /**
     * TODO Document me!
     * 
     * @param entityClass
     */
    public JPAContainer(Class<T> entityClass) {
        assert entityClass != null : "entityClass must not be null";
        this.entityClassMetadata = MetadataFactory.getInstance().
                getEntityClassMetadata(entityClass);
        this.propertyList = new PropertyList(entityClassMetadata);
        this.filterSupport = new AdvancedFilterableSupport();
        /*
         * Add a listener to filterSupport, so that we can notify all
         * clients that use our container that the data has been filtered.
         */
        this.filterSupport.addListener(new AdvancedFilterableSupport.Listener() {

            @Override
            public void filtersApplied(AdvancedFilterableSupport sender) {
                fireContainerItemSetChange(new FiltersAppliedEvent(
                        JPAContainer.this));
            }
        });
        // This list instance will remain the same, which means that any changes
        // made to propertyList will automatically show up in filterSupport as well.
        this.filterSupport.setFilterablePropertyIds((Collection) propertyList.
                getPersistentPropertyNames());
    }

    /**
     * TODO Document me!
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

    /**
     * Publishes <code>event</code> to all registered
     * <code>ItemSetChangeListener</code>s.
     *
     * @param event the event to publish (must not be null).
     */
    protected void fireContainerItemSetChange(final ItemSetChangeEvent event) {
        assert event != null : "event must not be null";
        if (listeners == null) {
            return;
        }
        LinkedList<ItemSetChangeListener> list =
                (LinkedList<ItemSetChangeListener>) listeners.clone();
        for (ItemSetChangeListener l : list) {
            l.containerItemSetChange(event);
        }
    }

    @Override
    public void addNestedContainerProperty(String nestedProperty) throws
            UnsupportedOperationException {
        propertyList.addNestedProperty(nestedProperty);
    }

    @Override
    public Class<T> getEntityClass() {
        return getEntityClassMetadata().getMappedClass();
    }

    @Override
    public EntityProvider<T> getEntityProvider() {
        return entityProvider;
    }

    /**
     * TODO Document me!
     * 
     * @return
     * @throws IllegalStateException
     */
    protected EntityProvider<T> doGetEntityProvider() throws
            IllegalStateException {
        if (entityProvider == null) {
            throw new IllegalStateException("No EntityProvider has been set");
        }
        return entityProvider;
    }

    @Override
    public boolean isReadOnly() {
        return !(doGetEntityProvider() instanceof MutableEntityProvider) || readOnly;
    }

    @Override
    public void setEntityProvider(EntityProvider<T> entityProvider) {
        assert entityProvider != null : "entityProvider must not be null";
        this.entityProvider = entityProvider;
    }

    @Override
    public void setReadOnly(boolean readOnly) throws
            UnsupportedOperationException {
        if (doGetEntityProvider() instanceof MutableEntityProvider) {
            this.readOnly = readOnly;
        } else {
            throw new UnsupportedOperationException(
                    "EntityProvider is not mutable");
        }
    }

    @Override
    public Collection<String> getSortableContainerPropertyIds() {
        return propertyList.getPersistentPropertyNames();
    }

    @Override
    @SuppressWarnings("element-type-mismatch")
    public void sort(Object[] propertyId, boolean[] ascending) {
        assert propertyId != null : "propertyId must not be null";
        assert ascending != null : "ascending must not be null";
        assert propertyId.length == ascending.length :
                "propertyId and ascending must have the same length";
        sortByList = new LinkedList<SortBy>();
        for (int i = 0; i < propertyId.length; ++i) {
            if (!getSortableContainerPropertyIds().contains(propertyId[i])) {
                throw new IllegalArgumentException(
                        "No such sortable property ID: " + propertyId[i]);
            }
            sortByList.add(new SortBy(propertyId[i], ascending[i]));
        }
        sortByList = Collections.unmodifiableList(sortByList);
        fireContainerItemSetChange(new ContainerSortedEvent());
    }

    /**
     * Gets all the properties that the items should be sorted by, if any.
     *
     * @return an unmodifiable, possible empty list of <code>SortBy</code>
     *      instances (never null).
     */
    protected List<SortBy> getSortByList() {
        if (sortByList == null) {
            return Collections.emptyList();
        } else {
            return sortByList;
        }
    }

    /**
     * <strong>This functionality is not supported by this implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public Object addItemAfter(Object previousItemId) throws
            UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * <strong>This functionality is not supported by this implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public Item addItemAfter(Object previousItemId, Object newItemId) throws
            UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object firstItemId() {
        return doGetEntityProvider().getFirstEntityIdentifier(
                getAppliedFiltersAsConjunction(), getSortByList());
    }

    @Override
    public boolean isFirstId(Object itemId) {
        assert itemId != null : "itemId must not be null";
        return itemId.equals(firstItemId());
    }

    @Override
    public boolean isLastId(Object itemId) {
        assert itemId != null : "itemId must not be null";
        return itemId.equals(lastItemId());
    }

    @Override
    public Object lastItemId() {
        return doGetEntityProvider().getLastEntityIdentifier(
                getAppliedFiltersAsConjunction(), getSortByList());
    }

    @Override
    public Object nextItemId(Object itemId) {
        return doGetEntityProvider().getNextEntityIdentifier(itemId,
                getAppliedFiltersAsConjunction(),
                getSortByList());
    }

    @Override
    public Object prevItemId(Object itemId) {
        return doGetEntityProvider().getPreviousEntityIdentifier(itemId,
                getAppliedFiltersAsConjunction(),
                getSortByList());
    }

    /**
     * <strong>This functionality is not supported by this implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public boolean addContainerProperty(Object propertyId, Class<?> type,
            Object defaultValue) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * <strong>This functionality is not supported by this implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * <strong>This functionality is not supported by this implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsId(Object itemId) {
        return doGetEntityProvider().containsEntity(itemId,
                getAppliedFiltersAsConjunction());
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        Item item = getItem(itemId);
        return item == null ? null : item.getItemProperty(propertyId);
    }

    @Override
    public Collection<String> getContainerPropertyIds() {
        return propertyList.getPropertyNames();
    }

    @Override
    public Item getItem(Object itemId) {
        T entity = doGetEntityProvider().getEntity(itemId);
        return entity != null ? new EntityItem(propertyList, entity) : null;
    }

    /**
     * <strong>This impementation does not use lazy loading and performs bad when the number of items is large!
     * Do not use unless you absolutely have to!</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public Collection<Object> getItemIds() {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public Class<?> getType(Object propertyId) {
        assert propertyId != null : "propertyId must not be null";
        return propertyList.getPropertyType(propertyId.toString());
    }

    @Override
    public boolean removeContainerProperty(Object propertyId) throws
            UnsupportedOperationException {
        assert propertyId != null : "propertyId must not be null";
        return propertyList.removeProperty(propertyId.toString());
    }

    @Override
    public int size() {
        return doGetEntityProvider().getEntityCount(
                getAppliedFiltersAsConjunction());
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

    /**
     * TODO Document me!
     * 
     * @return
     */
    protected Filter getAppliedFiltersAsConjunction() {
        if (getAppliedFilters().isEmpty()) {
            return null;
        } else {
            return Filters.and(getAppliedFilters());
        }
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

    /**
     * <strong>This functionality is not supported by this implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public Object addItemAt(int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * <strong>This functionality is not supported by this implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public Item addItemAt(int index, Object newItemId) throws
            UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getIdByIndex(int index) {
        return doGetEntityProvider().getEntityIdentifierAt(
                getAppliedFiltersAsConjunction(), getSortByList(), index);
    }

    /**
     * <strong>This impementation does not use lazy loading and performs bad when the number of items is large!
     * Do not use unless you absolutely have to!</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public int indexOfId(Object itemId) {
        /*
         * This is intentionally an ugly implementation! This method
         * should not be used!
         */
        for (int i = 0; i < size(); i++) {
            Object id = getIdByIndex(i);
            if (id == null) {
                return -1;
            } else if (id.equals(itemId)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public T addEntity(T entity) throws UnsupportedOperationException,
            IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeItem(Object itemId) throws
            UnsupportedOperationException {
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
    public void setWriteThrough(boolean writeThrough) throws SourceException,
            InvalidValueException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Event indicating that the container has been resorted.
     *
     * @author Petter Holmström (IT Mill)
     * @since 1.0
     */
    public final class ContainerSortedEvent implements ItemSetChangeEvent {

        protected ContainerSortedEvent() {
        }

        @Override
        public Container getContainer() {
            return JPAContainer.this;
        }
    }
}

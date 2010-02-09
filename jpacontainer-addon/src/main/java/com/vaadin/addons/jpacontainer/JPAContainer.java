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
import com.vaadin.addons.jpacontainer.filter.PropertyFilter;
import com.vaadin.addons.jpacontainer.filter.util.AdvancedFilterableSupport;
import com.vaadin.addons.jpacontainer.metadata.EntityClassMetadata;
import com.vaadin.addons.jpacontainer.metadata.MetadataFactory;
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
 * to make the container writable. Buffered mode (write through turned off) can
 * be used if the entity provider implements the {@link BatchableEntityProvider} interface.
 * <p>
 * When using buffered mode, the following rules apply:
 * <ul>
 *   <li>All operations that add, update or remove entities are recorded internally.</li>
 *   <li>If an item is added and then removed later within the same transaction, the add operation will be removed and no update operation will be recorded.</li>
 *   <li>If an item is added and then updated later within the same transaction, only the add operation will be recorded.</li>
 *   <li>If an item is updated and then removed later within the same transaction, only the remove operation will be recorded.</li>
 *   <li>In the case of an update or edit, a clone of the affected entity is recorded together with the operation if the entity class implements the {@link Cloneable} interface. Otherwise,
 *       the entity instance is recorded.</li>
 *   <li>When the changes are committed, all recorded operations are carried out on the {@link BatchableEntityProvider} in the same order that they were recorded.</li>
 * </ul>
 * <p>
 * Please note, that if an entity is not cloneable and it is modified twice, both update records will
 * point to the same entity instance. Thus, the changes of the second modification will be present
 * in the record of the first modification. This is something that implementations of {@link BatchableEntityProvider}
 * need to be aware of.
 * <p>
 * Also note, that it is possible to use buffered mode even if the entities returned from the entity provider
 * are not explicitly detached (see {@link EntityProvider#isEntitiesDetached() }), but this should
 * be avoided unless you know what you are doing.
 * <p>
 * As the data source is responsible for sorting the items, new items
 * cannot be added to a specific location in the list. Instead, new items
 * are always added to the top of the container.
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
    private BufferedContainerDelegate<T> bufferingDelegate;
    private boolean readOnly = false;
    private boolean writeThrough = false;

    /**
     * Creates a new <code>JPAContainer</code> instance for entities of class
     * <code>entityClass</code>. An entity provider must be provided using
     * the {@link #setEntityProvider(com.vaadin.addons.jpacontainer.EntityProvider) }
     * before the container can be used.
     * 
     * @param entityClass the class of the entities that will reside in this container (must not be null).
     */
    public JPAContainer(Class<T> entityClass) {
        assert entityClass != null : "entityClass must not be null";
        this.entityClassMetadata = MetadataFactory.getInstance().
                getEntityClassMetadata(entityClass);
        this.propertyList = new PropertyList(entityClassMetadata);
        this.filterSupport = new AdvancedFilterableSupport();
        this.bufferingDelegate = new BufferedContainerDelegate<T>(this);
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
     * Gets the mapping metadata of the entity class.
     * @see EntityClassMetadata
     *
     * @return the metadata (never null).
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
     * Checks that the entity provider is not null and returns it.
     * 
     * @return the entity provider (never null).
     * @throws IllegalStateException if the entity provider was null.
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
        if (readOnly) {
            this.readOnly = readOnly;
        } else {
            if (doGetEntityProvider() instanceof MutableEntityProvider) {
                this.readOnly = readOnly;
            } else {
                throw new UnsupportedOperationException(
                        "EntityProvider is not mutable");
            }
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
        if (isWriteThrough() || bufferingDelegate.getAddedItemIds().isEmpty()) {
            Object itemId = doGetEntityProvider().getFirstEntityIdentifier(
                    getAppliedFiltersAsConjunction(), getSortByList());
            return itemId;
        } else {
            return bufferingDelegate.getAddedItemIds().get(0);
        }
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
        Object itemId = doGetEntityProvider().getLastEntityIdentifier(
                getAppliedFiltersAsConjunction(), getSortByList());
        if (isWriteThrough() || bufferingDelegate.getAddedItemIds().isEmpty()) {
            return itemId;
        } else {
            if (itemId == null) {
                return bufferingDelegate.getAddedItemIds().get(bufferingDelegate.
                        getAddedItemIds().size() - 1);
            } else {
                return itemId;
            }
        }
    }

    @Override
    public Object nextItemId(Object itemId) {
        if (isWriteThrough() || bufferingDelegate.getAddedItemIds().isEmpty() || !bufferingDelegate.
                isAdded(itemId)) {
            return doGetEntityProvider().getNextEntityIdentifier(itemId,
                    getAppliedFiltersAsConjunction(),
                    getSortByList());
        } else {
            int ix = bufferingDelegate.getAddedItemIds().indexOf(itemId);
            if (ix == bufferingDelegate.getAddedItemIds().size() - 1) {
                return doGetEntityProvider().getFirstEntityIdentifier(
                        getAppliedFiltersAsConjunction(), getSortByList());
            } else {
                return bufferingDelegate.getAddedItemIds().get(ix + 1);
            }
        }
    }

    @Override
    public Object prevItemId(Object itemId) {
        if (isWriteThrough() || bufferingDelegate.getAddedItemIds().isEmpty()) {
            return doGetEntityProvider().getPreviousEntityIdentifier(itemId,
                    getAppliedFiltersAsConjunction(),
                    getSortByList());
        } else {
            if (bufferingDelegate.isAdded(itemId)) {
                int ix = bufferingDelegate.getAddedItemIds().indexOf(itemId);
                if (ix == 0) {
                    return null;
                } else {
                    return bufferingDelegate.getAddedItemIds().get(ix - 1);
                }
            } else {
                Object prevId = doGetEntityProvider().
                        getPreviousEntityIdentifier(itemId,
                        getAppliedFiltersAsConjunction(),
                        getSortByList());
                if (prevId == null) {
                    return bufferingDelegate.getAddedItemIds().get(bufferingDelegate.
                            getAddedItemIds().size() - 1);
                } else {
                    return prevId;
                }
            }
        }
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
        if (isWriteThrough()) {
            return doGetEntityProvider().containsEntity(itemId,
                    getAppliedFiltersAsConjunction());
        } else {
            return bufferingDelegate.isAdded(itemId) || doGetEntityProvider().
                    containsEntity(itemId,
                    getAppliedFiltersAsConjunction());
        }
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

    /**
     * Method used by {@link EntityItem} to gain access to the property list.
     * Not to be used by clients directly.
     * @return the property list.
     */
    PropertyList<T> getPropertyList() {
        // TODO Not sure whether this is a good idea, maybe the property list could be passed to EntityItem as a constructor parameter?
        return propertyList;
    }

    /**
     * {@inheritDoc }
     * <p>
     * Please note, that this method will create a new instance of {@link EntityItem} upon every execution. That is,
     * two subsequent calls to this method with the same <code>itemId</code> will <b>not</b> return the same {@link EntityItem} instance.
     * The actual entity instance may still be the same though, depending on the implementation of the entity provider.
     */
    @Override
    public EntityItem<T> getItem(Object itemId) {
        // TODO This is slow! Is there some way of optimizing this, eg. caching the items?
        if (isWriteThrough() || !bufferingDelegate.isModified()) {
            T entity = doGetEntityProvider().getEntity(itemId);
            return entity != null ? new JPAContainerItem<T>(this, entity) : null;
        } else {
            if (bufferingDelegate.isAdded(itemId)) {
                JPAContainerItem<T> item = new JPAContainerItem<T>(this, bufferingDelegate.
                        getAddedEntity(itemId), itemId, false);
                return item;
            } else if (bufferingDelegate.isUpdated(itemId)) {
                JPAContainerItem<T> item = new JPAContainerItem<T>(this, bufferingDelegate.
                        getUpdatedEntity(itemId));
                item.setDirty(true);
                return item;
            } else if (bufferingDelegate.isDeleted(itemId)) {
                T entity = doGetEntityProvider().getEntity(itemId);
                if (entity != null) {
                    JPAContainerItem<T> item = new JPAContainerItem<T>(this,
                            entity);
                    item.setDeleted(true);
                    return item;
                } else {
                    return null;
                }
            } else {
                T entity = doGetEntityProvider().getEntity(itemId);
                return entity != null ? new JPAContainerItem<T>(this, entity) : null;
            }
        }
    }

    /**
     * <strong>This impementation does not use lazy loading and performs bad when the number of items is large!
     * Do not use unless you absolutely have to!</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public Collection<Object> getItemIds() {
        Collection<Object> ids = getEntityProvider().getAllEntityIdentifiers(
                getAppliedFiltersAsConjunction(), getSortByList());
        if (isWriteThrough() || !bufferingDelegate.isModified()) {
            return ids;
        } else {
            List<Object> newIds = new LinkedList<Object>();
            newIds.addAll(bufferingDelegate.getAddedItemIds());
            newIds.addAll(ids);
            return Collections.unmodifiableCollection(newIds);
        }
    }

    @Override
    public EntityItem<T> createEntityItem(T entity) {
        return new JPAContainerItem<T>(this, entity, null, false);
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
        int origSize = doGetEntityProvider().getEntityCount(
                getAppliedFiltersAsConjunction());
        if (isWriteThrough()) {
            return origSize;
        } else {
            int newSize = origSize + bufferingDelegate.getAddedItemIds().size();
            return newSize;
        }
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
     * Returns a conjunction (filter1 AND filter2 AND ... AND filterN) of all the applied filters.
     * If there are no applied filters, this method returns null.
     * @see #getAppliedFilters()
     * @see Filters#and(com.vaadin.addons.jpacontainer.Filter[]) 
     * @return a conjunction filter or null.
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

    public void addContainerFilter(Object propertyId, String filterString,
            boolean ignoreCase, boolean onlyMatchPrefix) {
        // TODO Test me!
        if (onlyMatchPrefix) {
            filterString = filterString + "%";
        } else {
            filterString = "%" + filterString + "%";
        }
        addFilter(Filters.like(propertyId, filterString, !ignoreCase));
        if (!isApplyFiltersImmediately()) {
            applyFilters();
        }
    }

    public void removeAllContainerFilters() {
        // TODO Test me!
        removeAllFilters();
        if (!isApplyFiltersImmediately()) {
            applyFilters();
        }
    }

    public void removeContainerFilters(Object propertyId) {
        // TODO Test me!
        List<Filter> filters = getFilters();
        for (int i = filters.size() - 1; i >= 0; i--) {
            Filter f = filters.get(i);
            if (f instanceof PropertyFilter && ((PropertyFilter) f).
                    getPropertyId().equals(propertyId)) {
                removeFilter(f);
            }
        }
        if (!isApplyFiltersImmediately()) {
            applyFilters();
        }
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
        if (isWriteThrough()) {
            return doGetEntityProvider().getEntityIdentifierAt(
                    getAppliedFiltersAsConjunction(), getSortByList(), index);
        } else {
            int addedItems = bufferingDelegate.getAddedItemIds().size();
            if (index < addedItems) {
                return bufferingDelegate.getAddedItemIds().get(index);
            } else {
                Object itemId = doGetEntityProvider().getEntityIdentifierAt(
                        getAppliedFiltersAsConjunction(), getSortByList(),
                        index - addedItems);
                return itemId;
            }
        }
    }

    /**
     * <strong>This impementation does not use lazy loading and performs <b>extremely</b> bad when the number of items is large!
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
        if (size() > 100) {
            System.err.println(
                    "(JPAContainer) WARNING! Invoking indexOfId() when size > 100 is not recommended!");
        }
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

    /**
     * Checks that the container is writable, i.e. the entity provider implements
     * the {@link MutableEntityProvider} interface and the container is not marked
     * as read only.
     * 
     * @throws IllegalStateException if the container is read only.
     * @throws UnsupportedOperationException if the entity provider does not support editing.
     */
    protected void requireWritableContainer() throws IllegalStateException,
            UnsupportedOperationException {
        if (!(entityProvider instanceof MutableEntityProvider)) {
            throw new UnsupportedOperationException(
                    "EntityProvider does not support editing");
        }
        if (readOnly) {
            throw new IllegalStateException("Container is read only");
        }
    }

    @Override
    public Object addEntity(T entity) throws UnsupportedOperationException,
            IllegalStateException {
        assert entity != null : "entity must not be null";
        requireWritableContainer();

        Object id;
        if (isWriteThrough()) {
            T result = ((MutableEntityProvider<T>) getEntityProvider()).
                    addEntity(entity);
            id = getEntityClassMetadata().getPropertyValue(result, getEntityClassMetadata().
                    getIdentifierProperty().getName());
        } else {
            id = bufferingDelegate.addEntity(entity);
        }
        fireContainerItemSetChange(new ItemAddedEvent(id));
        return id;
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        // TODO Implement me!
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeItem(Object itemId) throws
            UnsupportedOperationException {
        assert itemId != null : "itemId must not be null";
        requireWritableContainer();

        if (isWriteThrough()) {
            if (getEntityProvider().containsEntity(itemId, null)) {
                ((MutableEntityProvider) getEntityProvider()).removeEntity(
                        itemId);
                fireContainerItemSetChange(new ItemRemovedEvent(itemId));
                return true;
            } else {
                return false;
            }
        } else {
            if (bufferingDelegate.isAdded(itemId) || getEntityProvider().
                    containsEntity(itemId, null)) {
                bufferingDelegate.deleteItem(itemId);
                fireContainerItemSetChange(new ItemRemovedEvent(itemId));
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * This method is used by the {@link JPAContainerItem} class
     * and <b>should not be used by other classes</b>. It is only called when
     * the item is in write through mode, i.e. when an updated property value
     * is directly reflected in the backed entity instance. If the item is in buffered mode (write through
     * is off), {@link #containerItemModified(com.vaadin.addons.jpacontainer.EntityItem) } is
     * used instead.
     * <p>
     * This method notifies the container that the specified property of <code>item</code>
     * has been modified. The container will then take appropriate actions
     * to pass the changes on to the entity provider, depending on the state
     * of the <code>writeThrough</code> property <i>of the container</i>.
     * <p>
     * If <code>item</code> has no item ID ({@link JPAContainerItem#getItemId() }), this method does nothing.
     *
     * @see #isWriteThrough()
     * @param item the item that has been modified (must not be null).
     * @param propertyId the ID of the modified property (must not be null).
     */
    void containerItemPropertyModified(JPAContainerItem<T> item,
            String propertyId) {
        assert item != null : "item must not be null";
        assert propertyId != null : "propertyId must not be null";

        if (item.getItemId() != null) {

            requireWritableContainer();

            Object itemId = item.getItemId();
            if (isWriteThrough()) {
                ((MutableEntityProvider) getEntityProvider()).
                        updateEntityProperty(
                        itemId, propertyId, item.getItemProperty(propertyId).
                        getValue());
                item.setDirty(false);
            } else {
                bufferingDelegate.updateEntity(itemId, item.getEntity());
            }
            fireContainerItemSetChange(new ItemUpdatedEvent(itemId));
        }
    }

    /**
     * This method is used by the {@link JPAContainerItem} class
     * and <b>should not be used by other classes</b>. It is only called when
     * the item is in buffered mode (write through is off), i.e. when updated property
     * values are not reflected in the backend entity instance until the item's
     * commit method has been invoked. If write through
     * is turned on, {@link #containerItemPropertyModified(com.vaadin.addons.jpacontainer.JPAContainerItem, java.lang.String)  } is
     * used instead.
     * <p>
     * This method notifies the container that the specified <code>item</code>
     * has been modified. The container will then take appropriate actions
     * to pass the changes on to the entity provider, depending on the state
     * of the <code>writeThrough</code> property <i>of the container</i>.
     * <p>
     * If <code>item</code> has no item ID ({@link JPAContainerItem#getItemId() }), this method does nothing.
     * 
     * @see #isWriteThrough()
     * @param item the item that has been modified (must not be null).
     */
    void containerItemModified(JPAContainerItem<T> item) {
        assert item != null : "item must not be null";

        if (item.getItemId() != null) {
            requireWritableContainer();

            Object itemId = item.getItemId();
            if (isWriteThrough()) {
                ((MutableEntityProvider) getEntityProvider()).updateEntity(item.
                        getEntity());
                item.setDirty(false);
            } else {
                bufferingDelegate.updateEntity(itemId, item.getEntity());
            }
            fireContainerItemSetChange(new ItemUpdatedEvent(itemId));
        }
    }

    @Override
    public void commit() throws SourceException, InvalidValueException {
        if (!isWriteThrough() && isModified()) {
            bufferingDelegate.commit();
            fireContainerItemSetChange(new ChangesCommittedEvent());
        }
    }

    @Override
    public void discard() throws SourceException {
        if (!isWriteThrough() && isModified()) {
            bufferingDelegate.discard();
            fireContainerItemSetChange(new ChangesDiscardedEvent());
        }
    }

    @Override
    public boolean isModified() {
        if (isWriteThrough()) {
            return false;
        } else {
            return bufferingDelegate.isModified();
        }
    }

    @Override
    public boolean isReadThrough() {
        EntityProvider<T> ep = doGetEntityProvider();
        if (ep instanceof CachingEntityProvider) {
            return !((CachingEntityProvider) ep).isCacheInUse();
        }
        return true; // There is no cache at all
    }

    @Override
    public boolean isWriteThrough() {
        return !(doGetEntityProvider() instanceof BatchableEntityProvider) || writeThrough;
    }

    /**
     * <strong>This functionality is not supported by this implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    @Override
    public void setReadThrough(boolean readThrough) throws SourceException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc }
     * <p>
     * <b>Note</b>, that write-through mode can only be turned off if the entity
     * provider implements the {@link BatchableEntityProvider} interface.
     */
    @Override
    public void setWriteThrough(boolean writeThrough) throws SourceException,
            InvalidValueException {
        if (writeThrough) {
            commit();
            this.writeThrough = writeThrough;
        } else {
            if (doGetEntityProvider() instanceof BatchableEntityProvider) {
                this.writeThrough = writeThrough;
            } else {
                throw new UnsupportedOperationException(
                        "EntityProvider is not batchable");
            }
        }
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SourceException,
            InvalidValueException {
        setWriteThrough(autoCommit);
    }

    @Override
    public boolean isAutoCommit() {
        return isWriteThrough();
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

    /**
     * Event indicating that the changes have been committed. It will be fired when the container
     * has write-through/auto-commit turned off and {@link JPAContainer#commit()} is called.
     *
     * @author Petter Holmström (IT Mill)
     * @since 1.0
     */
    public final class ChangesCommittedEvent implements ItemSetChangeEvent {

        protected ChangesCommittedEvent() {
        }

        @Override
        public Container getContainer() {
            return JPAContainer.this;
        }
    }

    /**
     * Event indicating that the changes have been discarded. This event is fired when the container
     * has write-through/auto-commit turned off and {@link JPAContainer#discard() } is called.
     *
     * @author Petter Holmström (IT Mill)
     * @since 1.0
     */
    public final class ChangesDiscardedEvent implements ItemSetChangeEvent {

        protected ChangesDiscardedEvent() {
        }

        @Override
        public Container getContainer() {
            return JPAContainer.this;
        }
    }

    /**
     * Event indicating that all the items have been removed from the container. This event is
     * fired by {@link JPAContainer#removeAllItems() }.
     *
     * @author Petter Holmström (IT Mill)
     * @since 1.0
     */
    public final class AllItemsRemovedEvent implements ItemSetChangeEvent {

        protected AllItemsRemovedEvent() {
        }

        @Override
        public Container getContainer() {
            return JPAContainer.this;
        }
    }

    /**
     * Abstract base class for events concerning single {@link EntityItem}s.
     *
     * @author Petter Holmström (IT Mill)
     * @since 1.0
     */
    public abstract class ItemEvent implements ItemSetChangeEvent {

        protected final Object itemId;

        protected ItemEvent(Object itemId) {
            this.itemId = itemId;
        }

        @Override
        public Container getContainer() {
            return JPAContainer.this;
        }

        /**
         * Gets the ID of the item that this event concerns.
         * @return the item ID.
         */
        public Object getItemId() {
            return itemId;
        }
    }

    /**
     * Event indicating that an item has been added to the container. This event
     * is fired by {@link JPAContainer#addEntity(java.lang.Object) }.
     *
     * @author Petter Holmström (IT Mill)
     * @since 1.0
     */
    public final class ItemAddedEvent extends ItemEvent {

        protected ItemAddedEvent(Object itemId) {
            super(itemId);
        }
    }

    /**
     * Event indicating that an item has been updated inside the container.
     *
     * @author Petter Holmström (IT Mill)
     * @since 1.0
     */
    public final class ItemUpdatedEvent extends ItemEvent {

        protected ItemUpdatedEvent(Object itemId) {
            super(itemId);
        }
    }

    /**
     * Event indicating that an item has been removed from the container. This
     * event is fired by {@link JPAContainer#removeItem(java.lang.Object) }.
     *
     * @author Petter Holmström (IT Mill)
     * @since 1.0
     */
    public final class ItemRemovedEvent extends ItemEvent {

        protected ItemRemovedEvent(Object itemId) {
            super(itemId);
        }
    }
}

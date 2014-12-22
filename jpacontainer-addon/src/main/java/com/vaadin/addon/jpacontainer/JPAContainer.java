/**
 * Copyright 2009-2013 Oy Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vaadin.addon.jpacontainer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.vaadin.addon.jpacontainer.EntityProviderChangeEvent.EntityPropertyUpdatedEvent;
import com.vaadin.addon.jpacontainer.filter.util.AdvancedFilterableSupport;
import com.vaadin.addon.jpacontainer.metadata.EntityClassMetadata;
import com.vaadin.addon.jpacontainer.metadata.MetadataFactory;
import com.vaadin.addon.jpacontainer.metadata.PersistentPropertyMetadata;
import com.vaadin.addon.jpacontainer.metadata.PropertyKind;
import com.vaadin.addon.jpacontainer.util.CollectionUtil;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.data.util.filter.IsNull;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.util.filter.UnsupportedFilterException;

/**
 * This is the main container class of JPAContainer (and the default
 * implementation of {@link EntityContainer}). You can use it in your
 * applications like so: <code><pre>
 * EntityContainer&lt;MyEntity&gt; container = new JPAContainer&lt;MyEntity&gt;(
 *   MyEntity.class);
 * container.setEntityProvider(myEntityProvider);
 * ...
 * myTable.setContainerDataSource(container);
 * </pre></code> In the example code, <code>myEntityProvider</code> is an
 * instance of an {@link EntityProvider} that, like the name suggests, is
 * responsible for providing the entities to the container. If the container
 * should be writable, the entity provider must implement the
 * {@link MutableEntityProvider} interface and if buffering is desired (i.e.
 * write-through turned off) the {@link BatchableEntityProvider} interface as
 * well. There are ready-made implementations of all these interfaces that can
 * be used out-of-the-box (check the See Also section of this Javadoc).
 * <p>
 * All sorting and filtering is handled by the entity provider, which in turn
 * normally delegates it to the database. Therefore, only persistent properties
 * can be filtered and/or sorted by.
 * <p>
 * It is possible to use JPAContainer as a hierarchical container if the
 * entities in the container can be related to each other by means of a parent
 * property. For example:
 * 
 * <pre>
 * <code>
 * &#064;Entity
 * public class Node {
 *   ...
 *   &#064;ManyToOne
 *   private Node parent;
 *   ...
 * }
 * </code>
 * </pre>
 * 
 * Note, however, that the implementation of {@link HierarchicalEntityContainer}
 * is still experimental and has some limitations. For example, the data is
 * always read directly from the entity provider regardless of whether buffering
 * is used or not. Therefore, this feature should be used with care in
 * production systems.
 * 
 * <h2>Buffering</h2>
 * Here follows some notes on how buffering has been implemented in this class.
 * If you are not going to use buffering, you can skip this section.
 * <p>
 * When using buffered mode, the following rules apply:
 * <ul>
 * <li>All operations that add, update or remove entities are recorded
 * internally.</li>
 * <li>If an item is added and then removed later within the same transaction,
 * the add operation will be removed and no update operation will be recorded.</li>
 * <li>If an item is added and then updated later within the same transaction,
 * only the add operation will be recorded.</li>
 * <li>If an item is updated and then removed later within the same transaction,
 * only the remove operation will be recorded.</li>
 * <li>In the case of an update or edit all changes are applied at once.</li>
 * <li>When the changes are committed, all recorded operations are carried out
 * on the {@link BatchableEntityProvider} in the same order that they were
 * recorded.</li>
 * </ul>
 * <p>
 * Please note, that if an entity is modified twice or more, updates are
 * "merged". This is something that implementations of
 * {@link BatchableEntityProvider} need to be aware of.
 * <p>
 * Also note, that it is possible to use buffered mode even if the entities
 * returned from the entity provider are not explicitly detached (see
 * {@link EntityProvider#isEntitiesDetached() }), but this should be avoided
 * unless you know what you are doing.
 * 
 * @see com.vaadin.addon.jpacontainer.provider.LocalEntityProvider
 * @see com.vaadin.addon.jpacontainer.provider.CachingLocalEntityProvider
 * @see com.vaadin.addon.jpacontainer.provider.BatchableLocalEntityProvider
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class JPAContainer<T> implements EntityContainer<T>,
        EntityProviderChangeListener<T>, HierarchicalEntityContainer<T>,
        Container.Indexed {

    private static final long serialVersionUID = -4031940552175752858L;
    /**
     * Rate on which cache cleanup (of empty weak references to instantiated
     * entities) is performed.
     */
    private static final int CLEANUPRATE = 200;
    private EntityProvider<T> entityProvider;
    private AdvancedFilterableSupport filterSupport;
    private LinkedList<ItemSetChangeListener> listeners;
    private EntityClassMetadata<T> entityClassMetadata;
    private List<SortBy> sortByList;
    private PropertyList<T> propertyList;
    private BufferedContainerDelegate<T> bufferingDelegate;
    private boolean readOnly = false;
    private boolean writeThrough = false;

    transient private HashMap<Object, LinkedList<WeakReference<JPAContainerItem<T>>>> itemRegistry;

    private QueryModifierDelegate queryModifierDelegate;

    /**
     * Creates a new <code>JPAContainer</code> instance for entities of class
     * <code>entityClass</code>. An entity provider must be provided using the
     * {@link #setEntityProvider(com.vaadin.addon.jpacontainer.EntityProvider) }
     * before the container can be used.
     * 
     * @param entityClass
     *            the class of the entities that will reside in this container
     *            (must not be null).
     */
    public JPAContainer(Class<T> entityClass) {
        assert entityClass != null : "entityClass must not be null";
        this.entityClassMetadata = MetadataFactory.getInstance()
                .getEntityClassMetadata(entityClass);
        this.propertyList = new PropertyList<T>(entityClassMetadata);
        this.filterSupport = new AdvancedFilterableSupport();
        this.bufferingDelegate = new BufferedContainerDelegate<T>(this);
        /*
         * Add a listener to filterSupport, so that we can notify all clients
         * that use our container that the data has been filtered.
         */
        this.filterSupport
                .addListener(new AdvancedFilterableSupport.ApplyFiltersListener() {

                    private static final long serialVersionUID = -23196201919497112L;

                    public void filtersApplied(AdvancedFilterableSupport sender) {
                        fireContainerItemSetChange(new FiltersAppliedEvent<JPAContainer<T>>(
                                JPAContainer.this));
                    }
                });
        updateFilterablePropertyIds();
    }

    private Collection<String> additionalFilterablePropertyIds;

    /**
     * Sometimes, it may be necessary to filter by properties that do not show
     * up in the container. This method can be used to add additional property
     * IDs to the {@link #getFilterablePropertyIds() } collection. This method
     * performs no propertyId validation, so it is up to the client to make sure
     * the propertyIds are valid.
     * 
     * @param propertyIds
     *            an array of additional propertyIds, may be null.
     */
    public void setAdditionalFilterablePropertyIds(String... propertyIds) {
        if (propertyIds == null || propertyIds.length == 0) {
            additionalFilterablePropertyIds = null;
        } else {
            additionalFilterablePropertyIds = Arrays.asList(propertyIds);
        }
        updateFilterablePropertyIds();
    }

    protected void updateFilterablePropertyIds() {
        // this.filterSupport
        // .setFilterablePropertyIds((Collection<?>) propertyList
        // .getPersistentPropertyNames());
        HashSet<String> properties = new HashSet<String>();
        properties.addAll(propertyList.getPersistentPropertyNames());
        if (additionalFilterablePropertyIds != null) {
            properties.addAll(additionalFilterablePropertyIds);
        }
        this.filterSupport.setFilterablePropertyIds(properties);
    }

    /**
     * Gets the mapping metadata of the entity class.
     * 
     * @see EntityClassMetadata
     * 
     * @return the metadata (never null).
     */
    protected EntityClassMetadata<T> getEntityClassMetadata() {
        return entityClassMetadata;
    }

    public void addListener(ItemSetChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (listeners == null) {
            listeners = new LinkedList<ItemSetChangeListener>();
        }
        listeners.add(listener);
    }

    public void removeListener(ItemSetChangeListener listener) {
        if (listener != null && listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Publishes <code>event</code> to all registered
     * <code>ItemSetChangeListener</code>s.
     * 
     * @param event
     *            the event to publish (must not be null).
     */
    @SuppressWarnings("unchecked")
    protected void fireContainerItemSetChange(final ItemSetChangeEvent event) {
        assert event != null : "event must not be null";
        if (listeners == null || !fireContainerItemSetChangeEvents) {
            return;
        }
        LinkedList<ItemSetChangeListener> list = (LinkedList<ItemSetChangeListener>) listeners
                .clone();
        for (ItemSetChangeListener l : list) {
            l.containerItemSetChange(event);
        }
    }

    private boolean fireContainerItemSetChangeEvents = true;

    /**
     * Specifies whether the container should fire an item set change event when
     * it detects a change in the entity provider (such as an entity being
     * added, updated or deleted).
     */
    public void setFireContainerItemSetChangeEvents(boolean value) {
        this.fireContainerItemSetChangeEvents = value;
    }

    /**
     * Tests whether the container should fire an item set change event when it
     * detects a change in the entity provider (such as an entity being added,
     * updated or deleted).
     * 
     * @return true if item set change events should be fired (default), false
     *         otherwise.
     */
    public boolean isFireContainerItemSetChangeEvents() {
        return fireContainerItemSetChangeEvents;
    }

    public void addNestedContainerProperty(String nestedProperty)
            throws UnsupportedOperationException {
        propertyList.addNestedProperty(nestedProperty);
        updateFilterablePropertyIds();
    }

    public Class<T> getEntityClass() {
        return getEntityClassMetadata().getMappedClass();
    }

    public EntityProvider<T> getEntityProvider() {
        return entityProvider;
    }

    /**
     * Checks that the entity provider is not null and returns it.
     * 
     * @return the entity provider (never null).
     * @throws IllegalStateException
     *             if the entity provider was null.
     */
    protected EntityProvider<T> doGetEntityProvider()
            throws IllegalStateException {
        if (entityProvider == null) {
            throw new IllegalStateException("No EntityProvider has been set");
        }
        return entityProvider;
    }

    public boolean isReadOnly() {
        return !(doGetEntityProvider() instanceof MutableEntityProvider)
                || readOnly;
    }

    @SuppressWarnings("unchecked")
    public void setEntityProvider(EntityProvider<T> entityProvider) {
        assert entityProvider != null : "entityProvider must not be null";
        // Remove listener from old provider
        if (this.entityProvider != null
                && this.entityProvider instanceof EntityProviderChangeNotifier) {
            ((EntityProviderChangeNotifier<T>) this.entityProvider)
                    .removeListener(this);
        }
        this.entityProvider = entityProvider;
        // Register listener with new provider
        registerProvider();
    }

    @SuppressWarnings("unchecked")
    private void registerProvider() {
        if (this.entityProvider instanceof EntityProviderChangeNotifier) {
            ((EntityProviderChangeNotifier<T>) this.entityProvider)
                    .addListener(this);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        // reattach to weak listener list of provider
        registerProvider();
    }

    private boolean fireItemSetChangeOnProviderChange = true;

    /**
     * Specifies whether the container should fire an ItemSetChangeEvent when an
     * EntityProviderChangeEvent is received. This is used to prevent clients
     * from receiving duplicate ItemSetChangeEvents when the container modifies
     * data and wants to handle ItemSetChangeEvents itself.
     * 
     * @param fireItemSetChangeOnProviderChange
     *            true fo fire an ItemSetChangeEvent when the provider changes,
     *            false not to.
     */
    protected void setFireItemSetChangeOnProviderChange(
            boolean fireItemSetChangeOnProviderChange) {
        this.fireItemSetChangeOnProviderChange = fireItemSetChangeOnProviderChange;
    }

    /**
     * @see #setFireItemSetChangeOnProviderChange(boolean)
     * @return true if an ItemSetChangeEvent should be fired when the provider
     *         changes, false if it should not.
     */
    protected boolean isFireItemSetChangeOnProviderChange() {
        return fireItemSetChangeOnProviderChange;
    }

    public void entityProviderChange(EntityProviderChangeEvent<T> event) {
        if (isItemSetChangeEvent(event)
                && isFireItemSetChangeOnProviderChange()) {
            fireContainerItemSetChange(new ProviderChangedEvent(event));
        } else {
            if (event instanceof EntityPropertyUpdatedEvent) {
                // TODO fire itemSetChange event in case property of a sort
                // column has changed
                EntityPropertyUpdatedEvent<T> evt = (EntityPropertyUpdatedEvent<T>) event;
                Collection<T> affectedEntities = evt.getAffectedEntities();
                if (affectedEntities.isEmpty()) {
                    return;
                }
                for (T t : affectedEntities) {
                    if (entityClassMetadata.hasIdentifierProperty()) {
                        PersistentPropertyMetadata identifierProperty = entityClassMetadata
                                .getIdentifierProperty();
                        Object itemId = entityClassMetadata.getPropertyValue(t,
                                identifierProperty.getName());
                        firePropertyValueChangeEvent(itemId,
                                ((EntityPropertyUpdatedEvent<T>) event)
                                        .getPropertyId());
                    }
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    private void firePropertyValueChangeEvent(Object itemId, String propertyId) {
        LinkedList<WeakReference<JPAContainerItem<T>>> linkedList;
        synchronized (getItemRegistry()) {
            LinkedList<WeakReference<JPAContainerItem<T>>> origList = getItemRegistry()
                    .get(itemId);
            if (origList != null) {
                linkedList = (LinkedList<WeakReference<JPAContainerItem<T>>>) origList
                        .clone();
            } else {
                return;
            }
        }
        for (Iterator<WeakReference<JPAContainerItem<T>>> iterator = linkedList
                .iterator(); iterator.hasNext();) {
            WeakReference<JPAContainerItem<T>> weakReference = iterator.next();
            JPAContainerItem<T> jpaContainerItem = weakReference.get();
            if (jpaContainerItem != null) {
                EntityItemProperty itemProperty = jpaContainerItem
                        .getItemProperty(propertyId);
                itemProperty.fireValueChangeEvent();
            }
        }
    }

    private boolean isItemSetChangeEvent(EntityProviderChangeEvent<T> event) {
        if (event instanceof EntityPropertyUpdatedEvent) {
            return false;
        }
        return true;
    }

    public void setReadOnly(boolean readOnly)
            throws UnsupportedOperationException {
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

    /**
     * Configures a property to be sortable based on another property, normally
     * a sub-property of the main property to sort.
     * <p>
     * For example, let's say there is a property named <code>address</code> and
     * that this property's type in turn has the property <code>street</code>.
     * Addresses are not directly sortable as they are not simple properties.
     * <p>
     * If we want to be able to sort addresses based on the street property, we
     * can set the sort property for <code>address</code> to be
     * <code>address.street</code> using this method.
     * <p>
     * Normally the sort property should be of the form
     * <code>propertyId + "." + subPropertyId</code>. Sort properties must be
     * persistent and usable in JPQL, but need not be registered as separate
     * properties in the container.
     * <p>
     * Note that the sort property is not checked when this method is called. If
     * it is not a valid sort property, an exception will be thrown when trying
     * to sort a container.
     * 
     * @param propertyId
     *            property for which sorting should be configured
     * @param sortProperty
     *            property or other JPQL string that should be used when sorting
     *            by propertyId is requested, typically a sub-property
     *            propertyId
     * @throws IllegalArgumentException
     *             if the property <code>propertyId</code> is not in the
     *             container
     * @since 1.2.1
     */
    public void setSortProperty(String propertyId, String sortProperty)
            throws IllegalArgumentException {
        propertyList.setSortProperty(propertyId, sortProperty);
    }

    public Collection<String> getSortableContainerPropertyIds() {
        // This includes properties for which a separate sort property has been
        // defined.
        return propertyList.getSortablePropertyMap().keySet();
    }

    public void sort(Object[] propertyId, boolean[] ascending) {
        assert propertyId != null : "propertyId must not be null";
        assert ascending != null : "ascending must not be null";
        assert propertyId.length == ascending.length : "propertyId and ascending must have the same length";
        sortByList = new LinkedList<SortBy>();
        for (int i = 0; i < propertyId.length; ++i) {
            if (!getSortableContainerPropertyIds().contains(
                    propertyId[i].toString())) {
                throw new IllegalArgumentException(
                        "No such sortable property ID: " + propertyId[i]);
            }
            // #7711 map property ID to a sortable sub-property if configured
            Object sortProperty = propertyList.getSortablePropertyMap().get(
                    propertyId[i]);
            sortByList.add(new SortBy(sortProperty.toString(), ascending[i]));
        }
        sortByList = Collections.unmodifiableList(sortByList);
        fireContainerItemSetChange(new ContainerSortedEvent());
    }

    /**
     * Gets all the properties that the items should be sorted by, if any.
     * 
     * @return an unmodifiable, possible empty list of <code>SortBy</code>
     *         instances (never null).
     */
    protected List<SortBy> getSortByList() {
        if (sortByList == null) {
            return Collections.emptyList();
        } else {
            return sortByList;
        }
    }

    /**
     * <strong>This functionality is not supported by this
     * implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    public Object addItemAfter(Object previousItemId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * <strong>This functionality is not supported by this
     * implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    public Item addItemAfter(Object previousItemId, Object newItemId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public Object firstItemId() {
        if (isWriteThrough() || bufferingDelegate.getAddedItemIds().isEmpty()) {
            Object itemId = doGetEntityProvider().getFirstEntityIdentifier(
                    this, getAppliedFiltersAsConjunction(), getSortByList());
            if (itemId != null && !isWriteThrough()
                    && bufferingDelegate.getDeletedItemIds().contains(itemId)) {
                itemId = nextItemId(itemId);
            }
            return itemId;
        } else {
            return bufferingDelegate.getAddedItemIds().get(0);
        }
    }

    public boolean isFirstId(Object itemId) {
        assert itemId != null : "itemId must not be null";
        return itemId.equals(firstItemId());
    }

    public boolean isLastId(Object itemId) {
        assert itemId != null : "itemId must not be null";
        return itemId.equals(lastItemId());
    }

    public Object lastItemId() {
        Object itemId = doGetEntityProvider().getLastEntityIdentifier(this,
                getAppliedFiltersAsConjunction(), getSortByList());
        if (isWriteThrough() || bufferingDelegate.getAddedItemIds().isEmpty()) {
            return itemId;
        } else {
            if (itemId == null) {
                return bufferingDelegate.getAddedItemIds().get(
                        bufferingDelegate.getAddedItemIds().size() - 1);
            } else {
                return itemId;
            }
        }
    }

    public Object nextItemId(Object itemId) {
        // Note, we do not check if given itemId is deleted as we use this
        // method recursively to get itemId that is not deleted
        if (isWriteThrough() || bufferingDelegate.getAddedItemIds().isEmpty()
                || !bufferingDelegate.isAdded(itemId)) {
            Object id = doGetEntityProvider().getNextEntityIdentifier(this,
                    itemId, getAppliedFiltersAsConjunction(), getSortByList());
            if (id != null && !isWriteThrough()
                    && bufferingDelegate.isDeleted(id)) {
                id = nextItemId(id);
            }
            return id;
        } else {
            int ix = bufferingDelegate.getAddedItemIds().indexOf(itemId);
            if (ix == bufferingDelegate.getAddedItemIds().size() - 1) {
                Object id = doGetEntityProvider()
                        .getFirstEntityIdentifier(this,
                                getAppliedFiltersAsConjunction(),
                                getSortByList());
                if (id != null && bufferingDelegate.isDeleted(id)) {
                    id = nextItemId(id);
                }
                return id;
            } else {
                return bufferingDelegate.getAddedItemIds().get(ix + 1);
            }
        }
    }

    public Object prevItemId(Object itemId) {
        // Note, we do not check if given itemId is deleted as we use this
        // method recursively to get itemId that is not deleted
        if (isWriteThrough() || bufferingDelegate.getAddedItemIds().isEmpty()) {
            Object id = doGetEntityProvider().getPreviousEntityIdentifier(this,
                    itemId, getAppliedFiltersAsConjunction(), getSortByList());
            if (id != null && !isWriteThrough()
                    && bufferingDelegate.isDeleted(id)) {
                id = prevItemId(id);
            }
            return id;
        } else {
            if (bufferingDelegate.isAdded(itemId)) {
                int ix = bufferingDelegate.getAddedItemIds().indexOf(itemId);
                if (ix == 0) {
                    return null;
                } else {
                    return bufferingDelegate.getAddedItemIds().get(ix - 1);
                }
            } else {
                Object prevId = doGetEntityProvider()
                        .getPreviousEntityIdentifier(this, itemId,
                                getAppliedFiltersAsConjunction(),
                                getSortByList());
                if (prevId == null) {
                    return bufferingDelegate.getAddedItemIds().get(
                            bufferingDelegate.getAddedItemIds().size() - 1);
                } else {
                    if (!isWriteThrough()
                            && bufferingDelegate.isDeleted(prevId)) {
                        prevId = prevItemId(prevId);
                    }
                    return prevId;
                }
            }
        }
    }

    /**
     * <strong>This functionality is not supported by this
     * implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    public boolean addContainerProperty(Object propertyId, Class<?> type,
            Object defaultValue) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * <strong>This functionality is not supported by this
     * implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * <strong>This functionality is not fully supported by this
     * implementation.</strong> The implementation tries to call empty parameter
     * constructor and add entity as such to database. If identifiers are not
     * autogenerated or empty parameter constructor does not exist, the
     * operation will fail and throw UnSupportedOperationException.
     * <p>
     * {@inheritDoc }
     */
    public Object addItem() throws UnsupportedOperationException {
        try {
            T newInstance = getEntityClass().newInstance();
            Object id = addEntity(newInstance);
            return id;
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
        throw new UnsupportedOperationException();
    }

    public boolean containsId(Object itemId) {
        boolean result = doContainsId(itemId);
        if (containsIdFiresItemSetChangeIfNotFound && !result) {
            fireContainerItemSetChange(new ItemNotFoundEvent());
        }
        return result;
    }

    private boolean containsIdFiresItemSetChangeIfNotFound = false;
    private int cleanupCount;

    /**
     * Returns whether the {@link #containsId(java.lang.Object) } method will
     * fire an item set change event if it returns false. This may be necessary
     * when using the container together with a {@link com.vaadin.ui.Table} and
     * there are multiple users modifying the same data source.
     * <p>
     * When a user selects an item in a Table, the table checks with the
     * container if the item exists or not. If it does not exist, nothing
     * happens. Normally, the item should always exist, but if the container has
     * been changed after the initial set of items were fetched and cached by
     * the table, there may be items in the Table that are not present in the
     * container.
     * <p>
     * By enabling this flag, the Table will repaint itself if it tries to
     * select a nonexistent item, causing the item to dissapear from the table
     * as well.
     */
    public boolean isContainsIdFiresItemSetChangeIfNotFound() {
        return containsIdFiresItemSetChangeIfNotFound;
    }

    /**
     * See {@link #isContainsIdFiresItemSetChangeIfNotFound() }.
     * 
     * @param value
     */
    public void setContainsIdFiresItemSetChangeIfNotFound(boolean value) {
        this.containsIdFiresItemSetChangeIfNotFound = value;
    }

    /**
     * @see Container#containsId(java.lang.Object)
     */
    protected boolean doContainsId(Object itemId) {
        if (isWriteThrough()) {
            return doGetEntityProvider().containsEntity(this, itemId,
                    getAppliedFiltersAsConjunction());
        } else {
            return bufferingDelegate.isAdded(itemId)
                    || (!bufferingDelegate.isDeleted(itemId) && doGetEntityProvider()
                            .containsEntity(this, itemId,
                                    getAppliedFiltersAsConjunction()));
        }
    }

    public Property getContainerProperty(Object itemId, Object propertyId) {
        Item item = getItem(itemId);
        return item == null ? null : item.getItemProperty(propertyId);
    }

    public Collection<String> getContainerPropertyIds() {
        return propertyList.getPropertyNames();
    }

    /**
     * Method used by {@link EntityItem} to gain access to the property list.
     * Not to be used by clients directly.
     * 
     * @return the property list.
     */
    PropertyList<T> getPropertyList() {
        // TODO Not sure whether this is a good idea, maybe the property list
        // could be passed to EntityItem as a constructor parameter?
        return propertyList;
    }

    /**
     * {@inheritDoc }
     * <p>
     * Please note, that this method will create a new instance of
     * {@link EntityItem} upon every execution. That is, two subsequent calls to
     * this method with the same <code>itemId</code> will <b>not</b> return the
     * same {@link EntityItem} instance. The actual entity instance may still be
     * the same though, depending on the implementation of the entity provider.
     */
    public EntityItem<T> getItem(Object itemId) {
        if (itemId == null) {
            return null;
        }
        if (isWriteThrough() || !bufferingDelegate.isModified()) {
            T entity = doGetEntityProvider().getEntity(this, itemId);
            return entity != null ? new JPAContainerItem<T>(this, entity)
                    : null;
        } else {
            if (bufferingDelegate.isAdded(itemId)) {
                JPAContainerItem<T> item = new JPAContainerItem<T>(this,
                        bufferingDelegate.getAddedEntity(itemId), itemId, false);
                return item;
            } else if (bufferingDelegate.isUpdated(itemId)) {
                JPAContainerItem<T> item = new JPAContainerItem<T>(this,
                        bufferingDelegate.getUpdatedEntity(itemId));
                item.setDirty(true);
                return item;
            } else if (bufferingDelegate.isDeleted(itemId)) {
                T entity = doGetEntityProvider().getEntity(this, itemId);
                if (entity != null) {
                    JPAContainerItem<T> item = new JPAContainerItem<T>(this,
                            entity);
                    item.setDeleted(true);
                    return item;
                } else {
                    return null;
                }
            } else {
                T entity = doGetEntityProvider().getEntity(this, itemId);
                return entity != null ? new JPAContainerItem<T>(this, entity)
                        : null;
            }
        }
    }

    /**
     * Called by JPAContainerItem when item is created. Container can then keep
     * (weak) references to all instantiated items. Those are needed e.g. for
     * property value changes to happen correctly.
     * 
     * @param item
     */
    void registerItem(JPAContainerItem<T> item) {
        // TODO write tests to ensure the registry gets cleaned up properly
        synchronized (getItemRegistry()) {
            doItemRegistryCleanup();
            LinkedList<WeakReference<JPAContainerItem<T>>> listOfItemsForEntity = itemRegistry
                    .get(item.getItemId());
            if (listOfItemsForEntity == null) {
                listOfItemsForEntity = new LinkedList<WeakReference<JPAContainerItem<T>>>();
                getItemRegistry().put(item.getItemId(), listOfItemsForEntity);
            }
            listOfItemsForEntity.add(new WeakReference<JPAContainerItem<T>>(
                    item));
        }
    }

    private HashMap<Object, LinkedList<WeakReference<JPAContainerItem<T>>>> getItemRegistry() {
        if (itemRegistry == null) {
            itemRegistry = new HashMap<Object, LinkedList<WeakReference<JPAContainerItem<T>>>>();
        }
        return itemRegistry;
    }

    private void doItemRegistryCleanup() {
        final boolean cleanup = (cleanupCount++) % CLEANUPRATE == 0;
        if (cleanup) {
            HashMap<Object, LinkedList<WeakReference<JPAContainerItem<T>>>> itemRegistry = getItemRegistry();
            for (Iterator<Object> idIterator = itemRegistry.keySet().iterator(); idIterator
                    .hasNext();) {
                Object id = idIterator.next();
                LinkedList<WeakReference<JPAContainerItem<T>>> linkedList = itemRegistry
                        .get(id);
                for (Iterator<WeakReference<JPAContainerItem<T>>> iterator = linkedList
                        .iterator(); iterator.hasNext();) {
                    WeakReference<JPAContainerItem<T>> ref = iterator.next();
                    if (ref.get() == null) {
                        iterator.remove();
                    }
                }
                if (linkedList.isEmpty()) {
                    idIterator.remove();
                }
            }
        }
    }

    /**
     * <strong>This impementation does not use lazy loading and performs bad
     * when the number of items is large! Do not use unless you absolutely have
     * to!</strong>
     * <p>
     * {@inheritDoc }
     */
    public Collection<Object> getItemIds() {
        Collection<Object> ids = getEntityProvider().getAllEntityIdentifiers(
                this, getAppliedFiltersAsConjunction(), getSortByList());
        if (isWriteThrough() || !bufferingDelegate.isModified()) {
            return ids;
        } else {
            List<Object> newIds = new LinkedList<Object>();
            newIds.addAll(ids);
            newIds.addAll(bufferingDelegate.getAddedItemIds());
            newIds.removeAll(bufferingDelegate.getDeletedItemIds());
            return Collections.unmodifiableCollection(newIds);
        }
    }

    public EntityItem<T> createEntityItem(T entity) {
        return new JPAContainerItem<T>(this, entity, null, false);
    }

    public Class<?> getType(Object propertyId) {
        assert propertyId != null : "propertyId must not be null";
        return propertyList.getPropertyType(propertyId.toString());
    }

    public boolean removeContainerProperty(Object propertyId)
            throws UnsupportedOperationException {
        assert propertyId != null : "propertyId must not be null";
        boolean result = propertyList.removeProperty(propertyId.toString());
        updateFilterablePropertyIds();
        return result;
    }

    public int size() {
        int origSize = doGetEntityProvider().getEntityCount(this,
                getAppliedFiltersAsConjunction());
        if (isWriteThrough()) {
            return origSize;
        } else {
            int newSize = origSize + bufferingDelegate.getAddedItemIds().size()
                    - bufferingDelegate.getDeletedItemIds().size();
            return newSize;
        }
    }

    /**
     * Returns a conjunction (filter1 AND filter2 AND ... AND filterN) of all
     * the applied filters. If there are no applied filters, this method returns
     * null.
     * 
     * @see #getAppliedFilters()
     * @return a conjunction filter or null.
     */
    protected Filter getAppliedFiltersAsConjunction() {
        if (getAppliedFilters().isEmpty()) {
            return null;
        } else if (getAppliedFilters().size() == 1) {
            return getAppliedFilters().iterator().next();
        } else {
            return new And(CollectionUtil.toArray(Filter.class,
                    getAppliedFilters()));
        }
    }

    public Collection<Object> getFilterablePropertyIds() {
        return filterSupport.getFilterablePropertyIds();
    }

    public boolean isFilterable(Object propertyId) {
        return filterSupport.isFilterable(propertyId);
    }

    public List<Filter> getFilters() {
        return filterSupport.getFilters();
    }

    public List<Filter> getAppliedFilters() {
        return filterSupport.getAppliedFilters();
    }

    public void setApplyFiltersImmediately(boolean applyFiltersImmediately) {
        filterSupport.setApplyFiltersImmediately(applyFiltersImmediately);
    }

    public boolean isApplyFiltersImmediately() {
        return filterSupport.isApplyFiltersImmediately();
    }

    public void applyFilters() {
        filterSupport.applyFilters();
    }

    public boolean hasUnappliedFilters() {
        return filterSupport.hasUnappliedFilters();
    }

    public void addContainerFilter(Object propertyId, String filterString,
            boolean ignoreCase, boolean onlyMatchPrefix) {
        addContainerFilter(new SimpleStringFilter(propertyId, filterString,
                ignoreCase, onlyMatchPrefix));
        if (!isApplyFiltersImmediately()) {
            applyFilters();
        }
    }

    public void removeAllContainerFilters() {
        filterSupport.removeAllFilters();
    }

    public void removeContainerFilters(Object propertyId) {
        removeAllContainerFilters();
        applyFilters();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <strong>Note</strong> that JPAContainer don't support custom
     * {@link Filter}s as filtering is done on DB level. Only basic Filter
     * implementations are supported. If more complex filtering is needed,
     * developers should tend to {@link QueryModifierDelegate} that allows
     * developers to use JPA Criteria API to modify queries.
     * 
     * @see com.vaadin.data.Container.Filterable#addContainerFilter(com.vaadin.data.Container.Filter)
     */
    public void addContainerFilter(Filter filter)
            throws UnsupportedFilterException {
        filterSupport.addFilter(filter);
    }

    public void removeContainerFilter(Filter filter) {
        filterSupport.removeFilter(filter);
    }

    /**
     * <strong>This functionality is not supported by this
     * implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    public Object addItemAt(int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * <strong>This functionality is not supported by this
     * implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    public Item addItemAt(int index, Object newItemId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public Object getIdByIndex(int index) {
        if (isWriteThrough()) {
            return doGetEntityProvider().getEntityIdentifierAt(this,
                    getAppliedFiltersAsConjunction(), getSortByList(), index);
        } else {
            int addedItems = bufferingDelegate.getAddedItemIds().size();
            if (index < addedItems) {
                return bufferingDelegate.getAddedItemIds().get(index);
            } else {
                index -= addedItems;
                index = bufferingDelegate.fixDbIndexWithDeletedItems(index);
                Object itemId = doGetEntityProvider().getEntityIdentifierAt(
                        this, getAppliedFiltersAsConjunction(),
                        getSortByList(), index);
                return itemId;
            }
        }
    }

    /**
     * <strong>This implementation does not use lazy loading and performs
     * <b>extremely</b> bad when the number of items is large! Do not use unless
     * you absolutely have to!</strong>
     * <p>
     * {@inheritDoc }
     */
    public int indexOfId(Object itemId) {
        /*
         * This is intentionally an ugly implementation! This method should not
         * be used!
         */
        int size = size();
        if (size > 100) {
            Logger.getLogger(getClass().getName())
                    .warning(
                            "(JPAContainer) WARNING! Invoking indexOfId() when size > 100 is not recommended!");
        }
        for (int i = 0; i < size; i++) {
            Object id = getIdByIndex(i);
            if (id == null) {
                return -1;
            } else if (id.equals(itemId)) {
                if (!isWriteThrough() && bufferingDelegate.isDeleted(id)) {
                    return -1;
                }
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks that the container is writable, i.e. the entity provider
     * implements the {@link MutableEntityProvider} interface and the container
     * is not marked as read only.
     * 
     * @throws IllegalStateException
     *             if the container is read only.
     * @throws UnsupportedOperationException
     *             if the entity provider does not support editing.
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

    public Object addEntity(T entity) throws UnsupportedOperationException,
            IllegalStateException {
        assert entity != null : "entity must not be null";
        requireWritableContainer();

        Object id;
        if (isWriteThrough()) {
            T result = ((MutableEntityProvider<T>) getEntityProvider())
                    .addEntity(entity);
            id = getEntityClassMetadata().getPropertyValue(result,
                    getEntityClassMetadata().getIdentifierProperty().getName());
        } else {
            id = bufferingDelegate.addEntity(entity);
        }
        setFireItemSetChangeOnProviderChange(false); // Prevent the container
        // from firing duplicate
        // events
        try {
            fireContainerItemSetChange(new ItemAddedEvent(id));
        } finally {
            setFireItemSetChangeOnProviderChange(true);
        }
        return id;
    }

    /**
     * <strong>This feature is not well optimized. Using direct access to db is
     * much faster.</strong>
     * <p>
     * {@inheritDoc }
     */
    public boolean removeAllItems() {
        try {
            Collection<Object> itemIds = getItemIds();
            for (Object id : itemIds) {
                removeItem(id);
            }
            if (!isWriteThrough()) {
                commit();
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean removeItem(Object itemId)
            throws UnsupportedOperationException {
        assert itemId != null : "itemId must not be null";
        requireWritableContainer();

        if (isWriteThrough()) {
            if (getEntityProvider().containsEntity(this, itemId, null)) {
                ((MutableEntityProvider<T>) getEntityProvider())
                        .removeEntity(itemId);
                setFireItemSetChangeOnProviderChange(false);
                try {
                    fireContainerItemSetChange(new ItemRemovedEvent(itemId));
                } finally {
                    setFireItemSetChangeOnProviderChange(true);
                }
                return true;
            } else {
                return false;
            }
        } else {
            if (bufferingDelegate.isAdded(itemId)
                    || getEntityProvider().containsEntity(this, itemId, null)) {
                bufferingDelegate.deleteItem(itemId);
                setFireItemSetChangeOnProviderChange(false);
                try {
                    fireContainerItemSetChange(new ItemRemovedEvent(itemId));
                } finally {
                    setFireItemSetChangeOnProviderChange(true);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * This method is used by the {@link JPAContainerItem} class and <b>should
     * not be used by other classes</b>. It is only called when the item is in
     * write through mode, i.e. when an updated property value is directly
     * reflected in the backed entity instance. If the item is in buffered mode
     * (write through is off), {@link #containerItemModified(JPAContainerItem)}
     * is used instead.
     * <p>
     * This method notifies the container that the specified property of
     * <code>item</code> has been modified. The container will then take
     * appropriate actions to pass the changes on to the entity provider,
     * depending on the state of the <code>writeThrough</code> property <i>of
     * the container</i>.
     * <p>
     * If <code>item</code> has no item ID ({@link JPAContainerItem#getItemId() }
     * ), this method does nothing.
     * 
     * @see #isWriteThrough()
     * @param item
     *            the item that has been modified (must not be null).
     * @param propertyId
     *            the ID of the modified property (must not be null).
     */
    void containerItemPropertyModified(JPAContainerItem<T> item,
            String propertyId) {
        assert item != null : "item must not be null";
        assert propertyId != null : "propertyId must not be null";

        if (item.getItemId() != null) {

            requireWritableContainer();

            Object itemId = item.getItemId();
            if (isWriteThrough()) {
                ((MutableEntityProvider<T>) getEntityProvider())
                        .updateEntityProperty(itemId, propertyId, item
                                .getItemProperty(propertyId).getValue());
                item.setDirty(false);
            } else {
                bufferingDelegate.updateEntity(itemId, item.getEntity());
            }
        }
    }

    /**
     * This method is used by the {@link JPAContainerItem} class and <b>should
     * not be used by other classes</b>. It is only called when the item is in
     * buffered mode (write through is off), i.e. when updated property values
     * are not reflected in the backend entity instance until the item's commit
     * method has been invoked. If write through is turned on,
     * {@link #containerItemPropertyModified(JPAContainerItem, String)} is used
     * instead.
     * <p>
     * This method notifies the container that the specified <code>item</code>
     * has been modified. The container will then take appropriate actions to
     * pass the changes on to the entity provider, depending on the state of the
     * <code>writeThrough</code> property <i>of the container</i>.
     * <p>
     * If <code>item</code> has no item ID ({@link JPAContainerItem#getItemId() }
     * ), this method does nothing.
     * 
     * @see #isWriteThrough()
     * @param item
     *            the item that has been modified (must not be null).
     */
    void containerItemModified(JPAContainerItem<T> item) {
        assert item != null : "item must not be null";

        if (item.getItemId() != null) {
            requireWritableContainer();

            Object itemId = item.getItemId();
            if (isWriteThrough()) {
                ((MutableEntityProvider<T>) getEntityProvider())
                        .updateEntity(item.getEntity());
                item.setDirty(false);
            } else {
                bufferingDelegate.updateEntity(itemId, item.getEntity());
            }
        }
    }

    public void commit() throws SourceException, InvalidValueException {
        if (!isWriteThrough() && isModified()) {
            bufferingDelegate.commit();
            setFireItemSetChangeOnProviderChange(false);
            try {
                fireContainerItemSetChange(new ChangesCommittedEvent());
            } finally {
                setFireItemSetChangeOnProviderChange(true);
            }
        }
    }

    public void discard() throws SourceException {
        if (!isWriteThrough() && isModified()) {
            bufferingDelegate.discard();
            setFireItemSetChangeOnProviderChange(false);
            try {
                fireContainerItemSetChange(new ChangesDiscardedEvent());
            } finally {
                setFireItemSetChangeOnProviderChange(true);
            }
        }
    }

    public boolean isModified() {
        if (isWriteThrough()) {
            return false;
        } else {
            return bufferingDelegate.isModified();
        }
    }

    public boolean isReadThrough() {
        EntityProvider<T> ep = doGetEntityProvider();
        if (ep instanceof CachingEntityProvider) {
            return !((CachingEntityProvider<T>) ep).isCacheEnabled();
        }
        return true; // There is no cache at all
    }

    public boolean isWriteThrough() {
        return !(doGetEntityProvider() instanceof BatchableEntityProvider)
                || writeThrough;
    }

    /**
     * <strong>This functionality is not supported by this
     * implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    public void setReadThrough(boolean readThrough) throws SourceException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc }
     * <p>
     * <b>Note</b>, that write-through mode can only be turned off if the entity
     * provider implements the {@link BatchableEntityProvider} interface.
     */
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

    public void setAutoCommit(boolean autoCommit) throws SourceException,
            InvalidValueException {
        setWriteThrough(autoCommit);
    }

    public boolean isAutoCommit() {
        return isWriteThrough();
    }

    private String parentProperty;

    private String parentIdProperty;

    public String getParentProperty() {
        return parentProperty;
    }

    public void setParentProperty(String parentProperty) {
        this.parentProperty = parentProperty;
        if (parentProperty == null) {
            parentIdProperty = null;
        } else {
            StringBuilder sb = new StringBuilder(parentProperty);
            sb.append('.');
            sb.append(getEntityClassMetadata().getIdentifierProperty()
                    .getName());
            parentIdProperty = sb.toString();
        }
    }

    public boolean areChildrenAllowed(Object itemId) {
        assert itemId != null : "itemId must not be null";
        return parentProperty != null && containsId(itemId);
    }

    private Filter getChildrenFilter(Object parentId) {
        Filter parentFilter;
        if (parentId == null) {
            parentFilter = new IsNull(parentProperty);
        } else {
            parentFilter = new Equal(parentIdProperty, parentId);
        }
        Filter appliedFilter = getAppliedFiltersAsConjunction();
        if (appliedFilter == null) {
            return parentFilter;
        } else {
            return new And(parentFilter, appliedFilter);
        }
    }

    public Collection<?> getChildren(Object itemId) {
        if (getParentProperty() == null) {
            if (itemId == null) {
                return getItemIds();
            } else {
                return Collections.emptyList();
            }
        } else {
            return doGetEntityProvider().getAllEntityIdentifiers(this,
                    getChildrenFilter(itemId), getSortByList());
        }
    }

    public Object getParent(Object itemId) {
        if (parentProperty == null) {
            return null;
        } else {
            EntityItem<T> item = getItem(itemId);
            @SuppressWarnings("unchecked")
            T parent = item == null ? null : (T) item.getItemProperty(
                    parentProperty).getValue();
            if (parent == null) {
                return null;
            } else {
                return getEntityClassMetadata().getPropertyValue(
                        parent,
                        getEntityClassMetadata().getIdentifierProperty()
                                .getName());
            }
        }
    }

    public boolean hasChildren(Object itemId) {
        return !getChildren(itemId).isEmpty();
    }

    public boolean isRoot(Object itemId) {
        return getParent(itemId) == null;
    }

    public Collection<?> rootItemIds() {
        return getChildren(null);
    }

    /**
     * <strong>This functionality is not supported by this
     * implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * <strong>This functionality is not supported by this
     * implementation.</strong>
     * <p>
     * {@inheritDoc }
     */
    public boolean setParent(Object itemId, Object newParentId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Event indicating that the container has been resorted.
     * 
     * @author Petter Holmström (Vaadin Ltd)
     * @since 1.0
     */
    public final class ContainerSortedEvent implements ItemSetChangeEvent {

        private static final long serialVersionUID = -4330673683011445634L;

        protected ContainerSortedEvent() {
        }

        public Container getContainer() {
            return JPAContainer.this;
        }
    }

    /**
     * Event indicating that the changes have been committed. It will be fired
     * when the container has write-through/auto-commit turned off and
     * {@link JPAContainer#commit()} is called.
     * 
     * @author Petter Holmström (Vaadin Ltd)
     * @since 1.0
     */
    public final class ChangesCommittedEvent implements ItemSetChangeEvent {

        private static final long serialVersionUID = -7802570988994951818L;

        protected ChangesCommittedEvent() {
        }

        public Container getContainer() {
            return JPAContainer.this;
        }
    }

    /**
     * Event indicating that the changes have been discarded. This event is
     * fired when the container has write-through/auto-commit turned off and
     * {@link JPAContainer#discard() } is called.
     * 
     * @author Petter Holmström (Vaadin Ltd)
     * @since 1.0
     */
    public final class ChangesDiscardedEvent implements ItemSetChangeEvent {

        private static final long serialVersionUID = 1192258036968002982L;

        protected ChangesDiscardedEvent() {
        }

        public Container getContainer() {
            return JPAContainer.this;
        }
    }

    /**
     * Event indicating that all the items have been removed from the container.
     * This event is fired by {@link JPAContainer#removeAllItems() }.
     * 
     * @author Petter Holmström (Vaadin Ltd)
     * @since 1.0
     */
    public final class AllItemsRemovedEvent implements ItemSetChangeEvent {

        private static final long serialVersionUID = -7429226164483121998L;

        protected AllItemsRemovedEvent() {
        }

        public Container getContainer() {
            return JPAContainer.this;
        }
    }

    /**
     * Event fired by {@link JPAContainer#containsId(java.lang.Object) } when the
     * result is false and {@link #isContainsIdFiresItemSetChangeIfNotFound() }
     * is true.
     * 
     * @author Petter Holmström (Vaadin Ltd)
     * @since 1.0
     */
    public final class ItemNotFoundEvent implements ItemSetChangeEvent {

        private static final long serialVersionUID = 7542676056363040711L;

        protected ItemNotFoundEvent() {
        }

        public Container getContainer() {
            return JPAContainer.this;
        }
    }

    /**
     * Event fired when a {@link EntityProviderChangeEvent} is received by the
     * container.
     * 
     * @author Petter Holmström (Vaadin Ltd)
     * @since 1.0
     */
    public final class ProviderChangedEvent implements ItemSetChangeEvent {

        private static final long serialVersionUID = -2719424959990430585L;
        private final EntityProviderChangeEvent<?> event;

        protected ProviderChangedEvent(EntityProviderChangeEvent<?> event) {
            this.event = event;
        }

        public Container getContainer() {
            return JPAContainer.this;
        }

        /**
         * Gets the {@link EntityProviderChangeEvent} that caused this container
         * event to be fired.
         */
        public EntityProviderChangeEvent<?> getChangeEvent() {
            return event;
        }
    }

    /**
     * Abstract base class for events concerning single {@link EntityItem}s.
     * 
     * @author Petter Holmström (Vaadin Ltd)
     * @since 1.0
     */
    public abstract class ItemEvent implements ItemSetChangeEvent {

        private static final long serialVersionUID = -7867054889972105067L;
        protected final Object itemId;

        protected ItemEvent(Object itemId) {
            this.itemId = itemId;
        }

        public Container getContainer() {
            return JPAContainer.this;
        }

        /**
         * Gets the ID of the item that this event concerns.
         * 
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
     * @author Petter Holmström (Vaadin Ltd)
     * @since 1.0
     */
    public final class ItemAddedEvent extends ItemEvent {

        private static final long serialVersionUID = 197074826066153230L;

        protected ItemAddedEvent(Object itemId) {
            super(itemId);
        }
    }

    /**
     * Event indicating that an item has been updated inside the container.
     * 
     * @author Petter Holmström (Vaadin Ltd)
     * @since 1.0
     */
    public final class ItemUpdatedEvent extends ItemEvent {

        private static final long serialVersionUID = 4464120712728895566L;

        protected ItemUpdatedEvent(Object itemId) {
            super(itemId);
        }
    }

    /**
     * Event indicating that an item has been removed from the container. This
     * event is fired by {@link JPAContainer#removeItem(java.lang.Object) }.
     * 
     * @author Petter Holmström (Vaadin Ltd)
     * @since 1.0
     */
    public final class ItemRemovedEvent extends ItemEvent {

        private static final long serialVersionUID = 530688830477630703L;

        protected ItemRemovedEvent(Object itemId) {
            super(itemId);
        }
    }

    public final class AllItemsRefreshedEvent implements ItemSetChangeEvent {

        private static final long serialVersionUID = 530180436710345623L;

        protected AllItemsRefreshedEvent() {
        }

        public Container getContainer() {
            return JPAContainer.this;
        }
    }

    public PropertyKind getPropertyKind(Object propertyId) {
        assert propertyId != null : "propertyId must not be null";
        return propertyList.getPropertyKind(propertyId.toString());
    }

    @SuppressWarnings("unchecked")
    public void refreshItem(Object itemId) {
        LinkedList<WeakReference<JPAContainerItem<T>>> linkedList = null;
        synchronized (getItemRegistry()) {
            LinkedList<WeakReference<JPAContainerItem<T>>> origList = getItemRegistry()
                    .get(itemId);
            if (origList != null) {
                linkedList = (LinkedList<WeakReference<JPAContainerItem<T>>>) origList
                        .clone();
            }
        }
        if (linkedList != null) {
            for (WeakReference<JPAContainerItem<T>> weakReference : linkedList) {
                JPAContainerItem<T> jpaContainerItem = weakReference.get();
                if (jpaContainerItem != null) {
                    jpaContainerItem.refresh();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.addon.jpacontainer.EntityContainer#refresh()
     */
    public void refresh() {
        doGetEntityProvider().refresh();
        bufferingDelegate.discard();
        synchronized (getItemRegistry()) {
            for (Object id : getItemRegistry().keySet().toArray()) {
                refreshItem(id);
            }
        }
        fireContainerItemSetChange(new AllItemsRefreshedEvent());
    }

    public QueryModifierDelegate getQueryModifierDelegate() {
        return queryModifierDelegate;
    }

    /**
     * Sets the {@link QueryModifierDelegate}, which is called in the different
     * stages that the EntityProvider builds a criteria query.
     * 
     * @param queryModifierDelegate
     *            the delegate.
     */
    public void setQueryModifierDelegate(
            QueryModifierDelegate queryModifierDelegate) {
        this.queryModifierDelegate = queryModifierDelegate;
    }

    public void setBuffered(boolean buffered) {
        // setReadThrough is an unsupported operation, so just set write
        // through.
        setWriteThrough(!buffered);
    }

    public boolean isBuffered() {
        return !isReadThrough() && isWriteThrough();
    }

    public void addItemSetChangeListener(ItemSetChangeListener listener) {
        addListener(listener);
    }

    public void removeItemSetChangeListener(ItemSetChangeListener listener) {
        removeListener(listener);
    }

    public List<?> getItemIds(int startIndex, int numberOfItems)
    {
	if (isWriteThrough())
	{
	    return doGetEntityProvider().getEntityIdentifierAt(this, getAppliedFiltersAsConjunction(), getSortByList(),
		    startIndex, numberOfItems);
	}
	int addedItems = bufferingDelegate.getAddedItemIds().size();
	ArrayList<Object> ids = new ArrayList<Object>();

	for (; startIndex < numberOfItems && numberOfItems > 0 && addedItems > startIndex; startIndex++)
	{
	    ids.add(bufferingDelegate.getAddedItemIds().get(startIndex));
	    numberOfItems--;
	}
	if (numberOfItems > 0)
	{
	    startIndex -= addedItems;
	    startIndex = bufferingDelegate.fixDbIndexWithDeletedItems(startIndex);
	    ids.addAll(doGetEntityProvider().getEntityIdentifierAt(this, getAppliedFiltersAsConjunction(),
		    getSortByList(), startIndex, numberOfItems));

	}
	return ids;

    }

    @Override
    public Collection<Filter> getContainerFilters() {
        return filterSupport.getAppliedFilters();
    }
}

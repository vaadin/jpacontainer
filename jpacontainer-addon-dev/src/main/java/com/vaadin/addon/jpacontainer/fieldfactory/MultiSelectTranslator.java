package com.vaadin.addon.jpacontainer.fieldfactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.ManyToMany;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.addon.jpacontainer.metadata.EntityClassMetadata;
import com.vaadin.addon.jpacontainer.metadata.MetadataFactory;
import com.vaadin.addon.jpacontainer.metadata.PropertyKind;
import com.vaadin.addon.jpacontainer.metadata.PropertyMetadata;
import com.vaadin.data.Property;
import com.vaadin.ui.AbstractSelect;

/**
 * Wrapper property that translates collection of entities to collection of
 * identifiers and visa versa. Expects that the translator is used in a select
 * backed by a jpacontainer.
 */
public class MultiSelectTranslator extends PropertyTranslator {

    private final AbstractSelect select;
    private Boolean owningSide;
    private String mappedBy;

    public MultiSelectTranslator(AbstractSelect select) {
        this.select = select;
    }

    @SuppressWarnings({ "rawtypes" })
    private EntityContainer getContainer() {
        return (EntityContainer) select.getContainerDataSource();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object translateFromDatasource(Object value) {
        // Value here is a collection of entities, should be transformed to a
        // collection (set) of identifier
        // TODO, consider creating a cached value
        HashSet<Object> identifiers = new HashSet<Object>();
        Collection<Object> entities = (Collection<Object>) value;
        for (Object entity : entities) {
            Object identifier = getContainer().getEntityProvider()
                    .getIdentifier(entity);
            identifiers.add(identifier);
        }
        return identifiers;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Object translateToDatasource(Object formattedValue) throws Exception {
        
        // NOTE, this currently works properly only if equals and hashcode
        // methods have been implemented correctly (both depending on identifier
        // of the entity)
        // TODO create a filter that has a workaround for invalid
        // equals/hashCode

        // formattedValue here is a set of identifiers.
        // We will modify the existing collection of entities to contain
        // corresponding entities
        Collection idset = (Collection) formattedValue;

        Collection value = (Collection) getPropertyDataSource().getValue();

        if (value == null) {
            value = createNewCollectionForType(getPropertyDataSource()
                    .getType());
        }

        HashSet orphaned = new HashSet(value);

        // Add those that did not exist do not exist already + remove them from
        // orphaned collection
        for (Object id : idset) {
            Object entity = getContainer().getEntityProvider().getEntity(id);
            if (!value.contains(entity)) {
                value.add(entity);
                addBackReference(entity);
            }
            orphaned.remove(entity);
        }

        // remove orphanded
        for (Object entity : orphaned) {
            value.remove(entity);
            removeBackReference(entity);
        }

        if (!isOwningSide()) {
            // refresh the item as modifying back references may also have
            // changed the collections, without this we'd get concurrent
            // modification exception.
            getPropertyDataSource().getItem().refresh();
        }
        return value;
    }

    @SuppressWarnings({ "rawtypes" })
    private void removeBackReference(Object entity) {
        if (!isOwningSide()) {
            EntityItemProperty itemProperty = getBackReferenceItemProperty(entity);
            Collection c = (Collection) itemProperty.getValue();
            c.remove(getPropertyDataSource().getItem().getEntity());
            itemProperty.setValue(c);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private EntityItemProperty getBackReferenceItemProperty(Object entity) {
        EntityItem item = getContainer().getItem(
                getContainer().getEntityProvider().getIdentifier(entity));
        EntityItemProperty itemProperty = item.getItemProperty(mappedBy);
        return itemProperty;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void addBackReference(Object entity) {
        if (!isOwningSide()) {
            EntityItemProperty itemProperty = getBackReferenceItemProperty(entity);
            Collection c = (Collection) itemProperty.getValue();
            c.add(
                    getPropertyDataSource().getItem().getEntity());
            itemProperty.setValue(c);
        }
    }

    /**
     * Checks if the manytomany relation is owned by this side of the property.
     * As a side effect detects the name of the owner property if the relation
     * is owned by the other side.
     * 
     * @return false if bidirectional connection and the mapping has a mappedBy
     *         parameter.
     */
    private boolean isOwningSide() {
        if (owningSide == null) {
            Class<?> entityClass = getPropertyDataSource().getItem()
                    .getContainer().getEntityClass();
            EntityClassMetadata<?> entityClassMetadata = MetadataFactory
                    .getInstance().getEntityClassMetadata(entityClass);
            PropertyMetadata property = entityClassMetadata
                    .getProperty(getPropertyDataSource().getPropertyId());
            ManyToMany annotation = property.getAnnotation(ManyToMany.class);
            if (annotation.mappedBy() != null
                    && !annotation.mappedBy().isEmpty()) {
                owningSide = Boolean.FALSE;
                mappedBy = annotation.mappedBy();
                return owningSide;
            }
            owningSide = Boolean.TRUE;
        }
        return owningSide;
    }

    @Override
    public EntityItemProperty getPropertyDataSource() {
        return (EntityItemProperty) super.getPropertyDataSource();
    }

    static Collection createNewCollectionForType(Class<?> type)
            throws InstantiationException, IllegalAccessException {
        if (type.isInterface()) {
            if (type == Set.class) {
                return new HashSet();
            } else if (type == List.class) {
                return new ArrayList();
            } else {
                throw new RuntimeException(
                        "Couldn't instantiate a collection for property.");
            }
        } else {
            return (Collection) type.newInstance();
        }
    }

}

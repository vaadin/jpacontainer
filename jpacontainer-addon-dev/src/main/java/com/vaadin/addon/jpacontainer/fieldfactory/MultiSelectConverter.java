package com.vaadin.addon.jpacontainer.fieldfactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.ManyToMany;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.addon.jpacontainer.metadata.EntityClassMetadata;
import com.vaadin.addon.jpacontainer.metadata.MetadataFactory;
import com.vaadin.addon.jpacontainer.metadata.PropertyMetadata;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.AbstractSelect;

public class MultiSelectConverter<T> implements
        Converter<Collection<Object>, Collection<T>> {

    private final AbstractSelect select;
    private Boolean owningSide;
    private String mappedBy;

    public MultiSelectConverter(AbstractSelect select) {
        this.select = select;
    }

    @SuppressWarnings("unchecked")
    private EntityContainer<T> getContainer() {
        return (EntityContainer<T>) select.getContainerDataSource();
    }

    public Collection<Object> convertToPresentation(Collection<T> value,
            Locale locale)
            throws com.vaadin.data.util.converter.Converter.ConversionException {
        // Value here is a collection of entities, should be transformed to a
        // collection (set) of identifier
        // TODO, consider creating a cached value
        HashSet<Object> identifiers = new HashSet<Object>();
        for (T entity : value) {
            Object identifier = getContainer().getEntityProvider()
                    .getIdentifier(entity);
            identifiers.add(identifier);
        }
        return identifiers;
    }

    public Collection<T> convertToModel(Collection<Object> value, Locale locale)
            throws com.vaadin.data.util.converter.Converter.ConversionException {

        // NOTE, this currently works properly only if equals and hashcode
        // methods have been implemented correctly (both depending on identifier
        // of the entity)
        // TODO create a filter that has a workaround for invalid
        // equals/hashCode

        // formattedValue here is a set of identifiers.
        // We will modify the existing collection of entities to contain
        // corresponding entities
        Collection<Object> idset = (Collection<Object>) value;

        Collection<T> modelValue = (Collection<T>) getPropertyDataSource()
                .getValue();

        if (modelValue == null) {
            try {
                modelValue = createNewCollectionForType(getPropertyDataSource()
                        .getType());
            } catch (Exception e) {
                throw new ConversionException(e);
            }
        }

        HashSet<T> orphaned = new HashSet<T>(modelValue);

        // Add those that did not exist do not exist already + remove them from
        // orphaned collection
        for (Object id : idset) {
            T entity = getContainer().getEntityProvider().getEntity(
                    getContainer(), id);
            if (!modelValue.contains(entity)) {
                modelValue.add(entity);
                addBackReference(entity);
            }
            orphaned.remove(entity);
        }

        // remove orphanded
        for (T entity : orphaned) {
            modelValue.remove(entity);
            removeBackReference(entity);
        }

        if (!isOwningSide()) {
            // refresh the item as modifying back references may also have
            // changed the collections, without this we'd get concurrent
            // modification exception.
            getPropertyDataSource().getItem().refresh();
        }
        return modelValue;
    }

    private EntityItemProperty getPropertyDataSource() {
        return (EntityItemProperty) select.getPropertyDataSource();
    }

    private void removeBackReference(T entity) {
        if (!isOwningSide()) {
            EntityItemProperty itemProperty = getBackReferenceItemProperty(entity);
            Collection c = (Collection) itemProperty.getValue();
            c.remove(getPropertyDataSource().getItem().getEntity());
            itemProperty.setValue(c);
        }
    }

    private EntityItemProperty getBackReferenceItemProperty(T entity) {
        EntityItem item = getContainer().getItem(
                getContainer().getEntityProvider().getIdentifier(entity));
        EntityItemProperty itemProperty = item.getItemProperty(mappedBy);
        return itemProperty;
    }

    private void addBackReference(T entity) {
        if (!isOwningSide()) {
            EntityItemProperty itemProperty = getBackReferenceItemProperty(entity);
            Collection c = (Collection) itemProperty.getValue();
            c.add(getPropertyDataSource().getItem().getEntity());
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

    public Class<Collection<T>> getModelType() {
        return getPropertyDataSource().getType();
    }

    public Class<Collection<Object>> getPresentationType() {
        return getPropertyDataSource().getType();
    }

}

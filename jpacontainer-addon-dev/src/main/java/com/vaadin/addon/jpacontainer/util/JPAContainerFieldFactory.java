package com.vaadin.addon.jpacontainer.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.JPAContainerItem;
import com.vaadin.addon.jpacontainer.metadata.PropertyKind;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Table;

/**
 * A helper class for JPAContainer users. E.g. automatically creates selects for
 * reference fields to another entity.
 * 
 * TODO collection types, open for extension
 */
public class JPAContainerFieldFactory extends DefaultFieldFactory {

    private EntityManagerFactory emfFactory;
    private EntityManager em;

    /**
     * Creates a new JPAContainerFieldFactory. For referece/collection types
     * ComboBox or multiselects are created by default.
     * 
     * @param emfFactory
     *            the emfFactory that will be used to create EntityManagers for
     *            JPAContainers that are needed.
     */
    public JPAContainerFieldFactory(EntityManagerFactory emfFactory) {
        this.emfFactory = emfFactory;
    }
    
    /**
     * Creates a new JPAContainerFieldFactory. For referece/collection types
     * ComboBox or multiselects are created by default.
     * 
     * @param persistenceUnitName
     *            the name of persiscenceUnit that will be used by default to create JPAContainers needed by fields
     */
    public JPAContainerFieldFactory(String persistenceUnitName) {
        this.emfFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
    }


    /**
     * Creates a new JPAContainerFieldFactory. For referece/collection types
     * ComboBox or multiselects are created by default.
     * 
     * @param emfFactory
     *            the emfFactory that will be used to create EntityManagers for
     *            JPAContainers that are needed.
     */
    public JPAContainerFieldFactory(EntityManager em) {
        this.em = em;
    }

    @Override
    public Field createField(Item item, Object propertyId, Component uiContext) {
        if (item instanceof JPAContainerItem) {
            JPAContainerItem jpaitem = (JPAContainerItem) item;
            PropertyKind propertyKind = jpaitem.getContainer().getPropertyKind(
                    propertyId);
            EntityContainer container = jpaitem.getContainer();

            Field field = createJPAContainerBackedField(propertyId, container);
            if (field != null) {
                return field;
            }
        }
        return super.createField(item, propertyId, uiContext);
    }

    @Override
    public Field createField(Container container, Object itemId,
            Object propertyId, Component uiContext) {
        if (container instanceof EntityContainer) {
            EntityContainer jpacontainer = (EntityContainer) container;
            Field field = createJPAContainerBackedField(propertyId,
                    jpacontainer);
            if (field != null) {
                return field;
            }
        }
        return super.createField(container, itemId, propertyId, uiContext);
    }

    private Field createJPAContainerBackedField(Object propertyId,
            EntityContainer jpacontainer) {
        Field field = null;
        PropertyKind propertyKind = jpacontainer.getPropertyKind(propertyId);
        switch (propertyKind) {
        case REFERENCE:
            field = createReferenceSelect(jpacontainer, propertyId);
            break;
        case COLLECTION: 
            field = createCollectionSelect(jpacontainer, propertyId);
            break;
        default:
            break;
        }
        return field;
    }

    private Field createCollectionSelect(EntityContainer containerForProperty,
            Object propertyId) {
        Class<?> type = containerForProperty.getType(propertyId);
        JPAContainer container = createJPAContainerFor(type);
        Table select = new Table(
                DefaultFieldFactory.createCaptionByPropertyId(propertyId),
                container);
        select.setPropertyDataSource(new SingleSelectTranslator(
                select));
        select.setMultiSelect(true);
        return select;
    }

    /**
     * Creates a field for simple reference (ManyToOne)
     * 
     * @param containerForProperty
     * @param propertyId
     * @return
     */
    protected Field createReferenceSelect(EntityContainer containerForProperty,
            Object propertyId) {
        Class<?> type = containerForProperty.getType(propertyId);
        JPAContainer container = createJPAContainerFor(type);
        NativeSelect nativeSelect = new NativeSelect(
                DefaultFieldFactory.createCaptionByPropertyId(propertyId),
                container);
        nativeSelect.setPropertyDataSource(new SingleSelectTranslator(
                nativeSelect));
        nativeSelect.setItemCaptionMode(NativeSelect.ITEM_CAPTION_MODE_ITEM);
        return nativeSelect;
    }

    private JPAContainer createJPAContainerFor(Class<?> type) {
        if (emfFactory != null) {
            return JPAContainerFactory.makeReadOnly(type,
                    emfFactory.createEntityManager());
        } else if (em != null) {
            return JPAContainerFactory.makeReadOnly(type, em);
        }
        throw new IllegalStateException(
                "Either entitymanager or an entitymanagerfactory must be defined");
    }

}

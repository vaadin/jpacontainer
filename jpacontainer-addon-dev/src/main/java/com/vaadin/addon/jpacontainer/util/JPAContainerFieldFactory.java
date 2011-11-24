package com.vaadin.addon.jpacontainer.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.JPAContainerItem;
import com.vaadin.addon.jpacontainer.metadata.PropertyKind;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
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
     *            the name of persiscenceUnit that will be used by default to
     *            create JPAContainers needed by fields
     */
    public JPAContainerFieldFactory(String persistenceUnitName) {
        this.emfFactory = Persistence
                .createEntityManagerFactory(persistenceUnitName);
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

            Field field = createJPAContainerBackedField(jpaitem.getItemId(),
                    propertyId, container);
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
            Field field = createJPAContainerBackedField(itemId, propertyId,
                    jpacontainer);
            if (field != null) {
                return field;
            }
        }
        return super.createField(container, itemId, propertyId, uiContext);
    }

    private Field createJPAContainerBackedField(Object itemId,
            Object propertyId, EntityContainer jpacontainer) {
        Field field = null;
        PropertyKind propertyKind = jpacontainer.getPropertyKind(propertyId);
        switch (propertyKind) {
        case REFERENCE:
            field = createReferenceSelect(jpacontainer, propertyId);
            break;
        case COLLECTION:
            field = createCollectionSelect(jpacontainer, itemId, propertyId);
            break;
        default:
            break;
        }
        return field;
    }

    @SuppressWarnings({ "rawtypes", "serial" })
    private Field createCollectionSelect(EntityContainer containerForProperty,
            Object itemId, Object propertyId) {
        boolean oneToMany = false;
        Type elementType = null;
        Class referencedType = null;
        Class masterEntityClass = containerForProperty.getEntityClass();
        Metamodel metamodel = emfFactory.getMetamodel();
        Set<EntityType<?>> entities = metamodel.getEntities();
        for (EntityType<?> entityType : entities) {
            Class<?> javaType = entityType.getJavaType();
            if (javaType == masterEntityClass) {
                Attribute<?, ?> attribute = entityType.getAttribute(propertyId
                        .toString());
                PersistentAttributeType persistentAttributeType = attribute
                        .getPersistentAttributeType();
                PluralAttribute pAttribute = (PluralAttribute) attribute;
                elementType = pAttribute.getElementType();
                referencedType = elementType.getJavaType();
                if (persistentAttributeType == PersistentAttributeType.ONE_TO_MANY) {
                    oneToMany = true;
                }
                break;
            }
        }
        final JPAContainer container = createJPAContainerFor(referencedType);
        final Table table = new Table(
                DefaultFieldFactory.createCaptionByPropertyId(propertyId),
                container);
        if (oneToMany) {
            // oneToMany means we want a 'master-detail-view'

            // Modify container to filter only those details that relate to
            // this master data
            // TODO should use mappedBy parameter of OneToMany annotation,
            // currently using convention
            final String backReferencePropertyId = masterEntityClass
                    .getSimpleName().toLowerCase();
            final Object masterEntity = containerForProperty
                    .getEntityProvider().getEntity(itemId);
            Filter filter = new Compare.Equal(backReferencePropertyId,
                    masterEntity);
            container.addContainerFilter(filter);

            List<Object> asList = new ArrayList<Object>(Arrays.asList(table
                    .getVisibleColumns()));
            asList.remove("id");
            asList.remove(backReferencePropertyId);
            table.setVisibleColumns(asList.toArray());

            final Action add = new Action(getMasterDetailAddItemCaption());
            // add add and remove actions to table
            Action remove = new Action(getMasterDetailRemoveItemCaption());
            final Action[] actions = new Action[] { add, remove };

            table.addActionHandler(new Handler() {

                @SuppressWarnings("unchecked")
                public void handleAction(Action action, Object sender,
                        Object target) {
                    if (action == add) {
                        try {
                            Object newInstance = container.getEntityClass()
                                    .newInstance();
                            BeanItem beanItem = new BeanItem(newInstance);
                            beanItem.getItemProperty(backReferencePropertyId)
                                    .setValue(masterEntity);
                            // TODO need to update the actual property also!?
                            container.addEntity(newInstance);
                        } catch (InstantiationException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else {
                        table.removeItem(target);
                    }
                }

                public Action[] getActions(Object target, Object sender) {
                    return actions;
                }
            });
            table.setEditable(true);

        } else {
            // many to many, selectable from table listing all existing pojos
            table.setPropertyDataSource(new MultiSelectTranslator(table));
            table.setSelectable(true);
            table.setMultiSelect(true);
            List<Object> asList = new ArrayList<Object>(Arrays.asList(table
                    .getVisibleColumns()));
            asList.remove("id");
            // TODO this should be the true "back reference" field from the
            // opposite direction, now we expect convention
            final String backReferencePropertyId = masterEntityClass
                    .getSimpleName().toLowerCase() + "s";
            asList.remove(backReferencePropertyId);
            table.setVisibleColumns(asList.toArray());
        }
        return table;
    }

    private String getMasterDetailRemoveItemCaption() {
        return "Remove";
    }

    private String getMasterDetailAddItemCaption() {
        return "Add";
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
            return JPAContainerFactory.make(type,
                    emfFactory.createEntityManager());
        } else if (em != null) {
            return JPAContainerFactory.make(type, em);
        }
        throw new IllegalStateException(
                "Either entitymanager or an entitymanagerfactory must be defined");
    }

}

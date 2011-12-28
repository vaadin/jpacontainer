package com.vaadin.addon.jpacontainer.fieldfactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.JPAContainerItem;
import com.vaadin.addon.jpacontainer.metadata.PropertyKind;
import com.vaadin.addon.jpacontainer.util.EntityManagerPerRequestHelper;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;

/**
 * A {@link FormFieldFactory} and {@link TableFieldFactory} implementation
 * suitable for JPAContainer users.
 * <p>
 * As where the {@link DefaultFieldFactory} in Vaadin can only handle basic data
 * types, this field factory can also automatically create proper fields for
 * various referenced entities. This greatly speeds up construction of CRUD
 * views. Below are field types supported by this FieldFactory:
 * <p>
 * 
 * <dl>
 * <di><b>@ManyToOne</b></di>
 * <dd>
 * Creates a select backed up by a JPAContainer listing all entities of
 * referenced type. A {@link SingleSelectTranslator} is used to automatically
 * convert identifiers to actual referenced entity objects.
 * <p>
 * Example of a mapped property: @ManyToOne private Address address;<br>
 * Default type: {@link NativeSelect}
 * <p>
 * Created by
 * {@link #createManyToOneField(EntityContainer, Object, Object, Component)}
 * method.
 * <p>
 * The method
 * {@link #constructReferenceSelect(EntityContainer, Object, Object, Component, Class)}
 * can be used to override the select type. The type can also be set per
 * reference type with {@link #setMultiSelectType(Class, Class)}.
 * <p></dd>
 * <di><b>@ManyToMany</b></di>
 * <dd>
 * Creates a multiselect backed up by a JPAContainer listing all entities of the
 * type in the collection. Selected entities will be reflected to the collection
 * field in the entity using the {@link MultiSelectTranslator}.
 * <p>
 * Example of a mapped property: @ManyToMany private Set&lt;Address&gt;
 * addresses;
 * <p>
 * Default type: {@link Table} (in multiselect mode)
 * <p>
 * Created by
 * {@link #createManyToManyField(EntityContainer, Object, Object, Component)}
 * method.
 * <p>
 * The method
 * {@link #constructCollectionSelect(EntityContainer, Object, Object, Component, Class)}
 * can be used to override the select type. Type can also be set per reference
 * type with {@link #setMultiSelectType(Class, Class)}.
 * <p></dd>
 * <di><b>@OneToMany</b></di>
 * <dd>
 * Creates a custom field based on Table to edit referenced entities "owned" by
 * the master entity. The table lists only entities which belong to entity being
 * currently edited. Referenced entities are editable straight in the table and
 * instances can be removed and added.
 * <p>
 * Example of a mapped property: @OneToMany(mappedBy="person",
 * cascade=CascadeType.ALL, orphanRemoval = true) private Set&lt;Address&gt;
 * addresses;
 * <p>
 * Default type: {@link MasterDetailEditor} (in multiselect mode)
 * <p>
 * Created by
 * {@link #createOneToManyField(EntityContainer, Object, Object, Component)}
 * method.
 * <p>
 * Some things to note:
 * <ul>
 * <li>Creation of new entities uses empty paramater constructor.</li>
 * <li>The master detail editor expects the referenced entity to hava a
 * "back reference" to the owner. It needs to be specified in with mappedBy
 * parameter in the annotation or it to be "naturally named" (master type
 * starting in lowercase).</li>
 * </ul>
 * <p></dd>
 * <di><b>@OneToOne</b></di>
 * <dd>
 * Creates a sub form for the referenced type. If the value is initially null,
 * the sub form tries to create one with empty parameter constructor.
 * <p>
 * Example of a mapped property: @OneToOne private Address addresses;
 * <p>
 * 
 * Default type: {@link OneToOneForm}
 * <p>
 * Created by
 * {@link #createOneToOneField(EntityContainer, Object, Object, Component)}
 * method.
 * <p>
 * 
 * </dd>
 * </dl>
 * 
 * <p>
 * FieldFactory works recursively. E.g. sub forms or {@link MasterDetailEditor}s
 * it creates uses the same fieldfactory by default. When using the class in
 * such conditions one often wants to use
 * {@link #setVisibleProperties(Class, String...)} to configure created fields.
 * 
 */
@SuppressWarnings("rawtypes")
public class FieldFactory extends DefaultFieldFactory {

    private HashMap<Class<?>, String[]> propertyOrders;
    private EntityManagerPerRequestHelper entityManagerPerRequestHelper;
    private HashMap<Class<?>, Class<? extends AbstractSelect>> multiselectTypes;
    private HashMap<Class<?>, Class<? extends AbstractSelect>> singleselectTypes;

    /**
     * Creates a new instance of a {@link FieldFactory}.
     */
    public FieldFactory() {
    }

    /**
     * Creates a new JPAContainerFieldFactory. For referece/collection types
     * ComboBox or multiselects are created by default.
     * 
     * @param emprHelper
     *            the {@link EntityManagerPerRequestHelper} to use for updating
     *            the entity manager in internally generated JPAContainers for
     *            each request.
     */
    public FieldFactory(EntityManagerPerRequestHelper emprHelper) {
        setEntityManagerPerRequestHelper(emprHelper);
    }

    @Override
    public Field createField(Item item, Object propertyId, Component uiContext) {
        if (item instanceof JPAContainerItem) {
            JPAContainerItem jpaitem = (JPAContainerItem) item;
            EntityContainer container = jpaitem.getContainer();

            Field field = createJPAContainerBackedField(jpaitem.getItemId(),
                    propertyId, container, uiContext);
            if (field != null) {
                return field;
            }
        }
        if ("id".equals(propertyId)) {
            return createIdentifierField();
        }
        return configureBasicFields(super.createField(item, propertyId,
                uiContext));
    }

    /**
     * This method creates field for identifier property. The default
     * implementation does nothing. We expect identifiers to be assigned
     * automatically. This method can be overridden to change the behavior.
     * 
     * @return the field for identifier property
     */
    protected Field createIdentifierField() {
        return null;
    }

    /**
     * This method can be used to configure field generated by the
     * DefaultFieldFactory. By default it sets null representation of textfields
     * to empty string instead of 'null'.
     * 
     * @param field
     * @return
     */
    protected Field configureBasicFields(Field field) {
        if (field instanceof AbstractTextField) {
            ((AbstractTextField) field).setNullRepresentation("");
        }
        return field;
    }

    @Override
    public Field createField(Container container, Object itemId,
            Object propertyId, Component uiContext) {
        if (container instanceof EntityContainer) {
            EntityContainer jpacontainer = (EntityContainer) container;
            Field field = createJPAContainerBackedField(itemId, propertyId,
                    jpacontainer, uiContext);
            if (field != null) {
                return field;
            }
        }
        return configureBasicFields(super.createField(container, itemId,
                propertyId, uiContext));
    }

    private Field createJPAContainerBackedField(Object itemId,
            Object propertyId, EntityContainer jpacontainer, Component uiContext) {
        Field field = null;
        PropertyKind propertyKind = jpacontainer.getPropertyKind(propertyId);
        switch (propertyKind) {
        case MANY_TO_ONE:
            field = createManyToOneField(jpacontainer, itemId, propertyId,
                    uiContext);
            break;
        case ONE_TO_ONE:
            field = createOneToOneField(jpacontainer, itemId, propertyId,
                    uiContext);
            break;
        case ONE_TO_MANY:
            field = createOneToManyField(jpacontainer, itemId, propertyId,
                    uiContext);
            break;
        case MANY_TO_MANY:
            field = createManyToManyField(jpacontainer, itemId, propertyId,
                    uiContext);
            break;
        default:
            break;
        }
        return field;
    }

    protected OneToOneForm createOneToOneField(EntityContainer<?> jpacontainer,
            Object itemId, Object propertyId, Component uiContext) {
        OneToOneForm oneToOneForm = new OneToOneForm();
        oneToOneForm.setBackReferenceId(jpacontainer.getEntityClass()
                .getSimpleName().toLowerCase());
        oneToOneForm.setCaption(DefaultFieldFactory
                .createCaptionByPropertyId(propertyId));
        oneToOneForm.setFormFieldFactory(this);
        if (uiContext instanceof Form) {
            // write buffering is configure by Form after binding the data
            // source. Yes, you may read the previous sentence again or verify
            // this from the Vaadin code if you don't believe what you just
            // read.
            // As oneToOneForm creates the referenced type on demand if required
            // the buffering state needs to be available when proeprty is set
            // (otherwise the original master entity will be modified once the
            // form is opened).
            Form f = (Form) uiContext;
            oneToOneForm.setWriteThrough(f.isWriteThrough());
        }
        return oneToOneForm;
    }

    @SuppressWarnings({ "serial" })
    protected Field createManyToManyField(EntityContainer containerForProperty,
            Object itemId, Object propertyId, Component uiContext) {
        /*
         * Detect what kind of reference type we have
         */
        Class masterEntityClass = containerForProperty.getEntityClass();
        Class referencedType = detectReferencedType(
                getEntityManagerFactory(containerForProperty), propertyId,
                masterEntityClass);
        final JPAContainer container = createJPAContainerFor(
                containerForProperty, referencedType, false);
        final AbstractSelect select = constructCollectionSelect(
                containerForProperty, itemId, propertyId, uiContext,
                referencedType);
        select.setCaption(DefaultFieldFactory
                .createCaptionByPropertyId(propertyId));
        select.setContainerDataSource(container);
        // many to many, selectable from table listing all existing pojos
        select.setPropertyDataSource(new MultiSelectTranslator(select));
        select.setMultiSelect(true);
        if (select instanceof Table) {
            Table t = (Table) select;
            t.setSelectable(true);
            Object[] visibleProperties = getVisibleProperties(referencedType);
            if (visibleProperties == null) {
                List<Object> asList = new ArrayList<Object>(Arrays.asList(t
                        .getVisibleColumns()));
                asList.remove("id");
                // TODO this should be the true "back reference" field from the
                // opposite direction, now we expect convention
                String simpleName = masterEntityClass.getSimpleName();
                String backrefpropname = simpleName.substring(0, 1)
                        .toLowerCase() + simpleName.substring(1);
                final String backReferencePropertyId = backrefpropname + "s";
                asList.remove(backReferencePropertyId);
                visibleProperties = asList.toArray();
            }
            t.setVisibleColumns(visibleProperties);
        } else {
            select.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_ITEM);
        }

        return select;
    }

    protected Field createOneToManyField(EntityContainer containerForProperty,
            Object itemId, Object propertyId, Component uiContext) {
        return new MasterDetailEditor(this, containerForProperty, itemId,
                propertyId, uiContext);
    }

    /**
     * Detects the type entities in "collection types" (oneToMany, ManyToMany).
     * 
     * @param propertyId
     * @param masterEntityClass
     * @return the type of entities in collection type
     */
    protected Class detectReferencedType(EntityManagerFactory emf,
            Object propertyId, Class masterEntityClass) {
        Class referencedType = null;
        Metamodel metamodel = emf.getMetamodel();
        Set<EntityType<?>> entities = metamodel.getEntities();
        for (EntityType<?> entityType : entities) {
            Class<?> javaType = entityType.getJavaType();
            if (javaType == masterEntityClass) {
                Attribute<?, ?> attribute = entityType.getAttribute(propertyId
                        .toString());
                PluralAttribute pAttribute = (PluralAttribute) attribute;
                Type elementType = pAttribute.getElementType();
                referencedType = elementType.getJavaType();
                break;
            }
        }
        return referencedType;
    }

    protected EntityManagerFactory getEntityManagerFactory(
            EntityContainer<?> containerForProperty) {
        return containerForProperty.getEntityProvider().getEntityManager()
                .getEntityManagerFactory();
    }

    /**
     * Creates a field for simple reference (ManyToOne)
     * 
     * @param containerForProperty
     * @param propertyId
     * @return
     */
    protected Field createManyToOneField(EntityContainer containerForProperty,
            Object itemId, Object propertyId, Component uiContext) {
        Class<?> type = containerForProperty.getType(propertyId);
        JPAContainer container = createJPAContainerFor(containerForProperty,
                type, false);

        AbstractSelect nativeSelect = constructReferenceSelect(
                containerForProperty, itemId, propertyId, uiContext, type);
        nativeSelect.setMultiSelect(false);
        nativeSelect.setCaption(DefaultFieldFactory
                .createCaptionByPropertyId(propertyId));
        nativeSelect.setItemCaptionMode(NativeSelect.ITEM_CAPTION_MODE_ITEM);
        nativeSelect.setContainerDataSource(container);
        nativeSelect.setPropertyDataSource(new SingleSelectTranslator(
                nativeSelect));
        return nativeSelect;
    }

    protected AbstractSelect constructReferenceSelect(
            EntityContainer containerForProperty, Object itemId,
            Object propertyId, Component uiContext, Class<?> type) {
        if (singleselectTypes != null) {
            Class<? extends AbstractSelect> class1 = singleselectTypes
                    .get(type);
            if (class1 != null) {
                try {
                    return class1.newInstance();
                } catch (Exception e) {
                    Logger.getLogger(getClass().getName()).warning(
                            "Could not create select of type "
                                    + class1.getName());
                }
            }
        }
        return new NativeSelect();
    }

    protected AbstractSelect constructCollectionSelect(
            EntityContainer containerForProperty, Object itemId,
            Object propertyId, Component uiContext, Class<?> type) {
        if (multiselectTypes != null) {
            Class<? extends AbstractSelect> class1 = multiselectTypes.get(type);
            try {
                return class1.newInstance();
            } catch (Exception e) {
                Logger.getLogger(getClass().getName()).warning(
                        "Could not create select of type " + class1.getName());
            }
        }
        return new Table();
    }

    protected EntityManager getEntityManagerFor(
            EntityContainer<?> containerForProperty) {
        return containerForProperty.getEntityProvider().getEntityManager();
    }

    protected JPAContainer<?> createJPAContainerFor(
            EntityContainer<?> containerForProperty, Class<?> type,
            boolean buffered) {
        JPAContainer<?> container = null;
        EntityManager em = getEntityManagerFor(containerForProperty);
        if (buffered) {
            container = JPAContainerFactory.makeBatchable(type, em);
        } else {
            container = JPAContainerFactory.make(type, em);
        }
        // Set the lazy loading delegate to the same as the parent.
        container.getEntityProvider().setLazyLoadingDelegate(
                containerForProperty.getEntityProvider()
                        .getLazyLoadingDelegate());
        if (entityManagerPerRequestHelper != null) {
            entityManagerPerRequestHelper.addContainer(container);
        }
        return container;
    }

    /**
     * Configures visible properties and their order for fields created for
     * reference/collection types referencing to given entity type. This order
     * is for example used by {@link Table}s created for OneToMany or ManyToMany
     * reference types.
     * 
     * @param containerType
     *            the entity type for which the visible properties will be set
     * @param propertyIdentifiers
     *            the identifiers in wished order to be displayed
     */
    public void setVisibleProperties(Class<?> containerType,
            String... propertyIdentifiers) {
        if (propertyOrders == null) {
            propertyOrders = new HashMap<Class<?>, String[]>();
        }
        propertyOrders.put(containerType, propertyIdentifiers);
    }

    public void setMultiSelectType(Class<?> referenceType,
            Class<? extends AbstractSelect> selectType) {
        if (multiselectTypes == null) {
            multiselectTypes = new HashMap<Class<?>, Class<? extends AbstractSelect>>();
        }
        multiselectTypes.put(referenceType, selectType);
    }

    public void setSingleSelectType(Class<?> referenceType,
            Class<? extends AbstractSelect> selectType) {
        if (singleselectTypes == null) {
            singleselectTypes = new HashMap<Class<?>, Class<? extends AbstractSelect>>();
        }
        singleselectTypes.put(referenceType, selectType);
    }

    /**
     * Returns customized visible properties (and their order) for given entity
     * type.
     * 
     * @param containerType
     * @return property identifiers that are configured to be displayed
     */
    public String[] getVisibleProperties(Class<?> containerType) {
        if (propertyOrders != null) {
            return propertyOrders.get(containerType);
        }
        return null;
    }

    /**
     * @return The {@link EntityManagerPerRequestHelper} that is used for
     *         updating the entity managers for all JPAContainers generated by
     *         this field factory.
     */
    public EntityManagerPerRequestHelper getEntityManagerPerRequestHelper() {
        return entityManagerPerRequestHelper;
    }

    /**
     * Sets the {@link EntityManagerPerRequestHelper} that is used for updating
     * the entity manager of JPAContainers generated by this field factory.
     * 
     * @param entityManagerPerRequestHelper
     */
    public void setEntityManagerPerRequestHelper(
            EntityManagerPerRequestHelper entityManagerPerRequestHelper) {
        this.entityManagerPerRequestHelper = entityManagerPerRequestHelper;
    }
}
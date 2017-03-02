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
package com.vaadin.addon.jpacontainer.fieldfactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.Embedded;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.EntityManagerProvider;
import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.JPAContainerItem;
import com.vaadin.addon.jpacontainer.LazyLoadingDelegate;
import com.vaadin.addon.jpacontainer.metadata.PropertyKind;
import com.vaadin.addon.jpacontainer.provider.jndijta.CachingBatchableEntityProvider;
import com.vaadin.addon.jpacontainer.provider.jndijta.CachingMutableEntityProvider;
import com.vaadin.addon.jpacontainer.provider.jndijta.JndiJtaProvider;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.ui.AbstractSelect;
import com.vaadin.v7.ui.AbstractTextField;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.DefaultFieldFactory;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.Form;
import com.vaadin.v7.ui.FormFieldFactory;
import com.vaadin.v7.ui.NativeSelect;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.TableFieldFactory;

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
 * <dt><b>@ManyToOne</b></dt>
 * <dd>
 * Creates a select backed up by a JPAContainer listing all entities of
 * referenced type. An id->pojo converter is used to automatically convert
 * identifiers to actual referenced entity objects.
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
 * <dt><b>@ManyToMany</b></dt>
 * <dd>
 * Creates a multiselect backed up by a JPAContainer listing all entities of the
 * type in the collection. Selected entities will be reflected to the collection
 * field in the entity using an id->pojo converter.
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
 * <dt><b>@OneToMany</b></dt>
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
 * <li>Creation of new entities uses empty parameter constructor.</li>
 * <li>The master detail editor expects the referenced entity to have a
 * "back reference" to the owner. It needs to be specified in with mappedBy
 * parameter in the annotation or it to be "naturally named" (master type
 * starting in lower case).</li>
 * <li>Map type is not currently supported by the {@link MasterDetailEditor}.</li>
 * </ul>
 * <p></dd>
 * <dt><b>@OneToOne</b></dt>
 * <dd>
 * Creates a sub form for the referenced type. If the value is initially null,
 * the sub form tries to create one with empty parameter constructor.
 * <p>
 * Example of a mapped property: @OneToOne private Address address;
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
 * <dt><b>@Embedded</b></dt>
 * <dd>
 * Creates a sub form for the referenced embedded type. This is closely related
 * to @OneToOne reference. If the value is initially null, the sub form tries to
 * create one with empty parameter constructor.
 * <p>
 * Example of a mapped property: @Embedded private Address address;
 * <p>
 * 
 * Default type: {@link EmbeddedForm}
 * <p>
 * Created by
 * {@link #createEmbeddedField(EntityContainer, Object, Object, Component)}
 * method.</dd>
 * 
 * <dt><b>@ElementCollection</b></dt>
 * <dd>
 * Creates a custom field based on Table to edit referenced embeddables "owned"
 * by the master entity. Embeddables can be basic datatypes or types annotated
 * with @Embeddable annotation. This is closely related to the master detail
 * editor created for OneToMany relations. The table lists embeddables in
 * element collection. Referenced entities are editable straight in the table
 * and instances can be removed and added.
 * <p>
 * Example of a mapped property: @ElementCollection private Set&lt;Address&gt;
 * addresses;
 * <p>
 * Default type: {@link ElementCollectionEditor}
 * <p>
 * Created by
 * {@link #createElementCollectionField(EntityContainer, Object, Object, Component)}
 * method.
 * <p>
 * Note that creation of new elements uses empty paramater constructor. Also the
 * {@link ElementCollectionEditor} does not currently support Map type element
 * collection.</dd>
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
    private HashMap<Class<?>, Class<? extends AbstractSelect>> multiselectTypes;
    private HashMap<Class<?>, Class<? extends AbstractSelect>> singleselectTypes;
    private EntityManagerProvider entityManagerProvider;

    /**
     * Creates a new instance of a {@link FieldFactory}.
     */
    public FieldFactory() {
    }

    @Override
    public Field createField(Item item, Object propertyId, Component uiContext) {
        Field field;
        if (item instanceof JPAContainerItem) {
            JPAContainerItem jpaitem = (JPAContainerItem) item;
            EntityContainer container = jpaitem.getContainer();

            field = createJPAContainerBackedField(jpaitem.getItemId(),
                    propertyId, container, uiContext);
            if (field != null) {
                return field;
            }
        } else {
            field = createRelationFieldForEmbeddableEditor(item, propertyId,
                    uiContext);
            if (field != null) {
                return field;
            }
        }
        if ("id".equals(propertyId)) {
            return createIdentifierField();
        }
        field = createEnumSelect(item.getItemProperty(propertyId).getType(),
                propertyId);
        if (field == null) {
            field = super.createField(item, propertyId, uiContext);
        }
        return configureBasicFields(field);
    }

    /**
     * @param type
     * @param propertyId
     * @return
     */
    protected Field createEnumSelect(Class<?> type, Object propertyId) {
        if (type.isEnum()) {
            AbstractSelect select = constructCollectionSelect(null, null,
                    propertyId, null, type);
            populateEnums(type, select);
            select.setCaption(DefaultFieldFactory
                    .createCaptionByPropertyId(propertyId));
            return select;
        }
        return null;
    }

    private void populateEnums(Class<?> type, AbstractSelect select) {
        List<?> asList = Arrays.asList(type.getEnumConstants());
        for (Object object : asList) {
            select.addItem(object);
        }
    }

    @SuppressWarnings("unchecked")
    private Field createRelationFieldForEmbeddableEditor(Item item,
            Object propertyId, Component uiContext) {
        EmbeddableEditor embeddableEditor = getEmbeddableEditor(uiContext);
        if (embeddableEditor == null) {
            return null;
        }
        Class<?> embeddedClassType = embeddableEditor.getEmbeddedClassType();

        PropertyKind propertyKind = detectPropertyKind(embeddedClassType,
                propertyId);

        switch (propertyKind) {
        case MANY_TO_ONE:
            JPAContainer container = createJPAContainerFor(
                    embeddableEditor.getMasterEntityContainer(), item
                            .getItemProperty(propertyId).getType(), false);

            AbstractSelect select = constructReferenceSelect(
                    embeddableEditor.getMasterEntityContainer(), null,
                    propertyId, uiContext, embeddedClassType);
            select.setMultiSelect(false);
            select.setCaption(DefaultFieldFactory
                    .createCaptionByPropertyId(propertyId));
            select.setItemCaptionMode(NativeSelect.ITEM_CAPTION_MODE_ITEM);
            select.setContainerDataSource(container);
            select.setConverter(new SingleSelectConverter(select));
            return select;

        default:
            break;
        }

        return null;
    }

    private PropertyKind detectPropertyKind(Class<?> embeddedClassType,
            Object propertyId) {
        java.lang.reflect.Field[] declaredFields = embeddedClassType
                .getDeclaredFields();
        for (java.lang.reflect.Field field : declaredFields) {
            if (field.getName().equals(propertyId)) {
                if (field.getAnnotation(ManyToOne.class) != null) {
                    return PropertyKind.MANY_TO_ONE;
                } else if (field.getAnnotation(Embedded.class) != null) {
                    // TODO embedded in embedded ?
                } else if (field.getAnnotation(ManyToMany.class) != null) {
                    // TODO unidirectional multiselect is a possible use case
                    // also?
                }
                break;
            }
        }
        return PropertyKind.SIMPLE;
    }

    private EmbeddableEditor getEmbeddableEditor(Component uiContext) {
        if (uiContext == null) {
            return null;
        }
        if (uiContext instanceof EmbeddableEditor) {
            return (EmbeddableEditor) uiContext;

        }
        return getEmbeddableEditor(uiContext.getParent());
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
        Field field;
        if (container instanceof EntityContainer) {
            EntityContainer jpacontainer = (EntityContainer) container;
            field = createJPAContainerBackedField(itemId, propertyId,
                    jpacontainer, uiContext);
            if (field != null) {
                return field;
            }
        } else {
            field = createRelationFieldForEmbeddableEditor(
                    container.getItem(itemId), propertyId, uiContext);
            if (field != null) {
                return field;
            }

        }
        field = createEnumSelect(container.getType(propertyId), propertyId);
        if (field == null) {
            field = super.createField(container, itemId, propertyId, uiContext);
        }
        return configureBasicFields(field);
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
        case ELEMENT_COLLECTION:
            field = createElementCollectionField(jpacontainer, itemId,
                    propertyId, uiContext);
            break;
        case EMBEDDED:
            field = createEmbeddedField(jpacontainer, itemId, propertyId,
                    uiContext);
            break;
        default:
            break;
        }
        return field;
    }

    protected Field createEmbeddedField(EntityContainer jpacontainer,
            Object itemId, Object propertyId, Component uiContext) {
        // embedded fields are displayed in a sub form
        EmbeddedForm embeddedForm = new EmbeddedForm(this, jpacontainer);
        embeddedForm.setCaption(DefaultFieldFactory
                .createCaptionByPropertyId(propertyId));
        return embeddedForm;
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
            oneToOneForm.setBuffered(f.isBuffered());
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
        select.setConverter(new MultiSelectConverter(select));
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

    protected Field createElementCollectionField(
            EntityContainer containerForProperty, Object itemId,
            Object propertyId, Component uiContext) {

        Class referencedType = detectReferencedType(containerForProperty
                .getEntityProvider().getEntityManager()
                .getEntityManagerFactory(), propertyId,
                containerForProperty.getEntityClass());
        if (referencedType.isEnum()) {
            AbstractSelect collectionSelect = constructCollectionSelect(null,
                    itemId, propertyId, uiContext, referencedType);
            collectionSelect.setCaption(DefaultFieldFactory
                    .createCaptionByPropertyId(propertyId));
            populateEnums(referencedType, collectionSelect);
            collectionSelect.setMultiSelect(true);
            if (List.class.isAssignableFrom(containerForProperty
                    .getType(propertyId))) {
                collectionSelect.setPropertyDataSource(new ListTranslator(
                        collectionSelect));
            }

            if (collectionSelect instanceof Table) {
                Table t = (Table) collectionSelect;
                t.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
                t.setSelectable(true);
                t.setRowHeaderMode(Table.ROW_HEADER_MODE_ID);
            }
            return collectionSelect;
        } else {
            return new ElementCollectionEditor(this, containerForProperty,
                    itemId, propertyId, uiContext);
        }

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
        nativeSelect.setConverter(new SingleSelectConverter(nativeSelect));
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
        Table table = new Table();
        table.setPageLength(5);
        return table;
    }

    /**
     * This method creates new JPAContainer instances to be used in fields
     * generated by this FieldFactory.
     * <p>
     * After setting up the container (with provider) the method configures it
     * with {@link #configureContainer(EntityContainer, JPAContainer)} method.
     * <p>
     * If you need to use JPAContaiener with some special settings (e.g.
     * customized EntityProvider) you should override this method.
     * 
     * @param referenceContainer
     *            most commonly this is the container for which property the
     *            field is being created. The default implementation uses this
     *            extensively to decide the new JPAContainer should be
     *            configured.
     * @param type
     *            the entity type to be listed in the container
     * @param buffered
     *            true if the container should be "buffered" (e.g. for a
     *            {@link MasterDetailEditor} that is used in a buffered Form).
     * @return new JPAContainer for given type that is to be used in a relation
     *         field
     */
    @SuppressWarnings("unchecked")
    protected JPAContainer<?> createJPAContainerFor(
            EntityContainer<?> referenceContainer, Class<?> type,
            boolean buffered) {
        JPAContainer<?> container = null;
        EntityProvider<?> referenceEntityProvider = referenceContainer
                .getEntityProvider();
        if (referenceEntityProvider instanceof JndiJtaProvider) {
            JndiJtaProvider jndiProvider = (JndiJtaProvider) referenceEntityProvider;
            container = new JPAContainer(type);
            JndiJtaProvider entityProvider;
            if (buffered) {
                entityProvider = new CachingBatchableEntityProvider(type);
            } else {
                entityProvider = new CachingMutableEntityProvider(type);
            }
            // copy settings from parent provider
            entityProvider.setJndiAddresses(jndiProvider.getJndiAddresses());

            container.setEntityProvider(entityProvider);
        } else {
            EntityManager em = referenceEntityProvider.getEntityManager();
            if (buffered) {
                container = JPAContainerFactory.makeBatchable(type, em);
            } else {
                container = JPAContainerFactory.make(type, em);
            }
        }
        configureContainer(referenceContainer, container);
        return container;
    }

    /**
     * This method does additional configurations for the container instantiated
     * for a field. By default it copies the registered
     * {@link LazyLoadingDelegate} and {@link EntityManagerProvider} from the
     * reference container.
     * 
     * @param referenceContainer
     * @param container
     */
    protected void configureContainer(EntityContainer<?> referenceContainer,
            JPAContainer<?> container) {
        // Set the lazy loading delegate to the same as the parent.
        container.getEntityProvider()
                .setLazyLoadingDelegate(
                        referenceContainer.getEntityProvider()
                                .getLazyLoadingDelegate());
        // Set the entity manager provider (if applicable)
        EntityManagerProvider registeredEMP = referenceContainer
                .getEntityProvider().getEntityManagerProvider();
        if (registeredEMP != null) {
            container.getEntityProvider().setEntityManager(null);
            container.getEntityProvider().setEntityManagerProvider(
                    registeredEMP);
        }
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

}

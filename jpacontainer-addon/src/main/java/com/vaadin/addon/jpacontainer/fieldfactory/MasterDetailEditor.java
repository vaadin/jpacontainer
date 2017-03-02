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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.util.HibernateUtil;
import com.vaadin.v7.data.Container.Filter;
import com.vaadin.v7.data.Validator.InvalidValueException;
import com.vaadin.v7.data.util.BeanItem;
import com.vaadin.v7.data.util.filter.Compare;
import com.vaadin.event.Action;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.v7.ui.CustomField;
import com.vaadin.v7.ui.DefaultFieldFactory;
import com.vaadin.v7.ui.Form;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.TableFieldFactory;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class MasterDetailEditor extends CustomField implements Action.Handler {

    private final FieldFactory fieldFactory;
    private Class<?> referencedType;

    final private Action add = new Action(getMasterDetailAddItemCaption());
    final private Action remove = new Action(getMasterDetailRemoveItemCaption());
    final private Action[] actions = new Action[] { add, remove };
    private JPAContainer container;
    private Table table;
    private String backReferencePropertyId;
    private Object masterEntity;
    private final Object propertyId;
    private final EntityContainer<?> containerForProperty;
    private final Object itemId;

    /**
     * @param containerForProperty
     * @param itemId
     * @param propertyId
     * @param uiContext
     */
    public MasterDetailEditor(FieldFactory fieldFactory,
            EntityContainer<?> containerForProperty, Object itemId,
            Object propertyId, Component uiContext) {
        this.fieldFactory = fieldFactory;
        this.containerForProperty = containerForProperty;
        this.itemId = itemId;
        this.propertyId = propertyId;
        masterEntity = containerForProperty.getItem(itemId).getEntity();

        // this.setConverter(new MasterDetailConverter(containerForProperty,
        // itemId, propertyId));

        boolean writeThrough = true;
        if (uiContext instanceof Form) {
            Form f = (Form) uiContext;
            writeThrough = f.isBuffered();
        }
        buildContainer(writeThrough);

        setCaption(DefaultFieldFactory.createCaptionByPropertyId(propertyId));
    }

    private void buildContainer(boolean writeThrough) {
        Class<?> masterEntityClass = containerForProperty.getEntityClass();
        referencedType = fieldFactory.detectReferencedType(
                fieldFactory.getEntityManagerFactory(containerForProperty),
                propertyId, masterEntityClass);
        container = fieldFactory.createJPAContainerFor(containerForProperty,
                referencedType, !writeThrough);
        backReferencePropertyId = HibernateUtil.getMappedByProperty(
                masterEntity, propertyId.toString());
        Filter filter = new Compare.Equal(backReferencePropertyId, masterEntity);
        container.addContainerFilter(filter);
    }

    private void buildTable() {
        table = new Table(null, container);
        Object[] visibleProperties = fieldFactory
                .getVisibleProperties(referencedType);
        if (visibleProperties == null) {
            List<Object> asList = new ArrayList<Object>(
                    Arrays.asList(getTable().getVisibleColumns()));
            asList.remove("id");
            asList.remove(backReferencePropertyId);
            visibleProperties = asList.toArray();
        }
        getTable().setPageLength(5);
        getTable().setVisibleColumns(visibleProperties);
        getTable().addActionHandler(this);

        getTable().setTableFieldFactory(getFieldFactoryForMasterDetailEditor());
        getTable().setEditable(true);
        getTable().setSelectable(true);
    }

    protected Table getTable() {
        return table;
    }

    /**
     * TODO consider opening and adding parameters like propertyId, master class
     * etc
     * 
     * @return
     */
    private TableFieldFactory getFieldFactoryForMasterDetailEditor() {
        return fieldFactory;
    }

    protected String getMasterDetailRemoveItemCaption() {
        return "Remove";
    }

    protected String getMasterDetailAddItemCaption() {
        return "Add";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.addon.jpacontainer.fieldfactory.JPAContainerCustomField#getType
     * ()
     */
    @Override
    public Class<?> getType() {
        return containerForProperty.getItem(itemId).getItemProperty(propertyId)
                .getType();
    }

    public void handleAction(Action action, Object sender, Object target) {
        if (action == add) {
            addNew();
        } else {
            remove(target);
        }
    }

    public Action[] getActions(Object target, Object sender) {
        return actions;
    }

    private void remove(Object itemId) {
        if (itemId != null) {
            Collection<?> collection = (Collection<?>) getPropertyDataSource()
                    .getValue();
            EntityItem item = container.getItem(itemId);
            item.getItemProperty(backReferencePropertyId).setValue(null);
            container.removeItem(itemId);
            if (isBuffered()) {
                collection.remove(item.getEntity());
            }
        }
    }

    private void addNew() {
        try {
            Object newInstance = container.getEntityClass().newInstance();
            BeanItem<?> beanItem = new BeanItem(newInstance);
            beanItem.getItemProperty(backReferencePropertyId).setValue(
                    masterEntity);
            container.addEntity(newInstance);
            if (isBuffered()) {
                // TODO need to update the actual property also!?
            }
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).warning(
                    "Could not instantiate detail instance "
                            + container.getEntityClass().getName());
        }
    }

    @Override
    public void commit() throws SourceException, InvalidValueException {
        if (!isBuffered()) {
            // Update the original collection to contain up to date list of
            // referenced entities
            ((EntityItemProperty) getPropertyDataSource()).getItem().refresh();
            Collection c = (Collection) getPropertyDataSource().getValue();
            boolean isNew = c == null;
            HashSet orphaned = !isNew ? new HashSet(c) : null;
            Collection itemIds = container.getItemIds();
            for (Object object : itemIds) {
                EntityItem item = container.getItem(object);
                Object entity = item.getEntity();
                if (!isNew) {
                    orphaned.remove(entity);
                }
                if (c == null) {
                    try {
                        c = MultiSelectConverter
                                .createNewCollectionForType(containerForProperty
                                        .getItem(itemId)
                                        .getItemProperty(propertyId).getType());
                    } catch (InstantiationException e) {
                        throw new SourceException(container, e);
                    } catch (IllegalAccessException e) {
                        throw new SourceException(container, e);
                    }
                }
                if (isNew || !c.contains(entity)) {
                    c.add(entity);
                }
            }
            if (!isNew) {
                c.removeAll(orphaned);
            }
            getPropertyDataSource().setValue(c);
        } else {
            super.commit();
        }
    }

    @Override
    protected Component initContent() {
        CssLayout vl = new CssLayout();
        buildTable();
        vl.addComponent(getTable());

        CssLayout buttons = new CssLayout();
        buttons.addComponent(new Button(getMasterDetailAddItemCaption(),
                new ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        addNew();
                    }
                }));
        buttons.addComponent(new Button(getMasterDetailRemoveItemCaption(),
                new ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        remove(getTable().getValue());
                    }
                }));
        vl.addComponent(buttons);
        return vl;
    }

}

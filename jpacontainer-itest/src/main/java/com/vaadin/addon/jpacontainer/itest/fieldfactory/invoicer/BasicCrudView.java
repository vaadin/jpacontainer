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
package com.vaadin.addon.jpacontainer.itest.fieldfactory.invoicer;

import java.util.Arrays;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.fieldfactory.FieldFactory;
import com.vaadin.addon.jpacontainer.itest.TestLauncherUI;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.Reindeer;

/**
 * This is a rudimentary general purpose CRUD view to list and edit JPA entities
 * with JPAContainer. Lists all entities in a table and puts the selected row
 * into a buffered form below it.
 */
public class BasicCrudView<T> extends AbsoluteLayout implements
        Property.ValueChangeListener, Handler, ClickListener {

    private JPAContainer<T> container;
    private Table table;
    private Form form;
    private FieldFactory fieldFactory;
    private Class<T> entityClass;
    private Button commit;
    private Button discard;
    private Object[] formPropertyIds;
    private Button addButton;
    private Button deleteButton;
    private Panel panel;

    public BasicCrudView(Class<T> entityClass) {
        this.entityClass = entityClass;
        setSizeFull();
        initContainer();
        initFieldFactory();
        buildView();
    }

    protected void initFieldFactory() {
        fieldFactory = new FieldFactory();
    }

    protected FieldFactory getFieldFactory() {
        return fieldFactory;
    }

    protected Table getTable() {
        return table;
    }

    protected Form getForm() {
        return form;
    }

    protected void setVisibleTableProperties(Object... tablePropertyIds) {
        table.setVisibleColumns(tablePropertyIds);
    }

    protected void setVisibleFormProperties(Object... formPropertyIds) {
        this.formPropertyIds = formPropertyIds;
        form.setVisibleItemProperties(formPropertyIds);
    }

    protected void buildView() {
        setSizeFull();
        panel = new Panel(getCaption());
        panel.setSizeFull();
        addComponent(panel);

        VerticalSplitPanel verticalSplitPanel = new VerticalSplitPanel();
        verticalSplitPanel.setSizeFull();

        table.setSizeFull();
        table.setSelectable(true);
        table.addListener(this);
        table.setImmediate(true);
        table.addActionHandler(this);
        verticalSplitPanel.addComponent(table);

        addButton = new Button("+", this);
        addButton.setDescription("Add new " + getEntityClass().getSimpleName());
        addButton.setStyleName(Reindeer.BUTTON_SMALL);
        addComponent(addButton, "top:0;right:40px;");

        deleteButton = new Button("-", this);
        deleteButton.setDescription("Delete selected "
                + getEntityClass().getSimpleName());
        deleteButton.setStyleName(Reindeer.BUTTON_SMALL);
        deleteButton.setEnabled(false);
        addComponent(deleteButton, "top:0;right:5px;");

        form = new Form();
        form.setVisible(false);
        // form.setWriteThrough(false);
        form.setCaption(getEntityClass().getSimpleName());
        form.setFormFieldFactory(fieldFactory);
        commit = new Button("Save", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                form.commit();
            }
        });
        commit.setStyleName(Reindeer.BUTTON_DEFAULT);

        discard = new Button("Cancel", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                form.discard();
            }
        });
        form.getFooter().addComponent(commit);
        form.getFooter().addComponent(discard);
        // form.getLayout().setMargin(true);
        // form.getFooter().setMargin(false, true, false, true);
        ((HorizontalLayout) form.getFooter()).setSpacing(true);
        verticalSplitPanel.addComponent(form);
        verticalSplitPanel.setSplitPosition(30);

        panel.setContent(verticalSplitPanel);

        // register action handler (enter and ctrl-n)
        panel.addActionHandler(this);
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    protected void initContainer() {
        container = JPAContainerFactory.make(getEntityClass(),
                TestLauncherUI.PERSISTENCE_UNIT);
        table = new Table(null, container);
    }

    protected JPAContainer<T> getContainer() {
        return container;
    }

    @Override
    public void valueChange(ValueChangeEvent event) {
        Object itemId = event.getProperty().getValue();
        Item item = table.getItem(itemId);
        boolean entitySelected = item != null;
        // modify visibility of form and delete button if an item is selected
        form.setVisible(entitySelected);
        deleteButton.setEnabled(entitySelected);
        if (entitySelected) {
            // set entity item to form and focus it
            form.setItemDataSource(item,
                    formPropertyIds != null ? Arrays.asList(formPropertyIds)
                            : item.getItemPropertyIds());
            form.focus();
        }
    }

    @Override
    public String getCaption() {
        return getEntityClass().getSimpleName() + "s";
    }

    private static final ShortcutAction SAVE = new ShortcutAction("Save",
            KeyCode.ENTER, null);
    private static final ShortcutAction SAVE2 = new ShortcutAction("^Save");
    private static final ShortcutAction NEW = new ShortcutAction("^New");
    private static final Action DELETE = new Action("Delete");

    private static final Action[] ACTIONS = new Action[] { NEW, DELETE };
    private static final Action[] SHORTCUT_ACTIONS = new Action[] { SAVE,
            SAVE2, NEW };

    @Override
    public Action[] getActions(Object target, Object sender) {
        if (sender == table) {
            return ACTIONS;
        } else {
            return SHORTCUT_ACTIONS;
        }
    }

    @Override
    public void handleAction(Action action, Object sender, Object target) {
        if (action == NEW) {
            addItem();
        } else if (action == DELETE) {
            deleteItem(target);
        } else if (action == SAVE || action == SAVE2) {
            if (form.isVisible()) {
                form.commit();
            }
        }

    }

    private void deleteItem(Object itemId) {
        container.removeItem(itemId);
    }

    protected void addItem() {
        try {
            T newInstance = newInstance();
            Object itemId = container.addEntity(newInstance);
            table.setValue(itemId);
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * This method creates a new instance of the main entity type.
     * 
     * @return a new instance of the main entity type
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    protected T newInstance() throws InstantiationException,
            IllegalAccessException {
        T newInstance = getEntityClass().newInstance();
        return newInstance;
    }

    /**
     * Method to refresh containers in this view. E.g. if a bidirectional
     * reference is edited from the opposite direction or if we knew that an
     * other user had edited visible values.
     */
    public void refreshContainer() {
        container.refresh();
        if (table.getValue() != null) {
            // reset form as e.g. referenced containers may have changed
            Item item = table.getItem(table.getValue());
            form.setItemDataSource(item,
                    formPropertyIds != null ? Arrays.asList(formPropertyIds)
                            : item.getItemPropertyIds());
            form.focus();
        }
    }

    @Override
    public void buttonClick(ClickEvent event) {
        if (event.getButton() == addButton) {
            addItem();
        } else if (event.getButton() == deleteButton) {
            deleteItem(table.getValue());
        }
    }

    @Override
    public void attach() {
        super.attach();
        panel.focus();
    }
}

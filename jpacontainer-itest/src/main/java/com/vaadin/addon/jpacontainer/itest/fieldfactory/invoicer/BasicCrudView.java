package com.vaadin.addon.jpacontainer.itest.fieldfactory.invoicer;

import java.util.Arrays;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.fieldfactory.FieldFactory;
import com.vaadin.addon.jpacontainer.itest.TestLauncherApplication;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.Reindeer;

/**
 * This is a rudimentary general purpose crud view built for testing
 * JPAContainer. Lists all entities in a table and puts the selected row into a
 * buffered form below it.
 */
public class BasicCrudView<T> extends VerticalSplitPanel implements
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

    public Form getForm() {
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
        table.setSizeFull();
        table.setSelectable(true);
        table.addListener(this);
        table.setImmediate(true);
        table.addActionHandler(this);
        AbsoluteLayout absoluteLayout = new AbsoluteLayout();
        absoluteLayout.setSizeFull();
        absoluteLayout.addComponent(table);
        addComponent(absoluteLayout);
        
        addButton = new Button("+", this);
        addButton.setDescription("Add new " + getEntityClass().getSimpleName());
        addButton.setStyleName(Reindeer.BUTTON_SMALL);
        absoluteLayout.addComponent(addButton, "top:1px; right:40px;");
        
        deleteButton = new Button("-", this);
        deleteButton.setDescription("Delete selected " + getEntityClass().getSimpleName());
        deleteButton.setStyleName(Reindeer.BUTTON_SMALL);
        deleteButton.setEnabled(false);
        absoluteLayout.addComponent(deleteButton, "top:1px; right:5px;");
        
        form = new Form();
        form.setVisible(false);
        form.setWriteThrough(false);
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
        form.getLayout().setMargin(true);
        form.getFooter().setMargin(false, true, false, true);
        ((HorizontalLayout) form.getFooter()).setSpacing(true);
        addComponent(form);
        setSplitPosition(30);
        
    }
    
    public Class<T> getEntityClass() {
        return entityClass;
    }

    protected void initContainer() {
        container = JPAContainerFactory.make(getEntityClass(),
                TestLauncherApplication.PERSISTENCE_UNIT);
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
        form.setVisible(entitySelected);
        deleteButton.setEnabled(entitySelected);
        if(entitySelected) {
            form.setItemDataSource(
                    item,
                    formPropertyIds != null ? Arrays.asList(formPropertyIds) : item
                            .getItemPropertyIds());
            form.focus();
        }
    }

    @Override
    public String getCaption() {
        return getEntityClass().getSimpleName() + "s";
    }

    private static final Action NEW = new Action("New");
    private static final Action DELETE = new Action("Delete");
    private static final Action[] ACTIONS = new Action[] { NEW, DELETE };

    @Override
    public Action[] getActions(Object target, Object sender) {
        return ACTIONS;
    }

    @Override
    public void handleAction(Action action, Object sender, Object target) {
        if (action == NEW) {
            addItem();
        } else if (action == DELETE) {
            deleteItem(target);
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
            form.setItemDataSource(table.getItem(table.getValue()));
        }
    }

    @Override
    public void buttonClick(ClickEvent event) {
        if(event.getButton() == addButton) {
            addItem();
        } else if(event.getButton() == deleteButton) {
            deleteItem(table.getValue());
        }
    }
}

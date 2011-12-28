package com.vaadin.addon.jpacontainer.itest.fieldfactory.invoicer;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.fieldfactory.FieldFactory;
import com.vaadin.addon.jpacontainer.itest.TestLauncherApplication;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Form;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalSplitPanel;

public class BasicCrudView<T> extends VerticalSplitPanel implements
        Property.ValueChangeListener, Handler {

    private JPAContainer<T> container;
    private Table table;
    private Form form;
    private FieldFactory fieldFactory;
    private Class<T> entityClass;
    private Button commit;
    private Button discard;

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
    
    public FieldFactory getFieldFactory() {
        return fieldFactory;
    }
    
    protected Table getTable() {
        return table;
    }
    
    public Form getForm() {
        return form;
    }

    public void buildView() {
        table.setSizeFull();
        table.setSelectable(true);
        table.addListener(this);
        table.setImmediate(true);
        table.addActionHandler(this);
        addComponent(table);
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
    
    public JPAContainer<T> getContainer() {
        return container;
    }

    @Override
    public void valueChange(ValueChangeEvent event) {
        Object itemId = event.getProperty().getValue();
        Item item = table.getItem(itemId);
        form.setItemDataSource(item);
        form.setVisible(item != null);
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
        } else if (action == DELETE) {
            container.removeItem(target);
        }

    }

    protected T newInstance() throws InstantiationException,
            IllegalAccessException {
        T newInstance = getEntityClass().newInstance();
        return newInstance;
    }

    public void refreshContainer() {
        container.refresh();
        if (table.getValue() != null) {
            // reset form as e.g. referenced containers may have changed
            form.setItemDataSource(table.getItem(table.getValue()));
        }
    }
}

package com.vaadin.addon.jpacontainer.fieldfactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.util.HibernateUtil;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.event.Action;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Form;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.VerticalLayout;

public class MasterDetailEditor extends JPAContainerCustomField implements
        Action.Handler {

    private final FieldFactory fieldFactory;
    private Class<?> referencedType;

    final private Action add = new Action(getMasterDetailAddItemCaption());
    final private Action remove = new Action(getMasterDetailRemoveItemCaption());
    final private Action[] actions = new Action[] { add, remove };
    @SuppressWarnings("rawtypes")
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


        boolean writeThrough = true;
        if (uiContext instanceof Form) {
            Form f = (Form) uiContext;
            writeThrough = f.isWriteThrough();
        }
        buildContainer(writeThrough);

        buildLayout();

        setCaption(DefaultFieldFactory.createCaptionByPropertyId(propertyId));
    }

    private void buildContainer(boolean writeThrough) {
        // FIXME buffered mode
        Class<?> masterEntityClass = containerForProperty.getEntityClass();
        referencedType = fieldFactory.detectReferencedType(
                fieldFactory.getEntityManagerFactory(containerForProperty),
                propertyId, masterEntityClass);
        container = fieldFactory.createJPAContainerFor(containerForProperty,
                referencedType, !writeThrough);
        backReferencePropertyId = HibernateUtil.getMappedByProperty(
                masterEntity,
                propertyId.toString());
        Filter filter = new Compare.Equal(backReferencePropertyId, masterEntity);
        container.addContainerFilter(filter);
    }

    private void buildLayout() {
        VerticalLayout vl = new VerticalLayout();
        buildTable();
        vl.addComponent(getTable());

        CssLayout buttons = new CssLayout();
        buttons.addComponent(new Button(getMasterDetailAddItemCaption(),
                new ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        addNew();
                    }
                }));
        // TODO replace with a (-) button in a generated column? Table currently
        // not selectable.
        buttons.addComponent(new Button(getMasterDetailRemoveItemCaption(),
                new ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        remove(getTable().getValue());
                    }
                }));
        vl.addComponent(buttons);

        setCompositionRoot(vl);
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
        return referencedType;
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
            Collection<?> collection = (Collection<?>) getPropertyDataSource().getValue();
            EntityItem item = container.getItem(itemId);
            item.getItemProperty(backReferencePropertyId).setValue(null);
            container.removeItem(itemId);
            if(isWriteThrough()) {
                collection.remove(item.getEntity());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addNew() {
        try {
            Object newInstance = container.getEntityClass().newInstance();
            @SuppressWarnings("rawtypes")
            BeanItem<?> beanItem = new BeanItem(newInstance);
            beanItem.getItemProperty(backReferencePropertyId).setValue(
                    masterEntity);
            container.addEntity(newInstance);
            if(isWriteThrough()) {
                // TODO need to update the actual property also!?
            }
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).warning(
                    "Could not instantiate detail instance "
                            + container.getEntityClass().getName());
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void commit() throws SourceException, InvalidValueException {
        if (!isWriteThrough()) {
//            container.commit();
            // Update the original collection to contain up to date list of
            // referenced entities
            Collection c = (Collection) getPropertyDataSource().getValue();
            boolean isNew = c == null;
            HashSet orphaned = !isNew ? new HashSet(c) : null;
            Collection itemIds = container.getItemIds();
            for (Object object : itemIds) {
                EntityItem item = container.getItem(object);
                Object entity = item.getEntity();
                if(!isNew) {
                    orphaned.remove(entity);
                }
                if(c == null) {
                    try {
                        c = MultiSelectTranslator.createNewCollectionForType(containerForProperty.getItem(itemId).getItemProperty(propertyId).getType());
                    } catch (InstantiationException e) {
                        throw new SourceException(container, e);
                    } catch (IllegalAccessException e) {
                        throw new SourceException(container, e);
                    }
                }
                if(isNew || !c.contains(entity)) {
                    c.add(entity);
                }
            }
            if(isNew) {
                getPropertyDataSource().setValue(c);
            } else {
                c.removeAll(orphaned);
            }
        } else {
            super.commit();
        }
    }

}

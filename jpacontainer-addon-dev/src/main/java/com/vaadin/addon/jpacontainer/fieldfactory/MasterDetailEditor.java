package com.vaadin.addon.jpacontainer.fieldfactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.util.HibernateUtil;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.event.Action;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.VerticalLayout;

public class MasterDetailEditor extends JPAContainerCustomField implements
        Action.Handler {

    private final JPAContainerFieldFactory fieldFactory;
    private Class<?> referencedType;

    final Action add = new Action(getMasterDetailAddItemCaption());
    final Action remove = new Action(getMasterDetailRemoveItemCaption());
    final Action[] actions = new Action[] { add, remove };
    @SuppressWarnings("rawtypes")
    private JPAContainer container;
    private Table table;
    private String backReferencePropertyId;
    private Object masterEntity;
    private final Object propertyId;

    /**
     * @param containerForProperty
     * @param itemId
     * @param propertyId
     * @param uiContext
     */
    public MasterDetailEditor(JPAContainerFieldFactory fieldFactory,
            EntityContainer<?> containerForProperty, Object itemId,
            Object propertyId, Component uiContext) {
        this.fieldFactory = fieldFactory;
        this.propertyId = propertyId;

        buildContainer(containerForProperty, itemId);

        buildLayout();

        setCaption(DefaultFieldFactory.createCaptionByPropertyId(propertyId));
    }

    private void buildContainer(EntityContainer<?> containerForProperty,
            Object itemId) {
        // FIXME buffered mode
        Class<?> masterEntityClass = containerForProperty.getEntityClass();
        referencedType = fieldFactory.detectReferencedType(
                fieldFactory.getEntityManagerFactory(containerForProperty),
                propertyId, masterEntityClass);
        container = fieldFactory.createJPAContainerFor(containerForProperty,
                referencedType, false);
        backReferencePropertyId = HibernateUtil.getMappedByProperty(
                containerForProperty.getItem(itemId).getEntity(),
                propertyId.toString());
        masterEntity = containerForProperty.getEntityProvider().getEntity(
                itemId);
        Filter filter = new Compare.Equal(backReferencePropertyId, masterEntity);
        container.addContainerFilter(filter);
    }

    private void buildLayout() {
        VerticalLayout vl = new VerticalLayout();
        buildTable();
        vl.addComponent(table);

        CssLayout buttons = new CssLayout();
        buttons.addComponent(new Button(getMasterDetailAddItemCaption(),
                new ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        addNew();
                    }
                }));
        // TODO replace with a (-) button in a generated column? Table currently
        // not selectable.
//        buttons.addComponent(new Button(getMasterDetailRemoveItemCaption(),
//                new ClickListener() {
//                    public void buttonClick(ClickEvent event) {
//                        remove(table.getValue());
//                    }
//                }));
        vl.addComponent(buttons);

        setCompositionRoot(vl);
    }

    private void buildTable() {
        table = new Table(null, container);
        Object[] visibleProperties = fieldFactory
                .getVisibleProperties(referencedType);
        if (visibleProperties == null) {
            List<Object> asList = new ArrayList<Object>(Arrays.asList(table
                    .getVisibleColumns()));
            asList.remove("id");
            asList.remove(backReferencePropertyId);
            visibleProperties = asList.toArray();
        }
        table.setVisibleColumns(visibleProperties);

        table.addActionHandler(this);

        table.setTableFieldFactory(getFieldFactoryForMasterDetailEditor());
        table.setEditable(true);
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
        table.removeItem(itemId);
    }

    @SuppressWarnings("unchecked")
    private void addNew() {
        try {
            Object newInstance = container.getEntityClass().newInstance();
            @SuppressWarnings("rawtypes")
            BeanItem<?> beanItem = new BeanItem(newInstance);
            beanItem.getItemProperty(backReferencePropertyId).setValue(
                    masterEntity);
            // TODO need to update the actual property also!?
            container.addEntity(newInstance);
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).warning(
                    "Could not instantiate detail instance "
                            + container.getEntityClass().getName());
        }
    }
}

package com.vaadin.addon.jpacontainer.fieldfactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
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

/**
 * TODO make referenced fields in embeddables properly editable TODO make this
 * work with basic data types (wrap value in a helper class ?).
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ElementCollectionEditor extends JPAContainerCustomField implements
        Action.Handler, EmbeddableEditor {

    private static final Set<Class<?>> BASIC_DATA_TYPES = new HashSet<Class<?>>(
            Arrays.asList(Boolean.class, String.class, Integer.class,
                    Long.class, Float.class, Double.class, Date.class,
                    Number.class));

    private final FieldFactory fieldFactory;
    private Class<?> referencedType;

    final private Action add = new Action(getMasterDetailAddItemCaption());
    final private Action remove = new Action(getMasterDetailRemoveItemCaption());
    final private Action[] actions = new Action[] { add, remove };
    private BeanItemContainer container;
    private Table table;
    private String backReferencePropertyId;
    private final EntityContainer<?> containerForProperty;
    private Strategy strategy;

    /**
     * TODO make it possible to use this editor with Embedded types.
     * 
     * @param containerForProperty
     * @param itemId
     * @param propertyId
     * @param uiContext
     */
    public ElementCollectionEditor(FieldFactory fieldFactory,
            EntityContainer<?> containerForProperty, Object itemId,
            Object propertyId, Component uiContext) {
        this.fieldFactory = fieldFactory;
        this.containerForProperty = containerForProperty;
        containerForProperty.getItem(itemId).getEntity();
        Class<?> masterEntityClass = containerForProperty.getEntityClass();
        referencedType = fieldFactory.detectReferencedType(
                fieldFactory.getEntityManagerFactory(containerForProperty),
                propertyId, masterEntityClass);

        detectStrategy();

        if (uiContext instanceof Form) {
            // copy write buffering eagerly from parent if Form, form sets
            // buffering mode for fields too late
            Form f = (Form) uiContext;
            boolean writeThrough = f.isWriteThrough();
            setWriteThrough(writeThrough);
        }

        buildContainer();

        buildLayout();

        setCaption(DefaultFieldFactory.createCaptionByPropertyId(propertyId));
    }

    private void detectStrategy() {
        boolean isBasicDataType = BASIC_DATA_TYPES.contains(referencedType);
        if (isBasicDataType) {
            strategy = new ImmutableStrategy();
        } else {
            strategy = new MutableStrategy();
        }
    }

    private void buildContainer() {
        container = strategy.buildContainer();
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        super.setPropertyDataSource(newDataSource);
        strategy.populateContainer();
    }

    private void buildLayout() {
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
        strategy.configureTable();
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
            strategy.remove(itemId);
        }
    }

    private void addNew() {
        try {
            strategy.addNew();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).warning(
                    "Could not instantiate detail instance "
                            + container.getBeanType().getName());
        }
    }

    @Override
    public void commit() throws SourceException, InvalidValueException {
        if (!isWriteThrough()) {
            strategy.commit();
        } else {
            super.commit();
        }
    }

    public EntityContainer getMasterEntityContainer() {
        return containerForProperty;
    }

    public Class<?> getEmbeddedClassType() {
        return referencedType;
    }

    public Collection getElements() {
        return (Collection) getPropertyDataSource().getValue();
    }

    private void notifyPropertyOfChangedList() {
        // reset the same collection to item to notify entity item that the
        // property has changed. Needed to make property "dirty".
        getPropertyDataSource().setValue(getElements());
    }

    private interface Strategy {

        BeanItemContainer buildContainer();

        void configureTable();

        void commit();

        void remove(Object itemId);

        void addNew() throws InstantiationException, IllegalAccessException;

        void populateContainer();

    }

    private class MutableStrategy implements Strategy {

        public BeanItemContainer buildContainer() {
            return new BeanItemContainer(referencedType);
        }

        public void populateContainer() {
            Collection<?> elements = getElements();
            container.addAll(elements);
        }

        public void addNew() throws InstantiationException,
                IllegalAccessException {
            Object newInstance = container.getBeanType().newInstance();
            container.addBean(newInstance);
            if (isWriteThrough()) {
                getElements().add(newInstance);
                notifyPropertyOfChangedList();
            }
        }

        public void remove(Object itemId) {
            BeanItem item = container.getItem(itemId);
            container.removeItem(itemId);
            if (isWriteThrough()) {
                Collection collection = getElements();
                collection.remove(item.getBean());
                notifyPropertyOfChangedList();
            }
        }

        public void commit() {
            Collection c = (Collection) getPropertyDataSource().getValue();
            boolean isNew = c == null;
            HashSet orphaned = !isNew ? new HashSet(c) : null;
            Collection itemIds = container.getItemIds();
            for (Object object : itemIds) {
                Object element = object;
                if (!isNew) {
                    orphaned.remove(element);
                }
                if (c == null) {
                    try {
                        c = MultiSelectTranslator
                                .createNewCollectionForType(referencedType);
                    } catch (InstantiationException e) {
                        throw new SourceException(ElementCollectionEditor.this,
                                e);
                    } catch (IllegalAccessException e) {
                        throw new SourceException(ElementCollectionEditor.this,
                                e);
                    }
                }
                if (isNew || !c.contains(element)) {
                    c.add(element);
                }
            }
            if (!isNew) {
                c.removeAll(orphaned);
            }
            notifyPropertyOfChangedList();
        }

        public void configureTable() {
        }

    }

    public class ValueHolder {
        private Object value;

        public ValueHolder(Object o) {
            value = o;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object newValue) {
            if (this.value != newValue) {
                if (newValue == null) {
                    if (isWriteThrough()) {
                        getElements().remove(value);
                        notifyPropertyOfChangedList();
                    }
                } else if (!newValue.equals(value) && isWriteThrough()) {
                    getElements().remove(value);
                    getElements().add(newValue);
                    notifyPropertyOfChangedList();
                }
            }
            this.value = newValue;
        }
    }

    private class ImmutableStrategy implements Strategy {

        public BeanItemContainer buildContainer() {
            return new BeanItemContainer<ValueHolder>(ValueHolder.class);
        }

        public void populateContainer() {
            Collection<?> elements = getElements();
            ArrayList<ValueHolder> wrappedElements = new ArrayList<ValueHolder>();
            for (Object o : elements) {
                wrappedElements.add(new ValueHolder(o));
            }
            container.addAll(wrappedElements);
        }

        public void addNew() throws InstantiationException,
                IllegalAccessException {
            Object newInstance = referencedType.newInstance();
            container.addBean(new ValueHolder(newInstance));
            if (isWriteThrough()) {
                getElements().add(newInstance);
                notifyPropertyOfChangedList();
            }
        }

        public void remove(Object itemId) {
            BeanItem item = container.getItem(itemId);
            container.removeItem(itemId);
            if (isWriteThrough()) {
                Collection collection = getElements();
                collection.remove(((ValueHolder) item.getBean()).getValue());
                notifyPropertyOfChangedList();
            }
        }

        public void commit() {
            Collection c = getElements();
            boolean isNew = c == null;
            HashSet orphaned = !isNew ? new HashSet(c) : null;
            Collection<ValueHolder> itemIds = container.getItemIds();
            for (ValueHolder object : itemIds) {
                Object element = object.getValue();
                if (!isNew) {
                    orphaned.remove(element);
                }
                if (c == null) {
                    try {
                        c = MultiSelectTranslator
                                .createNewCollectionForType(referencedType);
                    } catch (InstantiationException e) {
                        throw new SourceException(ElementCollectionEditor.this,
                                e);
                    } catch (IllegalAccessException e) {
                        throw new SourceException(ElementCollectionEditor.this,
                                e);
                    }
                }
                if (isNew || !c.contains(element)) {
                    c.add(element);
                }
            }
            if (!isNew) {
                c.removeAll(orphaned);
            }
            notifyPropertyOfChangedList();

        }

        public void configureTable() {
            getTable().setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
        }

    }

}

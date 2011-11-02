package com.vaadin.demo.jpaaddressbook;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.vaadin.addon.propertytranslator.PropertyTranslator;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.provider.CachingMutableLocalEntityProvider;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.demo.jpaaddressbook.domain.Department;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.Window;

public class PersonEditor extends Window implements Button.ClickListener,
        FormFieldFactory {

    private final Item personItem;
    private Form editorForm;
    private Button saveButton;
    private Button cancelButton;
    private EntityManager em;
    static EntityManagerFactory emf = Persistence
            .createEntityManagerFactory("addressbook");

    public PersonEditor(Item personItem) {
        em = emf.createEntityManager();
        this.personItem = personItem;
        editorForm = new Form();
        editorForm.setFormFieldFactory(this);
        editorForm.setItemDataSource(personItem, Arrays.asList("firstName",
                "lastName", "phoneNumber", "street", "city", "zipCode",
                "department"));

        saveButton = new Button("Save", this);
        cancelButton = new Button("Cancel", this);

        editorForm.getFooter().addComponent(saveButton);
        editorForm.getFooter().addComponent(cancelButton);
        getContent().setSizeUndefined();
        addComponent(editorForm);
        setCaption(buildCaption());
    }

    /**
     * @return the caption of the editor window
     */
    private String buildCaption() {
        return String.format("%s %s", personItem.getItemProperty("firstName")
                .getValue(), personItem.getItemProperty("lastName").getValue());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.ui.Button.ClickListener#buttonClick(com.vaadin.ui.Button.
     * ClickEvent)
     */
    @Override
    public void buttonClick(ClickEvent event) {
        if (event.getButton() == saveButton) {
            editorForm.commit();
            fireEvent(new EditorSavedEvent(this, personItem));
        } else if (event.getButton() == cancelButton) {
            editorForm.discard();
        }
        close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.ui.FormFieldFactory#createField(com.vaadin.data.Item,
     * java.lang.Object, com.vaadin.ui.Component)
     */
    @Override
    public Field createField(Item item, Object propertyId, Component uiContext) {
        if ("department".equals(propertyId)) {
            final JPAContainer<Department> departments = getDepartmentContainer();

            ComboBox dep = new ComboBox("Department", departments) {
                @Override
                public void setPropertyDataSource(Property ds) {
                    super.setPropertyDataSource(new PropertyTranslator(ds) {
                        @Override
                        public Object translateFromDatasource(Object value) {
                            Department dep = (Department) value;
                            return dep == null ? null : dep.getId();
                        }

                        @Override
                        public Object translateToDatasource(
                                Object formattedValue) throws Exception {
                            EntityItem<Department> item = departments
                                    .getItem(formattedValue);
                            return item.getEntity();
                        }
                    });
                }
            };
            dep.setItemCaptionPropertyId("hierarchicalName");
            return dep;
        }

        return DefaultFieldFactory.get().createField(item, propertyId,
                uiContext);
    }

    private JPAContainer<Department> getDepartmentContainer() {
        final JPAContainer<Department> departments = new JPAContainer<Department>(
                Department.class);
        EntityProvider<Department> entityProvider = new CachingMutableLocalEntityProvider<Department>(
                Department.class, em);
        departments.setEntityProvider(entityProvider);
        departments.setParentProperty("parent");
        return departments;
    }

    public void addListener(EditorSavedListener listener) {
        try {
            Method method = EditorSavedListener.class.getDeclaredMethod(
                    "editorSaved", new Class[] { EditorSavedEvent.class });
            addListener(EditorSavedEvent.class, listener, method);
        } catch (final java.lang.NoSuchMethodException e) {
            // This should never happen
            throw new java.lang.RuntimeException(
                    "Internal error, editor saved method not found");
        }
    }

    public void removeListener(EditorSavedListener listener) {
        removeListener(EditorSavedEvent.class, listener);
    }

    public static class EditorSavedEvent extends Component.Event {
        private Item savedItem;

        public EditorSavedEvent(Component source, Item savedItem) {
            super(source);
            this.savedItem = savedItem;
        }

        public Item getSavedItem() {
            return savedItem;
        }
    }

    public interface EditorSavedListener extends Serializable {
        public void editorSaved(EditorSavedEvent event);
    }

}

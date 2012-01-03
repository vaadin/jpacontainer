package com.vaadin.addon.jpacontainer.fieldfactory;

import java.util.Arrays;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Form;

/**
 * TODO ensure this works with and without write buffering properly.
 * 
 */
@SuppressWarnings("rawtypes")
public class EmbeddedForm extends Form implements EmbeddableEditor {
    private FieldFactory ff;
    private EntityContainer masterEntityContainer;

    public EmbeddedForm(FieldFactory fieldFactory, EntityContainer masterEntityContainer) {
        setFormFieldFactory(fieldFactory);
        this.ff = fieldFactory;
        this.masterEntityContainer = masterEntityContainer;
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public void setPropertyDataSource(Property newDataSource) {
        super.setPropertyDataSource(newDataSource);
        Object embeddedObject = newDataSource.getValue();
        if (embeddedObject == null) {
            embeddedObject = createInstance(newDataSource.getType());
        }
        BeanItem beanItem = new BeanItem(embeddedObject);
        Object[] visibleProperties = ff.getVisibleProperties(newDataSource.getType());
        if (visibleProperties == null) {
            visibleProperties = beanItem.getItemPropertyIds().toArray();
        }
        setItemDataSource(beanItem, Arrays.asList(visibleProperties));
    }

    private Object createInstance(Class<?> type) {
        try {
            Object newInstance = type.newInstance();
            return newInstance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void commit() throws SourceException, InvalidValueException {
        super.commit();
        // notify JPA Property so that things get saved propertly is detached
        // entities are used.
        getPropertyDataSource().setValue(getPropertyDataSource().getValue());
    }

    public EntityContainer getMasterEntityContainer() {
        return masterEntityContainer;
    }

    public Class<?> getEmbeddedClassType() {
        return getPropertyDataSource().getType();
    }

}

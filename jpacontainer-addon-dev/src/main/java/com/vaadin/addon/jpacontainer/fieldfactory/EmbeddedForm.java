package com.vaadin.addon.jpacontainer.fieldfactory;

import java.util.Arrays;

import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;

/**
 * TODO ensure this works with and without write buffering properly.
 * 
 */
public class EmbeddedForm extends Form implements Field {
    private FieldFactory ff;

    public EmbeddedForm(FieldFactory fieldFactory) {
        setFormFieldFactory(fieldFactory);
        this.ff = fieldFactory;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void setPropertyDataSource(Property newDataSource) {
        super.setPropertyDataSource(newDataSource);
        EntityItemProperty ep = (EntityItemProperty) newDataSource;
        Object embeddedObject = newDataSource.getValue();
        if (embeddedObject == null) {
            embeddedObject = createInstance(ep.getItem().getContainer()
                    .getType(ep.getPropertyId()));
        }
        BeanItem beanItem = new BeanItem(embeddedObject);
        Object[] visibleProperties = ff.getVisibleProperties(ep.getItem()
                .getContainer().getType(ep.getPropertyId()));
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

}

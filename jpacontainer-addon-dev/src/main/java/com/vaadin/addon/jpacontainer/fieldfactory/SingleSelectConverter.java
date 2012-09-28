package com.vaadin.addon.jpacontainer.fieldfactory;

import java.util.Locale;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.AbstractSelect;

public class SingleSelectConverter<T> implements Converter<Object, T> {

    private final AbstractSelect select;

    public SingleSelectConverter(AbstractSelect select) {
        this.select = select;
    }

    @SuppressWarnings("unchecked")
    private EntityContainer<T> getContainer() {
        return (EntityContainer<T>) select.getContainerDataSource();
    }

    public T convertToModel(Object value, Locale locale)
            throws com.vaadin.data.util.converter.Converter.ConversionException {
        if (value != select.getNullSelectionItemId()) {
            return getContainer().getEntityProvider().getEntity(getContainer(),
                    value);
        } else {
            return null;
        }
    }

    public Object convertToPresentation(T value, Locale locale)
            throws com.vaadin.data.util.converter.Converter.ConversionException {
        if (value != null) {
            return getContainer().getEntityProvider().getIdentifier(value);
        }
        return select.getNullSelectionItemId();
    }

    public Class<T> getModelType() {
        return getContainer().getEntityClass();
    }

    public Class<Object> getPresentationType() {
        return Object.class;
    }

}

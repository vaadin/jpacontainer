/*
 * Copyright 2011 Oy Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.addon.jpacontainer.util;

import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.PropertyFormatter;

/**
 * PropertyTranslator is bit like the {@link PropertyFormatter}, but works also
 * for other than string fields.
 * <p>
 * Typical use case is where you have a select whose value is an Entity, but the
 * container datasource used in the select uses a different identifier (most
 * commonly entity id in database).
 */
public abstract class PropertyTranslator extends AbstractProperty implements
        Property.ValueChangeListener, Property.ReadOnlyStatusChangeListener,
        Property.Viewer {

    /** Datasource that stores the actual value. */
    Property dataSource;

    /**
     * Construct a new {@code PropertyTranslator} that is not connected to any
     * data source. Call {@link #setPropertyDataSource(Property)} later on to
     * attach it to a property.
     * 
     */
    protected PropertyTranslator() {
    }

    /**
     * Construct a new translator that is connected to given data source. Calls
     * {@link #translateFromDatasource(Object)} which can be a problem if the
     * formatter has not yet been initialized.
     * 
     * @param propertyDataSource
     *            to connect this property to.
     */
    public PropertyTranslator(Property propertyDataSource) {

        setPropertyDataSource(propertyDataSource);
    }

    /**
     * Gets the current data source of the translator, if any.
     * 
     * @return the current data source as a Property, or <code>null</code> if
     *         none defined.
     */
    public Property getPropertyDataSource() {
        return dataSource;
    }

    /**
     * Sets the specified Property as the data source for the translator.
     * 
     * @param newDataSource
     *            the new data source Property.
     */
    public void setPropertyDataSource(Property newDataSource) {

        boolean readOnly = false;
        Object prevValue = null;

        if (dataSource != null) {
            if (dataSource instanceof Property.ValueChangeNotifier) {
                ((Property.ValueChangeNotifier) dataSource)
                        .removeListener(this);
            }
            if (dataSource instanceof Property.ReadOnlyStatusChangeListener) {
                ((Property.ReadOnlyStatusChangeNotifier) dataSource)
                        .removeListener(this);
            }
            readOnly = isReadOnly();
            prevValue = getValue();
        }

        dataSource = newDataSource;

        if (dataSource != null) {
            if (dataSource instanceof Property.ValueChangeNotifier) {
                ((Property.ValueChangeNotifier) dataSource).addListener(this);
            }
            if (dataSource instanceof Property.ReadOnlyStatusChangeListener) {
                ((Property.ReadOnlyStatusChangeNotifier) dataSource)
                        .addListener(this);
            }
        }

        if (isReadOnly() != readOnly) {
            fireReadOnlyStatusChange();
        }
        
        
        Object newVal = getValue();
        if ((prevValue == null && newVal != null)
                || (prevValue != null && !prevValue.equals(newVal))) {
            fireValueChange();
        }
    }

    /* Documented in the interface */
    public Class getType() {
        return String.class;
    }

    /**
     * Get the translated value.
     * 
     * @return the translated value
     */
    public Object getValue() {
        Object value = dataSource == null ? null : dataSource.getValue();
        if (value == null) {
            return null;
        }
        return translateFromDatasource(value);
    }

    /**
     * Get the translated value as string.
     * 
     * @return If the datasource returns null, this is null. Otherwise this is
     *         translateFromDatasource.toString().
     */
    @Override
    public String toString() {
        Object value = dataSource == null ? null : dataSource.getValue();
        if (value == null) {
            return null;
        }
        return translateFromDatasource(value).toString();
    }

    /** Reflects the read-only status of the datasource. */
    public boolean isReadOnly() {
        return dataSource == null ? false : dataSource.isReadOnly();
    }

    /**
     * This method must be implemented to translate the value received from
     * DataSource.
     * 
     * @param value
     *            Value object got from the datasource. This is guaranteed to be
     *            non-null and of the type compatible with getType() of the
     *            datasource.
     * @return
     */
    abstract public Object translateFromDatasource(Object value);

    /**
     * This method is used by setValue() method to translate given value to be
     * suitable for the datasource.
     * 
     * The method is required to assure that
     * translateToDatasource(translateFromDatasource(x)) equals x.
     * 
     * @param translatedValue
     *            this is the value set by user of the formatter (typically
     *            field).
     * @return Non-null value compatible with datasource.
     * @throws Exception
     *             Any type of exception can be thrown to indicate that the
     *             conversion was not succesful.
     */
    abstract public Object translateToDatasource(Object formattedValue)
            throws Exception;

    /**
     * Sets the Property's read-only mode to the specified status.
     * 
     * @param newStatus
     *            the new read-only status of the Property.
     */
    public void setReadOnly(boolean newStatus) {
        if (dataSource != null) {
            dataSource.setReadOnly(newStatus);
        }
    }

    public void setValue(Object newValue) throws ReadOnlyException,
            ConversionException {
        if (dataSource == null) {
            return;
        }
        if (newValue == null) {
            if (dataSource.getValue() != null) {
                dataSource.setValue(null);
                if (!(dataSource instanceof ValueChangeNotifier)) {
                    fireValueChange();
                }
            }
        } else {
            try {
                dataSource.setValue(translateToDatasource(newValue));
                if (!(dataSource instanceof ValueChangeNotifier)) {
                    fireValueChange();
                }
            } catch (Exception e) {
                if (e instanceof ConversionException) {
                    throw (ConversionException) e;
                } else {
                    throw new ConversionException(e);
                }
            }
        }
    }

    /**
     * Listens for changes in the datasource.
     * 
     * This should not be called directly.
     */
    public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
        fireValueChange();
    }

    /**
     * Listens for changes in the datasource.
     * 
     * This should not be called directly.
     */
    public void readOnlyStatusChange(
            com.vaadin.data.Property.ReadOnlyStatusChangeEvent event) {
        fireReadOnlyStatusChange();
    }

}
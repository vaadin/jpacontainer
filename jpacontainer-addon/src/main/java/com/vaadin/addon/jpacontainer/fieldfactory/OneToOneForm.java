/**
 * Copyright 2009-2013 Oy Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vaadin.addon.jpacontainer.fieldfactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.Validator.InvalidValueException;
import com.vaadin.v7.ui.Form;

/**
 * TODO test in buffered mode the situation where the field is initially null.
 */
public class OneToOneForm extends Form {

    private Object createdInstance;
    private String backReferenceId;
    @SuppressWarnings("rawtypes")
    private EntityItem editedEntityItem;
    private Object masterEntity;
    private EntityItemProperty property;

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        property = (EntityItemProperty) newDataSource;
        masterEntity = property.getItem().getEntity();
        // TODO, should use item from generated JPAContainer instead of
        // beanitem??
        if (newDataSource.getValue() == null) {
            try {
                createdInstance = newDataSource.getType().newInstance();
                if (isBuffered()) {
                    tryToSetBackReference();
                }
                editedEntityItem = createItemForInstance(createdInstance);
                setItemDataSource(editedEntityItem, getVisiblePropertyIds());
                if (isBuffered()) {
                    newDataSource.setValue(editedEntityItem.getEntity());
                    createdInstance = null;
                }
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            editedEntityItem = getItemForInstance(newDataSource.getValue());
            setItemDataSource(editedEntityItem, getVisiblePropertyIds());
        }
        // super.setPropertyDataSource(newDataSource);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Collection<?> getVisiblePropertyIds() {
        String[] visibleProperties = getJPAContainerFieldFactory()
                .getVisibleProperties(editedEntityItem.getEntity().getClass());
        Collection<Object> itemPropertyIds;
        if (visibleProperties == null) {
            itemPropertyIds = new LinkedHashSet(
                    editedEntityItem.getItemPropertyIds());
        } else {
            itemPropertyIds = new LinkedHashSet(
                    Arrays.asList(visibleProperties));
        }
        // always remove the backReferenceId to avoid eternal loop
        // TODO should not trust on convention only
        itemPropertyIds.remove(backReferenceId);
        return itemPropertyIds;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private EntityItem getItemForInstance(Object createdInstance) {
        JPAContainer jpaContainer = getContainer(createdInstance);
        EntityItem item = jpaContainer.getItem(jpaContainer.getEntityProvider()
                .getIdentifier(createdInstance));
        return item;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private EntityItem createItemForInstance(Object createdInstance) {
        JPAContainer jpaContainer = getContainer(createdInstance);
        if (!isBuffered()) {
            // don't actually insert the new item, just create an item around
            // it, expect cascades are set in the "owning" entity.
            jpaContainer.setWriteThrough(false);
        }
        Object itemId = jpaContainer.addEntity(createdInstance);
        return jpaContainer.getItem(itemId);
    }

    private JPAContainer<?> getContainer(Object createdInstance) {
        FieldFactory formFieldFactory = getJPAContainerFieldFactory();
        JPAContainer<?> jpaContainer = formFieldFactory.createJPAContainerFor(
                property.getItem().getContainer(), createdInstance.getClass(),
                !isBuffered());
        return jpaContainer;
    }

    private FieldFactory getJPAContainerFieldFactory() {
        FieldFactory formFieldFactory = (FieldFactory) getFormFieldFactory();
        return formFieldFactory;
    }

    private void tryToSetBackReference() {
        try {
            Method method = createdInstance.getClass().getMethod(
                    "set" + masterEntity.getClass().getSimpleName(),
                    masterEntity.getClass());
            method.setAccessible(true);
            method.invoke(createdInstance, masterEntity);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void commit() throws SourceException, InvalidValueException {
        super.commit();
        if (createdInstance != null) {
            property.setValue(createdInstance);
        }
    }

    public void setBackReferenceId(String simpleName) {
        backReferenceId = simpleName;
    }

}

package com.vaadin.addon.jpacontainer.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Form;

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
                if(isWriteThrough()) {
                    tryToSetBackReference();
                }
                editedEntityItem = createItemForInstance(createdInstance);
                setItemDataSource(editedEntityItem, getVisiblePropertyIds());
                if(isWriteThrough()) {
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
        if (!isWriteThrough()) {
            // don't actually insert the new item, just create an item around
            // it, expect cascades are set in the "owning" entity.
            jpaContainer.setWriteThrough(false);
        }
        Object itemId = jpaContainer.addEntity(createdInstance);
        return jpaContainer.getItem(itemId);
    }

    private JPAContainer getContainer(Object createdInstance) {
        JPAContainerFieldFactory formFieldFactory = getJPAContainerFieldFactory();
        JPAContainer jpaContaienr = formFieldFactory.createJPAContainerFor(
                property.getItem().getContainer(), createdInstance.getClass(), !isWriteThrough());
        return jpaContaienr;
    }

    private JPAContainerFieldFactory getJPAContainerFieldFactory() {
        JPAContainerFieldFactory formFieldFactory = (JPAContainerFieldFactory) getFormFieldFactory();
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
        this.backReferenceId = simpleName;
    }

}

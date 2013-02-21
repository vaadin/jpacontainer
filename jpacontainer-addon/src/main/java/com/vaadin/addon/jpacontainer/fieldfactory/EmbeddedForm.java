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

import java.util.Arrays;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Form;

/**
 * TODO ensure this works without write buffering properly. There might be an
 * issue getting things updated if only field in this embedded form are updated.
 */
@SuppressWarnings("rawtypes")
public class EmbeddedForm extends Form implements EmbeddableEditor {
    private FieldFactory ff;
    private EntityContainer masterEntityContainer;

    public EmbeddedForm(FieldFactory fieldFactory,
            EntityContainer masterEntityContainer) {
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
            if (isWriteThrough()) {
                newDataSource.setValue(embeddedObject);
            }
        }
        BeanItem beanItem = new BeanItem(embeddedObject);
        Object[] visibleProperties = ff.getVisibleProperties(newDataSource
                .getType());
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
        if (isModified()) {

            super.commit();
            // notify JPA Property so that things get saved properly if detached
            // entities are used.

            Object bean = ((BeanItem) getItemDataSource()).getBean();

            getPropertyDataSource().setValue(bean);
        }
    }

    public EntityContainer getMasterEntityContainer() {
        return masterEntityContainer;
    }

    public Class<?> getEmbeddedClassType() {
        return getPropertyDataSource().getType();
    }

}

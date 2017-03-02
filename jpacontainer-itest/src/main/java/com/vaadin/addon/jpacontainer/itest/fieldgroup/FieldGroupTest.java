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
package com.vaadin.addon.jpacontainer.itest.fieldgroup;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.itest.TestLauncherUI;
import com.vaadin.addon.jpacontainer.itest.addressbook.DemoDataGenerator;
import com.vaadin.addon.jpacontainer.itest.addressbook.DepartmentSelector;
import com.vaadin.addon.jpacontainer.itest.domain.Department;
import com.vaadin.addon.jpacontainer.itest.domain.Person;
import com.vaadin.v7.data.fieldgroup.DefaultFieldGroupFieldFactory;
import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.data.validator.BeanValidator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

public class FieldGroupTest extends UI {

    static {
        DemoDataGenerator.create();
    }

    @Override
    protected void init(VaadinRequest request) {
        JPAContainer<Person> persons = JPAContainerFactory.make(Person.class,
                TestLauncherUI.PERSISTENCE_UNIT);
        FormLayout formLayout = new FormLayout();

        // Just edit the first item in the JPAContainer
        final FieldGroup fg = new FieldGroup(persons.getItem(persons
                .firstItemId())) {
            /*
             * Override configureField to add a bean validator to each field.
             */
            @Override
            protected void configureField(Field<?> field) {
                super.configureField(field);
                // Add Bean validators if there are annotations
                // Note that this requires a bean validation implementation to
                // be available.
                BeanValidator validator = new BeanValidator(Person.class,
                        getPropertyId(field).toString());
                field.addValidator(validator);
                if (field.getLocale() != null) {
                    validator.setLocale(field.getLocale());
                }
            }
        };

        /*
         * This is an example of a field factory that constructs a complex
         * field.
         */
        fg.setFieldFactory(new DefaultFieldGroupFieldFactory() {
            @Override
            public <T extends Field> T createField(Class<?> type,
                    Class<T> fieldType) {
                if (type.isAssignableFrom(Department.class)) {
                    return (T) new DepartmentSelector();
                }
                return super.createField(type, fieldType);
            }
        });

        formLayout.addComponent(fg.buildAndBind("firstName"));
        formLayout.addComponent(fg.buildAndBind("lastName"));
        formLayout.addComponent(fg.buildAndBind("street"));
        formLayout.addComponent(fg.buildAndBind("city"));
        formLayout.addComponent(fg.buildAndBind("zipCode"));
        formLayout.addComponent(fg.buildAndBind("phoneNumber"));
        formLayout.addComponent(fg.buildAndBind("department"));

        Button saveButton = new Button("Save");
        saveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    fg.commit();
                } catch (FieldGroup.CommitException e) {
                    Notification.show("Couldn't commit values: "
                            + e.getCause().getMessage(),
                            Notification.Type.ERROR_MESSAGE);
                }
            }
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                fg.discard();
            }
        });

        formLayout.addComponent(saveButton);
        formLayout.addComponent(cancelButton);

        setContent(formLayout);
    }
}

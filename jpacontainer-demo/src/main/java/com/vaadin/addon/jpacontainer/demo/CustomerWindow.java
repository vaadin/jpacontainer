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
/*
JPAContainer
Copyright (C) 2009-2011 Oy Vaadin Ltd

This program is available under GNU Affero General Public License (version
3 or later at your option).

See the file licensing.txt distributed with this software for more
information about licensing.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addon.jpacontainer.demo;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.demo.domain.Customer;
import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Modal window for editing customers.
 *
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class CustomerWindow extends Window {

    private EntityItem<Customer> customer;

    public CustomerWindow(EntityItem<Customer> customer) {
        super("Customer Details");
        setModal(true);
        customer.setWriteThrough(false); // We want an Apply feature
        this.customer = customer;
        initWindow();
    }

    protected static class CustomerFieldFactory extends DefaultFieldFactory {

        public static final String COMMON_FIELD_WIDTH = "12em";

        @Override
        public Field createField(Item item, Object propertyId,
                Component uiContext) {
            Field f = super.createField(item, propertyId, uiContext);
            EntityItem<Customer> i = (EntityItem<Customer>) item;
            if ("custNo".equals(propertyId)) {
                TextField tf = (TextField) f;
                tf.setNullRepresentation("");
                tf.setReadOnly(i.isPersistent()); // In a real application, this value should probably be auto-generated
                tf.setWidth("5em");
                tf.setRequired(true);
                tf.setRequiredError("Please enter a customer no");
                tf.addValidator(new IntegerValidator(
                        "The customer no has to be an integer"));
				tf.setImmediate(true);
            } else if ("customerName".equals(propertyId)) {
                TextField tf = (TextField) f;
                tf.setRequired(true);
                tf.setRequiredError("Please enter a customer name");
                tf.setWidth(COMMON_FIELD_WIDTH);
                tf.addValidator(new StringLengthValidator(
                        "Company name must be at least 2 characters", 2, 255,
                        false));
				tf.setImmediate(true);
            } else if ("notes".equals(propertyId)) {
                TextField tf = (TextField) f;
                tf.setWidth(COMMON_FIELD_WIDTH);
            } else if ("lastInvoiceDate".equals(propertyId) || "lastOrderDate".
                    equals(propertyId)) {
                f.setReadOnly(true);
            } else if (propertyId.toString().endsWith("streetOrBox")) {
                f.setCaption("Street or Box");
                f.setWidth(COMMON_FIELD_WIDTH);
            } else if (propertyId.toString().endsWith("postalCode")) {
                f.setCaption("Postal Code");
                f.addValidator(new StringLengthValidator(
                        "Postal code must be 5 characters", 5,
                        5, true));
                f.setWidth("4em");
				((TextField) f).setImmediate(true);
            } else if (propertyId.toString().endsWith("postOffice")) {
                f.setCaption("Post Office");
                f.setWidth(COMMON_FIELD_WIDTH);
            } else if (propertyId.toString().endsWith("country")) {
                f.setCaption("Country");
                f.setWidth(COMMON_FIELD_WIDTH);
            }

            return f;
        }
    }

    protected void initWindow() {
        VerticalLayout layout = (VerticalLayout) getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
        setClosable(false);

        // Create the forms
        CustomerFieldFactory fieldFactory = new CustomerFieldFactory();
        final Form generalForm = new Form();
        {
            generalForm.setCaption("General information");
            generalForm.setWriteThrough(true); // We use the buffering of the EntityItem instead
            generalForm.setFormFieldFactory(fieldFactory);
            generalForm.setItemDataSource(customer);
            generalForm.setVisibleItemProperties(new String[]{"custNo",
                        "customerName",
                        "notes",
                        "lastInvoiceDate",
                        "lastOrderDate"});
            generalForm.setValidationVisible(true);
            addComponent(generalForm);
        }
        final Form billingForm = new Form();
        {
            billingForm.setCaption("Billing Address");
            billingForm.setWriteThrough(true); // We use the buffering of the EntityItem instead
            billingForm.setFormFieldFactory(fieldFactory);
            billingForm.setItemDataSource(customer);
            billingForm.setVisibleItemProperties(new String[]{"billingAddress.streetOrBox",
                        "billingAddress.postalCode",
                        "billingAddress.postOffice",
                        "billingAddress.country"});
            billingForm.setValidationVisible(true);
            addComponent(billingForm);
        }
        final Form shippingForm = new Form();
        {
            shippingForm.setCaption("Shipping Address");
            shippingForm.setWriteThrough(true); // We use the buffering of the EntityItem instead
            shippingForm.setFormFieldFactory(fieldFactory);
            shippingForm.setItemDataSource(customer);
            shippingForm.setVisibleItemProperties(new String[]{"shippingAddress.streetOrBox",
                        "shippingAddress.postalCode",
                        "shippingAddress.postOffice",
                        "shippingAddress.country"});
            shippingForm.setValidationVisible(true);
            addComponent(shippingForm);
        }

        HorizontalLayout buttons = new HorizontalLayout();
        {
            buttons.setSpacing(true);
            Button applyBtn = new Button("Apply and Close", new Button.ClickListener() {

                public void buttonClick(ClickEvent event) {
                    try {
                        generalForm.validate();
                        billingForm.validate();
                        shippingForm.validate();
                        try {
                            customer.commit();

                            if (customer.getItemId() == null) {
                                // We are adding a new customer
                                customer.getContainer().addEntity(customer.
                                        getEntity());
                            }
                            ((Window) getParent()).removeWindow(
                                    CustomerWindow.this);
                        } catch (Exception e) {
                            showNotification("Could not apply changes",
                                    e.getMessage(),
                                    Notification.TYPE_ERROR_MESSAGE);
                        }
                    } catch (InvalidValueException e) {
                        // Ignore it, let the form handle it
                    }
                }
            });
            buttons.addComponent(applyBtn);

            Button discardBtn = new Button("Discard and Close", new Button.ClickListener() {

                public void buttonClick(ClickEvent event) {
                    customer.discard();
                    ((Window) getParent()).removeWindow(CustomerWindow.this);
                }
            });
            buttons.addComponent(discardBtn);
            layout.addComponent(buttons);
            layout.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT);
        }
        setSizeUndefined();
        setWidth("30em");
    }
}

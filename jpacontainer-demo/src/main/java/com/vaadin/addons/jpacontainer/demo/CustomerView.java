/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.demo;

import com.vaadin.addons.jpacontainer.EntityItem;
import com.vaadin.addons.jpacontainer.EntityProvider;
import com.vaadin.addons.jpacontainer.JPAContainer;
import com.vaadin.addons.jpacontainer.demo.domain.Customer;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * View for browsing and editing customers.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class CustomerView extends CustomComponent {

    public CustomerView(EntityProvider<Customer> entityProvider) {
        this.entityProvider = entityProvider;
        init();
    }
    private EntityProvider<Customer> entityProvider;
    private Button newCustomer = new Button("New Customer");
    private Button openCustomer = new Button("Open Customer");
    private Button deleteCustomer = new Button("Delete Customer");
    private Button search = new Button("Search");
    private JPAContainer<Customer> customerContainer = new JPAContainer(
            Customer.class);
    private Table customerTable = new Table();

    private void init() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(true);

        HorizontalLayout toolbar = new HorizontalLayout();
        {
            newCustomer.addListener(new Button.ClickListener() {

                public void buttonClick(ClickEvent event) {
                    getWindow().addWindow(new CustomerWindow(customerContainer.
                            createEntityItem(new Customer())));
                }
            });

            openCustomer.setEnabled(false);
            openCustomer.addListener(new Button.ClickListener() {

                public void buttonClick(ClickEvent event) {
                    Object itemId = customerTable.getValue();
                    if (itemId != null) {
                        EntityItem<Customer> customerItem = customerContainer.
                                getItem(itemId);
                        getWindow().addWindow(new CustomerWindow(customerItem));
                    }
                }
            });

            deleteCustomer.setEnabled(false);
            deleteCustomer.addListener(new Button.ClickListener() {

                public void buttonClick(ClickEvent event) {
                    Object itemId = customerTable.getValue();
                    if (itemId != null) {
                        try {
                            customerContainer.removeItem(itemId);
                            customerTable.setValue(null);
                        } catch (Exception e) {
                            getWindow().showNotification(
                                    "Could not delete Customer", e.getMessage(),
                                    Notification.TYPE_ERROR_MESSAGE);
                        }
                    }
                }
            });

            toolbar.addComponent(newCustomer);
            toolbar.addComponent(openCustomer);
            toolbar.addComponent(deleteCustomer);
            toolbar.addComponent(search);
            toolbar.setSpacing(true);
            toolbar.setMargin(false, false, true, false);
        }
        layout.addComponent(toolbar);

        {
            customerContainer.setEntityProvider(entityProvider);
            // Remove unused properties
            customerContainer.removeContainerProperty("billingAddress");
            customerContainer.removeContainerProperty("shippingAddress");
            customerContainer.removeContainerProperty("id");
            customerContainer.removeContainerProperty("version");
            // Add some nested properties
            customerContainer.addNestedContainerProperty("billingAddress.*");
            customerContainer.addNestedContainerProperty("shippingAddress.*");

            customerTable.setSizeFull();
            customerTable.setContainerDataSource(customerContainer);
            customerTable.setVisibleColumns(
                    new String[]{"custNo",
                        "customerName",
                        "billingAddress.streetOrBox",
                        "billingAddress.postalCode",
                        "billingAddress.postOffice",
                        "billingAddress.country",
                        "shippingAddress.streetOrBox",
                        "shippingAddress.postalCode",
                        "shippingAddress.postOffice",
                        "shippingAddress.country",
                        "lastInvoiceDate",
                        "lastOrderDate",
                        "notes"});
            customerTable.setColumnHeaders(
                    new String[]{"Cust No",
                        "Name",
                        "BillTo Address",
                        "BillTo Postal Code",
                        "BillTo Post Office",
                        "BillTo Country",
                        "ShipTo Address",
                        "ShipTo Postal Code",
                        "ShipTo Post Office",
                        "ShipTo Country",
                        "Last Invoice Date",
                        "Last Order Date",
                        "Notes"});
            customerTable.setColumnCollapsingAllowed(true);
            customerTable.setSelectable(true);
            customerTable.setImmediate(true);
            try {
                customerTable.setColumnCollapsed("shippingAddress.streetOrBox",
                        true);
                customerTable.setColumnCollapsed("shippingAddress.postalCode",
                        true);
                customerTable.setColumnCollapsed("shippingAddress.postOffice",
                        true);
                customerTable.setColumnCollapsed("shippingAddress.country",
                        true);
                customerTable.setColumnCollapsed("notes",
                        true);
            } catch (IllegalAccessException e) {
                // Ignore it
            }
            customerTable.setSortContainerPropertyId("custNo");
            customerTable.addListener(new Property.ValueChangeListener() {

                public void valueChange(ValueChangeEvent event) {
                    Object id = customerTable.getValue();
                    openCustomer.setEnabled(id != null);
                    deleteCustomer.setEnabled(id != null);
                }
            });
        }
        layout.addComponent(customerTable);
        layout.setExpandRatio(customerTable, 1);

        setCompositionRoot(layout);
        setSizeFull();
    }
}

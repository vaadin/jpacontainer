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
import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.demo.domain.Customer;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * View for browsing and editing customers.
 *
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
@Component(value = "customerView")
@Scope(value = "session")
public class CustomerView extends CustomComponent {

    @Resource(name = "customerProvider")
    private EntityProvider<Customer> entityProvider;
    private Button newCustomer = new Button("New Customer");
    private Button openCustomer = new Button("Open Customer");
    private Button deleteCustomer = new Button("Delete Customer");
    private Button showOrders = new Button("Show Orders");
    private Button showInvoices = new Button("Show Invoices");
    private JPAContainer<Customer> customerContainer = new JPAContainer(
            Customer.class);
    private CheckBox autoCommit = new CheckBox("Auto-commit");
    private Button commit = new Button("Commit");
    private Button discard = new Button("Discard");
    private Table customerTable = new Table();
    @Autowired
    private InvoiceView invoiceView;
    @Autowired
    private OrderView orderView;

    @PostConstruct
    public void init() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(true);

		layout.addComponent(new Label("When <strong>Auto-Commit</strong> is off (default), all the changes that you make are buffered inside the application. Thus, you have to click the <strong>Commit</strong> button to commit the changes to the database. If auto-commit is on, the changes are committed to the database immediately.", Label.CONTENT_XHTML));

        HorizontalLayout toolbar = new HorizontalLayout();
        {
            newCustomer.addListener(new Button.ClickListener() {

                public void buttonClick(ClickEvent event) {
                    getWindow().addWindow(new CustomerWindow(customerContainer.createEntityItem(new Customer())));
                }
            });

            openCustomer.setEnabled(false);
            openCustomer.addListener(new Button.ClickListener() {

                public void buttonClick(ClickEvent event) {
                    Object itemId = customerTable.getValue();
                    if (itemId != null) {
                        EntityItem<Customer> customerItem = customerContainer.getItem(itemId);
                        if (customerItem == null) {
                            getWindow().showNotification(
                                    "Customer deleted by another user");
                        } else {
                            getWindow().addWindow(new CustomerWindow(
                                    customerItem));
                        }
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

            autoCommit.setImmediate(true);
            autoCommit.addListener(new CheckBox.ValueChangeListener() {

                public void valueChange(ValueChangeEvent event) {
                    if (customerContainer.isAutoCommit() != autoCommit.booleanValue()) {
                        try {
                            customerContainer.setAutoCommit(
                                    autoCommit.booleanValue());
                            discard.setEnabled(!customerContainer.isAutoCommit());
                            commit.setEnabled(discard.isEnabled());
                        } catch (Exception e) {
                            autoCommit.setValue(customerContainer.isAutoCommit());
                            getWindow().showNotification(
                                    "Could not toggle auto-commit",
                                    e.getMessage(),
                                    Notification.TYPE_WARNING_MESSAGE);
                        }
                    }
                }
            });

            showOrders.setEnabled(false);
            showOrders.addListener(new Button.ClickListener() {

                public void buttonClick(ClickEvent event) {
                    Object itemId = customerTable.getValue();
                    if (itemId != null) {
                        orderView.showOrdersForCustomer(itemId);
                    }
                }
            });

            showInvoices.setEnabled(false);
            showInvoices.addListener(new Button.ClickListener() {

                public void buttonClick(ClickEvent event) {
                    Object itemId = customerTable.getValue();
                    if (itemId != null) {
                        invoiceView.showInvoicesForCustomer(itemId);
                    }
                }
            });

            commit.addListener(new Button.ClickListener() {

                public void buttonClick(ClickEvent event) {
                    try {
                        customerContainer.commit();
                        getWindow().showNotification("Changes committed");
                    } catch (Exception e) {
                        getWindow().showNotification("Could not commit", e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
                    }
                }
            });

            discard.addListener(new Button.ClickListener() {

                public void buttonClick(ClickEvent event) {
                    try {
                        customerContainer.discard();
                        getWindow().showNotification("Changes discarded");
                    } catch (Exception e) {
                        getWindow().showNotification("Could not discard", e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
                    }
                }
            });

            toolbar.addComponent(newCustomer);
            toolbar.addComponent(openCustomer);
            toolbar.addComponent(deleteCustomer);
            toolbar.addComponent(showOrders);
            toolbar.addComponent(showInvoices);
            toolbar.addComponent(autoCommit);
            toolbar.addComponent(commit);
            toolbar.addComponent(discard);
            toolbar.setSpacing(true);
            toolbar.setMargin(false, false, true, false);

        }
        layout.addComponent(toolbar);

        {
            customerContainer.setEntityProvider(entityProvider);
            autoCommit.setValue(customerContainer.isAutoCommit());
            customerContainer.setContainsIdFiresItemSetChangeIfNotFound(true);

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
            } catch (IllegalStateException e) {
                // Ignore it
            }
            customerTable.setSortContainerPropertyId("custNo");
            customerTable.addListener(new Property.ValueChangeListener() {

                public void valueChange(ValueChangeEvent event) {
                    Object id = customerTable.getValue();
                    EntityItem<Customer> item = id == null ? null : customerContainer.getItem(id);
                    boolean enabled = item != null && !item.isDeleted();
                    openCustomer.setEnabled(enabled);
                    /*
                     * We have a constraint that prevents customers from being deleted
                     * if they have orders or invoices.
                     */
                    deleteCustomer.setEnabled(enabled && item.getEntity().
                            getLastInvoiceDate() == null && item.getEntity().
                            getLastOrderDate() == null);
                    showOrders.setEnabled(enabled && item.isPersistent());
                    showInvoices.setEnabled(enabled && item.isPersistent());
                }
            });
            customerTable.setCellStyleGenerator(new Table.CellStyleGenerator() {

                public String getStyle(Object itemId, Object propertyId) {
                    if (propertyId != null) {
                        return null; // We only style rows
                    }
                    EntityItem<Customer> item = customerContainer.getItem(itemId);
                    if (item == null) {
                        return null;
                    }
                    if (!item.isPersistent()) {
                        return "added";
                    } else if (item.isPersistent() && item.isDirty()) {
                        return "modified";
                    } else if (item.isDeleted()) {
                        return "deleted";
                    } else {
                        return null;
                    }
                }
            });
            commit.setEnabled(!customerContainer.isAutoCommit());
            discard.setEnabled(!customerContainer.isAutoCommit());
        }
        layout.addComponent(customerTable);
        layout.setExpandRatio(customerTable, 1);

        setCompositionRoot(layout);
        setSizeFull();
    }
}

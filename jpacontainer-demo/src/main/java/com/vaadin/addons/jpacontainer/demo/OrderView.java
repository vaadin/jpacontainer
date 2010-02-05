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

import com.vaadin.addons.jpacontainer.EntityProvider;
import com.vaadin.addons.jpacontainer.JPAContainer;
import com.vaadin.addons.jpacontainer.demo.domain.Customer;
import com.vaadin.addons.jpacontainer.demo.domain.Order;
import com.vaadin.addons.jpacontainer.filter.Filters;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import java.util.Date;

/**
 * View for browsing and editing orders.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class OrderView extends CustomComponent {

    public OrderView(EntityProvider<Order> entityProvider, EntityProvider<Customer> customerProvider) {
        this.entityProvider = entityProvider;
        this.customerProvider = customerProvider;
        init();
    }
    private EntityProvider<Order> entityProvider;
    private EntityProvider<Customer> customerProvider;
    private JPAContainer<Order> orderContainer = new JPAContainer(Order.class);
    private JPAContainer<Customer> customerContainer = new JPAContainer(Customer.class);
    private ComboBox filterCustomer = new ComboBox("Customer:");
    private DateField filterFrom = new DateField("From:");
    private DateField filterTo = new DateField("To:");
    private Button filterBtn = new Button("Filter");
    private Button resetBtn = new Button("Reset");

    private void init() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(true);

        HorizontalLayout toolbar = new HorizontalLayout();
        {
            customerContainer.setEntityProvider(customerProvider);
            customerContainer.setApplyFiltersImmediately(true);
            customerContainer.sort(new Object[] {"customerName"}, new boolean[] {true});
            customerContainer.setReadOnly(true);

/*            filterCustomer.setNullSelectionAllowed(true);
            filterCustomer.setFilteringMode(ComboBox.FILTERINGMODE_STARTSWITH);
            filterCustomer.setContainerDataSource(customerContainer);
            filterCustomer.setImmediate(true);
            filterCustomer.setItemCaptionPropertyId("customerName");
            filterCustomer.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);

            toolbar.addComponent(filterCustomer);*/

            filterFrom.setResolution(DateField.RESOLUTION_DAY);
            filterFrom.setDateFormat("yyyy-MM-dd");
            filterTo.setResolution(DateField.RESOLUTION_DAY);
            filterTo.setDateFormat("yyyy-MM-dd");

            //toolbar.addComponent(new Label("Filter by Order Date:"));
            toolbar.addComponent(filterFrom);
            //toolbar.addComponent(new Label("-"));
            toolbar.addComponent(filterTo);
            resetBtn.setEnabled(false);
            toolbar.addComponent(filterBtn);
            toolbar.addComponent(resetBtn);
            toolbar.setSpacing(true);
            toolbar.setMargin(false, false, true, false);
            toolbar.setComponentAlignment(filterBtn, Alignment.BOTTOM_LEFT);
            toolbar.setComponentAlignment(resetBtn, Alignment.BOTTOM_LEFT);

            filterBtn.addListener(new Button.ClickListener() {

                public void buttonClick(Button.ClickEvent event) {
                    Date from = (Date) filterFrom.getValue();
                    Date to = (Date) filterTo.getValue();

                    if (from == null && to == null) {
                        getWindow().showNotification("Nothing to do");
                        return;
                    }

                    if (from != null && to != null) {
                        if (to.before(from)) {
                            getWindow().showNotification(
                                    "Please check the dates!",
                                    Notification.TYPE_WARNING_MESSAGE);
                            return;
                        }
                        orderContainer.removeAllFilters();
                        orderContainer.addFilter(Filters.between("orderDate", from,
                                to, true, true));
                    } else if (from != null) {
                        orderContainer.removeAllFilters();
                        orderContainer.addFilter(Filters.gteq("orderDate", from));
                    } else if (to != null) {
                        orderContainer.removeAllFilters();
                        orderContainer.addFilter(Filters.lteq("orderDate", to));
                    }
                    orderContainer.applyFilters();
                    resetBtn.setEnabled(true);
                    getWindow().showNotification("Filter applied");
                }
            });

            resetBtn.addListener(new Button.ClickListener() {

                public void buttonClick(ClickEvent event) {
                    filterTo.setValue(null);
                    filterFrom.setValue(null);
                    orderContainer.removeAllFilters();
                    orderContainer.applyFilters();
                    resetBtn.setEnabled(false);
                    getWindow().showNotification("Filter reset");
                }
            });

        }
        layout.addComponent(toolbar);

        Table orderTable = new Table();
        {
            orderContainer.setEntityProvider(entityProvider);
            orderContainer.setApplyFiltersImmediately(false);
            // Remove unused properties
            orderContainer.removeContainerProperty("id");
            orderContainer.removeContainerProperty("version");
            orderContainer.removeContainerProperty("items");

            // Add some nested properties
            orderContainer.addNestedContainerProperty("customer.customerName");
            orderContainer.addNestedContainerProperty("customer.custNo");
            orderContainer.addNestedContainerProperty("billingAddress.*");
            orderContainer.addNestedContainerProperty("shippingAddress.*");

            orderTable.setSizeFull();
            orderTable.setContainerDataSource(orderContainer);
            orderTable.setVisibleColumns(
                    new String[]{"orderNo",
                        "orderDate",
                        "customer.custNo",
                        "customer.customerName",
                        "customerReference",
                        "salesReference",
                        "billingAddress.streetOrBox",
                        "billingAddress.postalCode",
                        "billingAddress.postOffice",
                        "billingAddress.country",
                        "billedDate",
                        "shippingAddress.streetOrBox",
                        "shippingAddress.postalCode",
                        "shippingAddress.postOffice",
                        "shippingAddress.country",
                        "shippedDate",
                        "total"
                    });
            orderTable.setColumnHeaders(
                    new String[]{"Order No",
                        "Order Date",
                        "Cust No",
                        "Customer",
                        "Customer Ref",
                        "Sales Ref",
                        "BillTo Address",
                        "BillTo Postal Code",
                        "BillTo Post Office",
                        "BillTo Country",
                        "Billed Date",
                        "ShipTo Address",
                        "ShipTo Postal Code",
                        "ShipTo Post Office",
                        "ShipTo Country",
                        "Shipped Date",
                        "Total Amount"
                    });
            orderTable.setColumnAlignment("total", Table.ALIGN_RIGHT);
            orderTable.setColumnCollapsingAllowed(true);
            orderTable.setSelectable(true);
            orderTable.setImmediate(true);
            try {
                orderTable.setColumnCollapsed("customerReference", true);
                orderTable.setColumnCollapsed("shippingAddress.streetOrBox",
                        true);
                orderTable.setColumnCollapsed("shippingAddress.postalCode",
                        true);
                orderTable.setColumnCollapsed("shippingAddress.postOffice",
                        true);
                orderTable.setColumnCollapsed("shippingAddress.country",
                        true);
                orderTable.setColumnCollapsed("billingAddress.streetOrBox",
                        true);
                orderTable.setColumnCollapsed("billingAddress.postalCode",
                        true);
                orderTable.setColumnCollapsed("billingAddress.postOffice",
                        true);
                orderTable.setColumnCollapsed("billingAddress.country",
                        true);
            } catch (IllegalAccessException e) {
                // Ignore it
            }
            orderTable.setSortContainerPropertyId("orderNo");
        }
        layout.addComponent(orderTable);
        layout.setExpandRatio(orderTable, 1);

        setCompositionRoot(layout);
        setSizeFull();
    }
}

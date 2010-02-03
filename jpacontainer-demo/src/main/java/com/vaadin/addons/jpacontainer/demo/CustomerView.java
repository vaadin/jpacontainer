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
import com.vaadin.addons.jpacontainer.provider.LocalEntityProvider;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * View for browsing and editing customers
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
    private Button editCustomer = new Button("Edit Customer");
    private Button deleteCustomer = new Button("Delete Customer");
    private Button search = new Button("Search");
    private JPAContainer<Customer> customerContainer = new JPAContainer(Customer.class);

    private void init() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(true);

        HorizontalLayout toolbar = new HorizontalLayout();
        {
            toolbar.addComponent(newCustomer);
            toolbar.addComponent(editCustomer);
            toolbar.addComponent(deleteCustomer);
            toolbar.addComponent(search);
        }
        layout.addComponent(toolbar);

        Table customerTable = new Table();
        {
            customerContainer.setEntityProvider(entityProvider);
            customerTable.setSizeFull();
            customerTable.setContainerDataSource(customerContainer);
            customerTable.setVisibleColumns(new String[] {"custNo","customerName","billingAddress","shippingAddress"});
            customerTable.setColumnHeaders(new String[] {"CustNo", "Name", "Billing Address", "Shipping Address"});
            customerTable.setSelectable(true);
            customerTable.setImmediate(true);
        }
        layout.addComponent(customerTable);
        layout.setExpandRatio(customerTable, 1);

        setCompositionRoot(layout);
        setSizeFull();

    }
    
}

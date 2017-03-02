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
package com.vaadin.addon.jpacontainer.itest.fieldfactory.invoicer;

import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.BillingAddress2;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Customer;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.CustomerGroup;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Invoice;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Invoice2;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Invoice2.TestEnumTags;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.InvoiceRow;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.InvoiceRow2;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Product;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.v7.ui.OptionGroup;
import com.vaadin.v7.ui.Tree;
import com.vaadin.ui.Window;

public class InvoicerTestApp extends Window implements
        Property.ValueChangeListener {

    private Tree navTree = new Tree("Views");
    private HorizontalSplitPanel horizontalSplitPanel = new HorizontalSplitPanel();

    public InvoicerTestApp() {
        navTree.addListener(this);

        horizontalSplitPanel.setSplitPosition(200, UNITS_PIXELS);
        horizontalSplitPanel.addComponent(navTree);
        setContent(horizontalSplitPanel);

        addView(new WelcomeView());

        addView(new BasicCrudView<Product>(Product.class));

        BasicCrudView<Customer> customerCrudView = new BasicCrudView<Customer>(
                Customer.class);
        customerCrudView.setVisibleTableProperties("name");
        addView(customerCrudView);

        BasicCrudView<CustomerGroup> groupCrudView = new BasicCrudView<CustomerGroup>(
                CustomerGroup.class);
        groupCrudView.setVisibleTableProperties("name");
        addView(groupCrudView);

        BasicCrudView<Invoice> invoiceCrudView = new BasicCrudView<Invoice>(
                Invoice.class);
        invoiceCrudView.setVisibleTableProperties("date", "customer", "state");
        invoiceCrudView.setVisibleFormProperties("customer", "state", "date",
                "billingAddress", "rows");
        // configure the order of properties in invoicerow master-detail editor
        invoiceCrudView.getFieldFactory().setVisibleProperties(
                InvoiceRow.class, "product", "description", "amount", "unit",
                "unitPrice");
        addView(invoiceCrudView);

        BasicCrudView<Invoice2> invoice2CrudView = new BasicCrudView<Invoice2>(
                Invoice2.class) {
            @Override
            public String getCaption() {
                return "Invoice2 (@Embedded and @ElementCollection)";
            }
        };
        invoice2CrudView.setVisibleTableProperties("date", "customer");
        invoice2CrudView.setVisibleFormProperties("customer", "uppercaseText",
                "date", "billingAddress", "rows", "tags", "enumTags");
        // configure the order of properties in invoicerow master-detail editor
        invoice2CrudView.getFieldFactory().setVisibleProperties(
                InvoiceRow2.class, "product", "description", "amount", "unit",
                "unitPrice");
        invoice2CrudView.getFieldFactory().setVisibleProperties(
                BillingAddress2.class, "street", "postalCode", "city");
        invoice2CrudView.getFieldFactory().setMultiSelectType(TestEnumTags.class, OptionGroup.class);
        addView(invoice2CrudView);

        navTree.setSelectable(true);
        navTree.setNullSelectionAllowed(false);
        navTree.setImmediate(true);
        navTree.setValue(navTree.getItemIds().iterator().next());

    }

    private void addView(Component view) {
        navTree.addItem(view);
        if (view.getCaption() != null) {
            navTree.setItemCaption(view, view.getCaption());
        }
        navTree.setChildrenAllowed(view, false);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void valueChange(ValueChangeEvent event) {
        Component value = (Component) event.getProperty().getValue();
        if (value instanceof BasicCrudView) {
            BasicCrudView cv = (BasicCrudView) value;
            cv.refreshContainer();
        }
        horizontalSplitPanel.setSecondComponent(value);
    }

}

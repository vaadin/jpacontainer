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
package com.vaadin.addon.jpacontainer.itest.fieldfactory;

import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.fieldfactory.FieldFactory;
import com.vaadin.addon.jpacontainer.itest.TestLauncherUI;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.BillingAddress;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Customer;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.CustomerGroup;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Invoice;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.InvoiceRow;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Product;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.event.Action;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Table;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class FieldFactoryTest extends UI {

    static {
        createSomeTestData();
    }

    private VerticalLayout contentLayout;

    private static void createSomeTestData() {
        EntityManager em = Persistence.createEntityManagerFactory(
                TestLauncherUI.PERSISTENCE_UNIT).createEntityManager();

        em.getTransaction().begin();
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setName("Group1");
        em.persist(customerGroup);
        customerGroup = new CustomerGroup();
        customerGroup.setName("Silver customers");
        em.persist(customerGroup);
        customerGroup = new CustomerGroup();
        customerGroup.setName("Gold customers");
        em.persist(customerGroup);
        customerGroup = new CustomerGroup();
        customerGroup.setName("Pro customers");
        em.persist(customerGroup);

        Customer customer = new Customer();
        customer.setName("Poro corp");
        em.persist(customer);
        customer = new Customer();
        customer.setName("Pekka ltd");
        em.persist(customer);

        Product p = new Product();
        p.setName("Chair");
        em.persist(p);
        p = new Product();
        p.setName("Table");
        em.persist(p);
        p = new Product();
        p.setName("Installation");
        em.persist(p);
        p = new Product();
        p.setName("Consultation");
        em.persist(p);

        em.getTransaction().commit();
    }

    private JPAContainer<Invoice> invoices;
    private Table invoiceTable;
    private Form form;
    private JPAContainer<Customer> customers;
    private Form customerForm;
    private ComboBox customerSelect;
    private CheckBox bufferedInvoiceFormCheckbox;
    private Button commitButton;

    @Override
    protected void init(VaadinRequest request) {
        contentLayout = new VerticalLayout();
        setContent(contentLayout);
        bufferedInvoiceFormCheckbox = new CheckBox("Buffered Invoice form");

        contentLayout.addComponent(bufferedInvoiceFormCheckbox);
        customerSelect = new ComboBox("Select (or create) customer");
        customers = JPAContainerFactory.make(Customer.class,
                TestLauncherUI.PERSISTENCE_UNIT);
        customerSelect.setContainerDataSource(customers);
        customerSelect.setItemCaptionPropertyId("name");
        customerSelect.setNewItemsAllowed(true);
        customerSelect.setImmediate(true);
        customerSelect.setNewItemHandler(new AbstractSelect.NewItemHandler() {
            @Override
            public void addNewItem(String newItemCaption) {
                Customer customer = new Customer();
                customer.setName(newItemCaption);
                Object id = customers.addEntity(customer);
                customerSelect.setValue(id);
            }
        });

        customerSelect
                .addValueChangeListener(new Property.ValueChangeListener() {
                    @Override
                    public void valueChange(ValueChangeEvent event) {
                        invoices.removeAllContainerFilters();
                        EntityItem<Customer> entity = customers.getItem(event
                                .getProperty().getValue());
                        if (entity != null) {
                            invoices.addContainerFilter(new Compare.Equal(
                                    "customer", entity.getEntity()));
                        }
                        editCustomer(entity);
                    }
                });

        contentLayout.addComponent(customerSelect);

        invoices = JPAContainerFactory.make(Invoice.class,
                TestLauncherUI.PERSISTENCE_UNIT);
        invoiceTable = new Table("Invoices");
        invoiceTable.setContainerDataSource(invoices);
        invoiceTable
                .setVisibleColumns(new Object[] { "id", "customer", "date" });
        invoiceTable.setSelectable(true);
        invoiceTable.setImmediate(true);
        invoiceTable.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                editInvoice(event.getProperty().getValue());
            }
        });

        contentLayout.addComponent(invoiceTable);
        Button button = new Button("New invoice");
        button.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                addInvoice();
            }
        });
        contentLayout.addComponent(button);

    }

    protected void editCustomer(EntityItem<Customer> entityItem) {
        if (customerForm == null) {
            customerForm = new Form();
            customerForm.setCaption("EditCustomer groups");
            FieldFactory jpaContainerFieldFactory = new FieldFactory();
            jpaContainerFieldFactory.setMultiSelectType(CustomerGroup.class,
                    TwinColSelect.class);
            customerForm.setFormFieldFactory(jpaContainerFieldFactory);
            contentLayout.addComponent(customerForm);
        }
        customerForm.setItemDataSource(entityItem);
        customerForm
                .setVisibleItemProperties(new Object[] { "customerGroups" });

    }

    protected void editInvoice(Object value) {
        if (form == null) {
            form = new Form();
            form.setCaption("Invoice editor");
            FieldFactory jpaContainerFieldFactory = new FieldFactory();
            jpaContainerFieldFactory.setVisibleProperties(InvoiceRow.class,
                    "product", "description", "unit", "unitPrice");
            jpaContainerFieldFactory.setVisibleProperties(BillingAddress.class,
                    "street", "city", "postalCode");
            jpaContainerFieldFactory.setSingleSelectType(Customer.class,
                    ListSelect.class);
            form.setFormFieldFactory(jpaContainerFieldFactory);
            contentLayout.addComponent(form);
            commitButton = new Button("Commit", new Button.ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    form.commit();
                }
            });
            // form.getFooter().addComponent(commitButton);
            contentLayout.addComponent(commitButton);
        }

        form.getFooter().setVisible(bufferedInvoiceFormCheckbox.getValue());
        // form.setWriteThrough(!bufferedInvoiceFormCheckbox.booleanValue());
        EntityItem<Invoice> item = invoices.getItem(value);
        form.setItemDataSource(item,
                Arrays.asList("customer", "date", "billingAddress", "rows"));
    }

    private static final Action NEW = new Action("New invoice");
    private static final Action[] invoiceTableActions = new Action[] { NEW };

    private void addInvoice() {
        Invoice invoice = new Invoice();
        invoice.setCustomer(customers.getEntityProvider().getEntity(customers,
                customerSelect.getValue()));
        Object id = invoices.addEntity(invoice);
        invoiceTable.setValue(id);
    }

}

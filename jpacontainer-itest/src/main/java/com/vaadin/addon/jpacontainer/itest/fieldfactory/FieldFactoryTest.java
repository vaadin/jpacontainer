package com.vaadin.addon.jpacontainer.itest.fieldfactory;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.itest.TestLauncherApplication;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Customer;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Invoice;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

public class FieldFactoryTest extends Window {
    
    public FieldFactoryTest() {
        final ComboBox comboBox = new ComboBox("Select (or create) customer");
        final JPAContainer<Customer> customers = JPAContainerFactory.make(Customer.class, TestLauncherApplication.PERSISTENCY_UNIT);
        comboBox.setContainerDataSource(customers);
        comboBox.setItemCaptionPropertyId("name");
        comboBox.setNewItemsAllowed(true);
        comboBox.setImmediate(true);
        comboBox.setNewItemHandler(new AbstractSelect.NewItemHandler() {
            @Override
            public void addNewItem(String newItemCaption) {
                Customer customer = new Customer();
                customer.setName(newItemCaption);
                Object id = customers.addEntity(customer);
                comboBox.setValue(id);
            }
        });
        comboBox.addListener(new Property.ValueChangeListener() {
            
            @Override
            public void valueChange(ValueChangeEvent event) {
                System.err.println("TODO filter table for customer id " + event.getProperty());
            }
        });
        
        addComponent(comboBox);
        
        JPAContainer<Invoice> invoices = JPAContainerFactory.make(Invoice.class, TestLauncherApplication.PERSISTENCY_UNIT);
        Table table = new Table();
        table.setContainerDataSource(invoices);
        table.setSelectable(true);
        table.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                System.err.println("TODO set item to form");
            }
        });
        
        addComponent(table);
        
        
        
        
        
        
        
    }

}

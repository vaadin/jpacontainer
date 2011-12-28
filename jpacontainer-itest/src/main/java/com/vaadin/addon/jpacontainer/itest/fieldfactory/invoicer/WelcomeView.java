package com.vaadin.addon.jpacontainer.itest.fieldfactory.invoicer;

import java.util.HashSet;

import javax.persistence.EntityManager;

import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.itest.TestLauncherApplication;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Customer;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.CustomerGroup;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Product;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class WelcomeView extends VerticalLayout {

    public WelcomeView() {
        setMargin(true);
        setSpacing(true);
        setCaption("Welcome");
        addComponent(new Label(
                "Hello Vaadin user. This is an example app using simple JPA annotated datamodel, JPAContainer and its FieldFactory to create a basic invoicing app."));

        addComponent(new Button("Generate test data",
                new Button.ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        generateTestData();
                    }

                }));
    }

    private void generateTestData() {
        EntityManager em = JPAContainerFactory.createEntityManagerForPersistenceUnit(TestLauncherApplication.PERSISTENCE_UNIT);
        em.getTransaction().begin();
        
        Product p = new Product();
        p.setName("Chair");
        em.persist(p);
        p = new Product();
        p.setName("Table");
        em.persist(p);
        
        Customer c = new Customer();
        c.setName("Good customer");
        em.persist(c);
        c = new Customer();
        c.setName("Better customer");
        em.persist(c);
        
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setName("Gold club");
        customerGroup.setCustomers(new HashSet<Customer>());
        customerGroup.getCustomers().add(c);
        em.persist(customerGroup);
        
        em.getTransaction().commit();
        em.close();
        getWindow().showNotification("Some test data generated");
    }
}

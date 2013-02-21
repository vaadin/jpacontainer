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

import java.io.IOException;
import java.util.HashSet;

import javax.persistence.EntityManager;

import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.itest.TestLauncherApplication;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Customer;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.CustomerGroup;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Product;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.VerticalLayout;

public class WelcomeView extends VerticalLayout {

    public WelcomeView() {
        setMargin(true);
        setSpacing(true);
        setCaption("Welcome");
        try {
            addComponent(new CustomLayout(getClass().getClassLoader().getResourceAsStream("welcome.html")));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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

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

import com.vaadin.addons.jpacontainer.demo.domain.Customer;
import java.util.Random;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A component that randomly generates test data for this demo application.
 * It implements the {@link ApplicationListener} interface and generates
 * the data when it receives a {@link ContextRefreshedEvent}.
 * <p>
 * When running inside a web application, this means that the data will be
 * generated once the application context has been fully initialized.
 * <p>
 * If you don't want the test data to be generated, either completely
 * delete this class or comment out the <code>@Repository</code>
 * annotation.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
@Repository(value = "testDataGenerator")
public class TestDataGenerator implements
        ApplicationListener<ContextRefreshedEvent> {

    private final Log logger = LogFactory.getLog(getClass());
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (logger.isInfoEnabled()) {
            logger.info("Received ContextRefreshedEvent, creating test data");
        }
        createTestData();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void createTestData() {
        if (entityManager == null) {
            throw new IllegalStateException("No EntityManager provided");
        }
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Generating test data for entity manager [" + entityManager + "]");
        }
        createCustomerTestData();
        entityManager.flush();
    }

    private void createCustomerTestData() {
        final String[] fnames = {"Peter", "Alice", "Joshua", "Mike", "Olivia",
            "Nina", "Alex", "Rita", "Dan", "Umberto", "Henrik", "Rene",
            "Lisa", "Marge"};
        final String[] lnames = {"Smith", "Gordon", "Simpson", "Brown", "Clavel",
            "Simons", "Verne", "Scott", "Allison", "Gates", "Rowling",
            "Barks", "Ross", "Schneider", "Tate"};

        final String[] streets = {"Magna Avenue", "Fringilla Street",
            "Aliquet St.", "Pharetra Avenue", "Gravida St.", "Risus Street",
            "Ultricies Street", "Mi Avenue", "Libero Av.", "Purus Avenue"};
        final String[] postOffices = {"Stockholm", "Helsinki", "Paris",
            "London", "Luxemburg", "Duckburg", "New York", "Tokyo", "Athens",
            "Sydney"};
        final String[] countries = {"Sweden", "Finland", "France", "United Kingdom", "Luxemburg", "United States", "United States", "Japan", "Greece", "Australia"};

        if (logger.isDebugEnabled()) {
            logger.debug("Generating customers");
        }

        Random rnd = new Random();
        for (int i = 0; i < 2000; i++) {
            Customer customer = new Customer();
            customer.setCustNo(i + 1);
            customer.setCustomerName(lnames[(int) (lnames.length * Math.random())] + " " + fnames[(int) (fnames.length * Math.
                    random())]);

            customer.getBillingAddress().setStreetOrBox(
                    (rnd.nextInt(1000) + 1) + " " + streets[(int) (streets.length * Math.
                    random())]);
            customer.getBillingAddress().setPostalCode(String.format("%05d", rnd.
                    nextInt(99998) + 1));
            int poIndex = (int) (postOffices.length * Math.random());
            customer.getBillingAddress().setPostOffice(postOffices[poIndex]);
            customer.getBillingAddress().setCountry(countries[poIndex]);

            customer.getShippingAddress().setStreetOrBox(
                    (rnd.nextInt(1000) + 1) + " " + streets[(int) (streets.length * Math.
                    random())]);
            customer.getShippingAddress().setPostalCode(String.format("%05d", rnd.
                    nextInt(99998) + 1));
            poIndex = (int) (postOffices.length * Math.random());
            customer.getShippingAddress().setPostOffice(postOffices[poIndex]);
            customer.getShippingAddress().setCountry(countries[poIndex]);
            if (logger.isTraceEnabled()) {
                logger.trace("Persisting customer [" + customer + "]");
            }
            entityManager.persist(customer);
        }
    }
}

/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addon.jpacontainer.demo;

import com.vaadin.addon.jpacontainer.demo.domain.Customer;
import com.vaadin.addon.jpacontainer.demo.domain.Invoice;
import com.vaadin.addon.jpacontainer.demo.domain.InvoiceItem;
import com.vaadin.addon.jpacontainer.demo.domain.Order;
import com.vaadin.addon.jpacontainer.demo.domain.OrderItem;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;
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
 * When initialized, the test database will be emptied and regenerated once
 * every hour.
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
    private ArrayList<Long> customerIds;
    private ArrayList<Long> orderIds;

    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (logger.isInfoEnabled()) {
            logger.info("Received ContextRefreshedEvent, creating test data");
        }
        createTestData();
    }

    @Transactional(propagation = Propagation.REQUIRED)
	//* 15 9-17 * * MON-FRI
	@Scheduled(cron="0 30 * * * *")
	public synchronized void deleteTestData() {
        if (entityManager == null) {
            throw new IllegalStateException("No EntityManager provided");
        }
		if (logger.isInfoEnabled()) {
			logger.info("Emptying test database");
		}
		long t = System.currentTimeMillis();
		entityManager.createQuery("DELETE InvoiceItem").executeUpdate();
		entityManager.createQuery("DELETE Invoice").executeUpdate();
		entityManager.createQuery("DELETE OrderItem").executeUpdate();
		entityManager.createQuery("DELETE CustomerOrder").executeUpdate();
		entityManager.createQuery("DELETE Customer").executeUpdate();
		entityManager.flush();
		if (logger.isInfoEnabled()) {
			logger.info("Test database emptied in " + (System.currentTimeMillis() - t) + " ms");
		}
	}

    @Transactional(propagation = Propagation.REQUIRED)
	@Scheduled(cron="10 30 * * * *")
    public synchronized void createTestData() {
        if (entityManager == null) {
            throw new IllegalStateException("No EntityManager provided");
        }
		if (logger.isInfoEnabled()) {
			logger.info("Generating test data");
		}
		long t = System.currentTimeMillis();
        createCustomerTestData();
        createOrderTestData();
        createInvoiceTestData();
        entityManager.flush();
        // Clean up
        customerIds.clear();
        customerIds = null;
        orderIds.clear();
        orderIds = null;
        if (logger.isInfoEnabled()) {
            logger.info("Test data generation completed in " + (System.currentTimeMillis() - t + " ms"));
        }
    }
    final String[] salesReps = {"John Smith",
        "Scrooge McDuck",
        "Maxwell Smart",
        "Joe Cool",
        "Mick Dundee",
        "Adam Anderson",
        "Zandra Dickson"
    };
    final String[] products = {"Prepostulator",
        "Movable hole",
        "Chess computer",
        "Invisible ink",
        "Foobar",
        "Super glue",
        "Stargate",
        "Dial-home device",
        "Cellphone",
        "Kitchen sink",
        "Racecar",
        "Drumkit",
        "Submarine",
        "Some magazine",
        "Toilet paper",
        "Coal",
        "Orange juice"
    };
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
    final String[] countries = {"Sweden", "Finland", "France", "United Kingdom",
        "Luxemburg", "United States", "United States", "Japan", "Greece",
        "Australia"};

    private void createCustomerTestData() {
        if (logger.isDebugEnabled()) {
            logger.debug("Generating customers");
        }

        Random rnd = new Random();
        customerIds = new ArrayList(2000);
        for (int i = 0; i < 2000; i++) {
            Customer customer = new Customer();
            customer.setCustNo(i + 1);
            customer.setCustomerName(fnames[(int) (fnames.length * Math.random())] + " " + lnames[(int) (lnames.length * Math.
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
            customer.setNotes("No orders");
            entityManager.persist(customer);
            customerIds.add(customer.getId());
        }
    }

    private void createOrderTestData() {
        if (logger.isDebugEnabled()) {
            logger.debug("Generating orders");
        }

        Random rnd = new Random();
        orderIds = new ArrayList(3000);
        for (int i = 0; i < 3000; i++) {
            Order order = new Order();
            Customer customer = entityManager.find(Customer.class, customerIds.
                    get(rnd.nextInt(customerIds.size())));
            order.setOrderNo(i + 1);
            order.setCustomer(customer);
            order.setOrderDate(createRandomDate());
            if (customer.getLastOrderDate() == null || customer.getLastOrderDate().
                    before(order.getOrderDate())) {
                customer.setLastOrderDate(order.getOrderDate());
            }
            customer.setNotes(""); // Removes the "No orders" default note.
            order.setCustomerReference(customer.getCustomerName());
            order.setSalesReference(
                    salesReps[(int) (salesReps.length * Math.random())]);

            order.getBillingAddress().setStreetOrBox(customer.getBillingAddress().getStreetOrBox());
            order.getBillingAddress().setPostalCode(customer.getBillingAddress().getPostalCode());
            order.getBillingAddress().setPostOffice(customer.getBillingAddress().getPostOffice());
            order.getBillingAddress().setCountry(customer.getBillingAddress().getCountry());

            order.getShippingAddress().setStreetOrBox(
                    (rnd.nextInt(1000) + 1) + " " + streets[(int) (streets.length * Math.
                    random())]);
            order.getShippingAddress().setPostalCode(String.format("%05d", rnd.
                    nextInt(99998) + 1));
            int poIndex = (int) (postOffices.length * Math.random());
            order.getShippingAddress().setPostOffice(postOffices[poIndex]);
            order.getShippingAddress().setCountry(countries[poIndex]);

            order.setShippedDate(addDaysToDate(order.getOrderDate(), rnd.nextInt(31)));

            for (int n = 0; n < rnd.nextInt(9) + 1; n++) {
                OrderItem item = new OrderItem();
                item.setDescription(products[(int) (products.length * Math.
                        random())]);
                item.setQuantity(rnd.nextInt(10) + 1);
                item.setPrice(rnd.nextInt(1000));
                order.addItem(item);
            }

            entityManager.persist(order);
            orderIds.add(order.getId());
        }
    }

    private void createInvoiceTestData() {
        if (logger.isDebugEnabled()) {
            logger.debug("Generating invoices");
        }
        Set<Order> orders = new HashSet<Order>();
        Random rnd = new Random();
        for (int i = 0; i < 2500; i++) {
            Invoice invoice = new Invoice();
            Order order;
            do {
                order = entityManager.find(Order.class, orderIds.get(rnd.nextInt(orderIds.size())));
            } while (orders.contains(order));
            orders.add(order);
            invoice.setInvoiceNo(i + 1);
            invoice.setOrder(order);
            invoice.setInvoiceDate(addDaysToDate(order.getOrderDate(), rnd.nextInt(8)));
            invoice.setDueDate(addDaysToDate(invoice.getInvoiceDate(), 14));
            order.getCustomer().setLastInvoiceDate(invoice.getInvoiceDate());
            order.setBilledDate(invoice.getInvoiceDate());
            if (rnd.nextInt(2) == 1) {
                invoice.setPaidDate(addDaysToDate(invoice.getInvoiceDate(), rnd.nextInt(14)));
            }
            for (OrderItem orderItem : order.getItems()) {
                InvoiceItem item = new InvoiceItem();
                item.setDescription(orderItem.getDescription());
                item.setQuantity(orderItem.getQuantity());
                item.setPrice(orderItem.getPrice());
                invoice.addItem(item);
            }
            entityManager.persist(invoice);
        }
        orders.clear();
        orders = null;
    }

    private static Random dateRnd = new Random();

    private static Date createRandomDate() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR, 1970 + dateRnd.nextInt(40));
        cal.set(Calendar.MONTH, dateRnd.nextInt(12));
        cal.set(Calendar.DATE,
                dateRnd.nextInt(cal.getMaximum(Calendar.DATE)) + 1);
        return cal.getTime();
    }

    private static Date addDaysToDate(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }
}

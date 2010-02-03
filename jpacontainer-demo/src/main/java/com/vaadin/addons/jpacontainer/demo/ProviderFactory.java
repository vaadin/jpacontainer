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
import com.vaadin.addons.jpacontainer.demo.domain.Customer;
import com.vaadin.addons.jpacontainer.demo.domain.Order;
import com.vaadin.addons.jpacontainer.provider.LocalEntityProvider;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

/**
 * A component that creates the different {@link EntityProvider}s used in this
 * application. There are many different ways of creating an entity provider,
 * this is just one example.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
@Component(value = "entityProviderFactory")
public class ProviderFactory {

    @PersistenceContext
    private EntityManager entityManager;
    private EntityProvider<Customer> customerEntityProvider;
    private EntityProvider<Order> orderEntityProvider;

    /**
     * Gets the {@link EntityProvider} for {@link Customer}s.
     *
     * @return the entity provider (never null).
     */
    public EntityProvider<Customer> getCustomerEntityProvider() {
        if (entityManager == null) {
            throw new IllegalStateException("No EntityManager provided");
        }
        if (customerEntityProvider == null) {
            customerEntityProvider = new LocalEntityProvider<Customer>(
                    Customer.class, entityManager);
        }
        return customerEntityProvider;
    }

    /**
     * Gets the {@link EntityProvider} for {@link Order}s.
     *
     * @return the entity provider (never null).
     */
    public EntityProvider<Order> getOrderEntityProvider() {
        if (entityManager == null) {
            throw new IllegalStateException("No EntityManager provided");
        }
        if (orderEntityProvider == null) {
            orderEntityProvider = new LocalEntityProvider<Order>(Order.class,
                    entityManager);
        }
        return orderEntityProvider;
    }
}

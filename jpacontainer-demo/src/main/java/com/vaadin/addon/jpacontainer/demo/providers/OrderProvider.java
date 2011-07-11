/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.demo.providers;

import com.vaadin.addon.jpacontainer.demo.domain.Order;
import org.springframework.stereotype.Repository;

/**
 * Entity provider for {@link Order}s that uses Spring's declarative
 * transaction annotations. It is also annotated with the {@link Repository} annotation,
 * which means that the Spring container will automatically detect it and
 * add it to the container.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
@Repository(value = "orderProvider")
public class OrderProvider extends LocalEntityProviderBean<Order> {

    /**
     * Creates a new <code>OrderProvider</code>.
     */
    public OrderProvider() {
        super(Order.class);
    }
}

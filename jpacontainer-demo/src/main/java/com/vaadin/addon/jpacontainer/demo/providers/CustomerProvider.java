/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.demo.providers;

import com.vaadin.addon.jpacontainer.demo.domain.Customer;
import org.springframework.stereotype.Repository;

/**
 * Entity provider for {@link Customer}s that uses Spring's declarative
 * transaction annotations. It is also annotated with the {@link Repository} annotation,
 * which means that the Spring container will automatically detect it and
 * add it to the container.
 *
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
@Repository(value = "customerProvider")
public class CustomerProvider extends LocalEntityProviderBean<Customer> {

    /**
     * Creates a new <code>CustomerProvider</code>.
     */
    public CustomerProvider() {
        super(Customer.class);
    }
}

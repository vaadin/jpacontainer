/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.demo.providers;

import com.vaadin.addon.jpacontainer.demo.domain.Invoice;
import org.springframework.stereotype.Repository;

/**
 * Entity provider for {@link Invoice}s that uses Spring's declarative
 * transaction annotations. It is also annotated with the {@link Repository} annotation,
 * which means that the Spring container will automatically detect it and
 * add it to the container.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
@Repository(value = "invoiceProvider")
public class InvoiceProvider extends LocalEntityProviderBean<Invoice> {

    /**
     * Creates a new <code>InvoiceProvider</code>.
     */
    public InvoiceProvider() {
        super(Invoice.class);
    }
}

/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.demo.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Example domain object for the JPAContainer demo application.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
@Entity
public class InvoiceItem extends AbstractItem {

    @ManyToOne
    @JoinColumn(nullable = false)
    private Invoice invoice;

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
}

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
package com.vaadin.addons.jpacontainer.demo.domain;

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

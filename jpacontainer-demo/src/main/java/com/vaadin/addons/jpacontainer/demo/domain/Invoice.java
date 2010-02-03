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

import com.vaadin.addons.jpacontainer.demo.util.DateUtils;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import org.apache.commons.lang.ObjectUtils;

/**
 * Example domain object for the JPAContainer demo application.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
@Entity
public class Invoice implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Version
    private Long version;
    @Column(unique = true, nullable = false)
    private Integer invoiceNo;
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date invoiceDate;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Order order;
    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date dueDate;
    @Temporal(TemporalType.DATE)
    private Date paidDate;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL,
    mappedBy = "invoice")
    private Set<InvoiceItem> items = new HashSet();

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Long getId() {
        return id;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public Integer getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(Integer invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public Set<InvoiceItem> getItems() {
        return Collections.unmodifiableSet(items);
    }

    public void addItem(InvoiceItem item) {
        item.setInvoice(this);
        items.add(item);
    }

    public void removeItem(InvoiceItem item) {
        item.setInvoice(null);
        items.remove(item);
    }

    public Integer getTotal() {
        Integer sum = 0;

        for (InvoiceItem item : items) {
            sum += item.getTotal();
        }

        return sum;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Address getInvoiceAddress() {
        return getOrder() == null ? null : getOrder().getBillingAddress();
    }

    public String getCustomerReference() {
        return getOrder() == null ? null : getOrder().getCustomerReference();
    }

    public String getSalesReference() {
        return getOrder() == null ? null : getOrder().getSalesReference();
    }

    public Date getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(Date paidDate) {
        this.paidDate = paidDate;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == getClass()) {
            Invoice o = (Invoice) obj;
            return ObjectUtils.equals(invoiceNo, o.invoiceNo)
                    && DateUtils.isSameDayOrNull(invoiceDate, o.invoiceDate)
                    && ObjectUtils.equals(order, o.order)
                    && DateUtils.isSameDayOrNull(dueDate, o.dueDate)
                    && DateUtils.isSameDayOrNull(paidDate, o.paidDate)
                    && ObjectUtils.equals(items, o.items);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + ObjectUtils.hashCode(invoiceNo);
        hash = hash * 31 + DateUtils.sameDayHashCode(invoiceDate);
        hash = hash * 31 + ObjectUtils.hashCode(order);
        hash = hash * 31 + DateUtils.sameDayHashCode(dueDate);
        hash = hash * 31 + DateUtils.sameDayHashCode(paidDate);
        hash = hash * 31 + ObjectUtils.hashCode(items);
        return hash;
    }
}

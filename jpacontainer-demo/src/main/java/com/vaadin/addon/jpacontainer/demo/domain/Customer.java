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
package com.vaadin.addon.jpacontainer.demo.domain;

import com.vaadin.addon.jpacontainer.demo.util.DateUtils;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
public class Customer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Version
    private Long version;
    @Column(unique = true, nullable = false)
    private Integer custNo;
    @Column(nullable = false)
    private String customerName = "";
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "streetOrBox", column =
        @Column(name = "bill_streetOrBox")),
        @AttributeOverride(name = "postalCode", column =
        @Column(name = "bill_postalCode")),
        @AttributeOverride(name = "postOffice", column =
        @Column(name = "bill_postOffice")),
        @AttributeOverride(name = "country", column =
        @Column(name = "bill_country"))
    })
    private Address billingAddress = new Address();
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "streetOrBox", column =
        @Column(name = "ship_streetOrBox")),
        @AttributeOverride(name = "postalCode", column =
        @Column(name = "ship_postalCode")),
        @AttributeOverride(name = "postOffice", column =
        @Column(name = "ship_postOffice")),
        @AttributeOverride(name = "country", column =
        @Column(name = "ship_country"))
    })
    private Address shippingAddress = new Address();
    @Temporal(TemporalType.DATE)
    private Date lastInvoiceDate;
    @Temporal(TemporalType.DATE)
    private Date lastOrderDate;
    private String notes = "";

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public Integer getCustNo() {
        return custNo;
    }

    public void setCustNo(Integer custNo) {
        this.custNo = custNo;
    }

    public Address getBillingAddress() {
        return billingAddress;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Date getLastInvoiceDate() {
        return lastInvoiceDate;
    }

    public void setLastInvoiceDate(Date lastInvoiceDate) {
        this.lastInvoiceDate = lastInvoiceDate;
    }

    public Date getLastOrderDate() {
        return lastOrderDate;
    }

    public void setLastOrderDate(Date lastOrderDate) {
        this.lastOrderDate = lastOrderDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    @Override
    public String toString() {
        return String.format(
                "Customer(no=%d,name=%s,billingAddr=(%s),shippingAddr=(%s),lastInvoiceDate=%s,lastOrderDate=%s,notes=%s)",
                custNo, customerName, billingAddress, shippingAddress,
                lastInvoiceDate, lastOrderDate, notes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == getClass()) {
            Customer o = (Customer) obj;
            return ObjectUtils.equals(custNo, o.custNo)
                    && ObjectUtils.equals(customerName, o.customerName)
                    && ObjectUtils.equals(billingAddress, o.billingAddress)
                    && ObjectUtils.equals(shippingAddress, o.shippingAddress)
                    && DateUtils.isSameDayOrNull(lastInvoiceDate,
                    o.lastInvoiceDate)
                    && DateUtils.isSameDayOrNull(lastOrderDate, o.lastOrderDate)
                    && ObjectUtils.equals(notes, o.notes);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + ObjectUtils.hashCode(custNo);
        hash = hash * 31 + ObjectUtils.hashCode(customerName);
        hash = hash * 31 + ObjectUtils.hashCode(billingAddress);
        hash = hash * 31 + ObjectUtils.hashCode(shippingAddress);
        hash = hash * 31 + DateUtils.sameDayHashCode(lastInvoiceDate);
        hash = hash * 31 + DateUtils.sameDayHashCode(lastOrderDate);
        hash = hash * 31 + ObjectUtils.hashCode(notes);
        return hash;
    }
}

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
package com.vaadin.addons.jpacontainer.demo.domain;

import com.vaadin.addons.jpacontainer.demo.util.DateUtils;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Entity(name = "CustomerOrder")
public class Order implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Version
    private Long version;
    @Column(nullable = false, unique = true)
    private Integer orderNo;
    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date orderDate;
    @ManyToOne(optional = false)
    private Customer customer;
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
    private String customerReference;
    private String salesReference;
    @Temporal(TemporalType.DATE)
    private Date shippedDate;
    @Temporal(TemporalType.DATE)
    private Date billedDate;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL,
    mappedBy = "order")
    private Set<OrderItem> items = new HashSet();

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public Date getBilledDate() {
        return billedDate;
    }

    public void setBilledDate(Date billedDate) {
        this.billedDate = billedDate;
    }

    public Address getBillingAddress() {
        return billingAddress;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }

    public Set<OrderItem> getItems() {
        return Collections.unmodifiableSet(items);
    }

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
    }

    public void removeItem(OrderItem item) {
        item.setOrder(null);
        items.remove(item);
    }

    public Integer getTotal() {
        Integer sum = 0;

        for (OrderItem item : items) {
            sum += item.getTotal();
        }

        return sum;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }

    public String getSalesReference() {
        return salesReference;
    }

    public void setSalesReference(String salesReference) {
        this.salesReference = salesReference;
    }

    public Date getShippedDate() {
        return shippedDate;
    }

    public void setShippedDate(Date shippedDate) {
        this.shippedDate = shippedDate;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == getClass()) {
            Order o = (Order) obj;
            return ObjectUtils.equals(orderNo, o.orderNo)
                    && DateUtils.isSameDayOrNull(orderDate, o.orderDate)
                    && ObjectUtils.equals(customer, o.customer)
                    && ObjectUtils.equals(customerReference, o.customerReference)
                    && ObjectUtils.equals(salesReference, o.salesReference)
                    && ObjectUtils.equals(billingAddress, o.billingAddress)
                    && ObjectUtils.equals(shippingAddress, o.shippingAddress)
                    && DateUtils.isSameDayOrNull(shippedDate, o.shippedDate)
                    && DateUtils.isSameDayOrNull(billedDate, o.billedDate)
                    && ObjectUtils.equals(items, o.items);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + ObjectUtils.hashCode(orderNo);
        hash = hash * 31 + DateUtils.sameDayHashCode(orderDate);
        hash = hash * 31 + ObjectUtils.hashCode(customer);
        hash = hash * 31 + ObjectUtils.hashCode(customerReference);
        hash = hash * 31 + ObjectUtils.hashCode(salesReference);
        hash = hash * 31 + ObjectUtils.hashCode(billingAddress);
        hash = hash * 31 + ObjectUtils.hashCode(shippingAddress);
        hash = hash * 31 + DateUtils.sameDayHashCode(shippedDate);
        hash = hash * 31 + DateUtils.sameDayHashCode(billedDate);
        hash = hash * 31 + ObjectUtils.hashCode(items);
        return hash;
    }
}

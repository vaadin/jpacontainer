/**
 * Copyright 2009-2013 Oy Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vaadin.addon.jpacontainer.itest.fieldfactory.domain;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Example entity using @Embedded for BillingAddress and @ElementCollection for
 * InvoiceRow. It also has Set<String> tags to test elementcollection with basic
 * data types.
 * <p>
 * The uppercaseText field in this pojo tests that value modifications and
 * possible validations at domain model level are used.
 * 
 */
@Entity
public class Invoice2 {

    public enum TestEnumTags {
        ZERO, ONE, TWO, THREE, FOUR
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();

    private String uppercaseText;
    @ManyToOne
    private Customer customer;
    @ElementCollection
    private List<InvoiceRow2> rows;
    @Embedded
    private BillingAddress2 billingAddress = new BillingAddress2();

    @ElementCollection
    private Set<String> tags;

    /*
     * Most commonly one would use Set here, but using List to ensure
     * ListTranslator works properly
     */
    @ElementCollection
    @OrderColumn
    private List<TestEnumTags> enumTags;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<InvoiceRow2> getRows() {
        return rows;
    }

    public void setRows(List<InvoiceRow2> rows) {
        this.rows = rows;
    }

    public BillingAddress2 getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(BillingAddress2 billingAddress) {
        this.billingAddress = billingAddress;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Invoice2) {
            Invoice2 p = (Invoice2) obj;
            if (this == p) {
                return true;
            }
            if (this.id == null || p.id == null) {
                return false;
            }
            return this.id.equals(p.id);
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getUppercaseText() {
        return uppercaseText;
    }

    public void setUppercaseText(String uppercaseText) {
        if (uppercaseText == null || uppercaseText.isEmpty()) {
            uppercaseText = "EMPTY";
        } else {
            uppercaseText = uppercaseText.toUpperCase();
        }
        this.uppercaseText = uppercaseText;
    }

    public List<TestEnumTags> getEnumTags() {
        return enumTags;
    }

    public void setEnumTags(List<TestEnumTags> enumTags) {
        this.enumTags = enumTags;
    }

}

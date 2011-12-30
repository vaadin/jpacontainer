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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Example entity using @Embedded for BillingAddress and @ElementCollection for
 * InvoiceRow. It also has Set<String> tags to test elementcollection with basic
 * data types.
 * 
 * When bound to a Form with FieldFactory from JPAContainer add-on:
 * 
 * - customer will be edited a select of customers backed by JPAContainer
 * listing Customer entities
 * 
 * - rows will be edited with a Table based "master-detail editor" backed up
 * JPAContainer listing InvoiceRows related to this entity
 * 
 * - billingAddress will be edited with a sub form
 * 
 * See related screenshot for the result
 * 
 */
@Entity
public class Invoice2 {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();
    @ManyToOne
    private Customer customer;
    @ElementCollection
    private List<InvoiceRow2> rows;
    @Embedded
    private BillingAddress2 billingAddress = new BillingAddress2();
    
    @ElementCollection
    private Set<String> tags;

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

}

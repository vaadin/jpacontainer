package com.vaadin.addon.jpacontainer.itest.fieldfactory.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Example entity. When bound to a Form with FieldFactory from JPAContainer
 * add-on:
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
public class Invoice {
    
    public enum State {
        DRAFT,
        SENT,
        PAYED
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();
    @ManyToOne
    private Customer customer;
    @OneToMany(mappedBy = "invoice", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<InvoiceRow> rows;
    @OneToOne
    private BillingAddress billingAddress;
    
    private State state;

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

    public List<InvoiceRow> getRows() {
        return rows;
    }

    public void setRows(List<InvoiceRow> rows) {
        this.rows = rows;
    }

    public BillingAddress getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(BillingAddress billingAddress) {
        this.billingAddress = billingAddress;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Invoice) {
            Invoice p = (Invoice) obj;
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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }


}

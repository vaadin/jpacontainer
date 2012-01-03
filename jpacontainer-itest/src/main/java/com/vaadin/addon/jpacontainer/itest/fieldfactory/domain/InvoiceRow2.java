package com.vaadin.addon.jpacontainer.itest.fieldfactory.domain;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

@Embeddable
public class InvoiceRow2 {

    private String description;
    private Double amount;
    private Double unitPrice;
    private String unit = "pcs";
    @ManyToOne
    private Product product;
    
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Double getAmount() {
        return amount;
    }
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    public Double getUnitPrice() {
        return unitPrice;
    }
    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }
    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }
    public Product getProduct() {
        return product;
    }
    public void setProduct(Product product) {
        this.product = product;
    }
    
}

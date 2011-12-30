package com.vaadin.addon.jpacontainer.itest.fieldfactory.domain;

import javax.persistence.Embeddable;

@Embeddable
public class BillingAddress2 {
    
    private String street;
    private String city;
    private String postalCode;
    
    public String getStreet() {
        return street;
    }
    public void setStreet(String street) {
        this.street = street;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getPostalCode() {
        return postalCode;
    }
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
}

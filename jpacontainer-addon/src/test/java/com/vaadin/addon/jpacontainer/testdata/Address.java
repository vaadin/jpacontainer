/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.testdata;

import java.io.Serializable;
import javax.persistence.Embeddable;
import org.apache.commons.lang.ObjectUtils;

/**
 * Embeddable Java bean for testing.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
@SuppressWarnings("serial")
@Embeddable
public class Address implements Serializable, Cloneable {

    private String street;
    private String postalCode;
    private String postOffice;
    private transient String tempData;

    public String getPostOffice() {
        return postOffice;
    }

    public void setPostOffice(String postOffice) {
        this.postOffice = postOffice;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getFullAddress() {
        return street + " " + postalCode + " " + postOffice;
    }

    public String getTempData() {
        return tempData;
    }

    public void setTempData(String tempData) {
        this.tempData = tempData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Address) {
            Address o = (Address) obj;
            return ObjectUtils.equals(street, o.street)
                    && ObjectUtils.equals(postalCode, o.postalCode)
                    && ObjectUtils.equals(postOffice, o.postOffice);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCode(street)
                + 7 * ObjectUtils.hashCode(postalCode)
                + 7 * ObjectUtils.hashCode(postOffice);
    }

    @Override
    public String toString() {
        return street + ", " + postalCode + " " + postOffice;
    }

    @Override
    public Address clone() {
        Address a = new Address();
        a.postOffice = postOffice;
        a.postalCode = postalCode;
        a.street = street;
        a.tempData = tempData;
        return a;
    }
}

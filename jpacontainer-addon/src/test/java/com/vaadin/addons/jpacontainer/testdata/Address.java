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
package com.vaadin.addons.jpacontainer.testdata;

import java.io.Serializable;
import javax.persistence.Embeddable;
import org.apache.commons.lang.ObjectUtils;

/**
 * Embeddable Java bean for testing.
 * 
 * @author Petter Holmström (IT Mill)
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

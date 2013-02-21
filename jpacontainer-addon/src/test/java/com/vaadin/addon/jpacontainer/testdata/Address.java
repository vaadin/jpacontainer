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

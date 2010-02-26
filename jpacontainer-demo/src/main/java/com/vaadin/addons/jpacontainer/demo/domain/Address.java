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

import java.io.Serializable;
import javax.persistence.Embeddable;
import org.apache.commons.lang.ObjectUtils;

/**
 * Example embeddable object for the JPAContainer demo application.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
@Embeddable
public class Address implements Serializable {

    private String streetOrBox = "";
    private String postalCode = "";
    private String postOffice = "";
    private String country = "";

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

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

    public String getStreetOrBox() {
        return streetOrBox;
    }

    public void setStreetOrBox(String streetOrBox) {
        this.streetOrBox = streetOrBox;
    }

    @Override
    public String toString() {
        return String.format("%s, %s %s, %s", streetOrBox, postalCode,
                postOffice, country);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == getClass()) {
            Address o = (Address) obj;
            return ObjectUtils.equals(streetOrBox, o.streetOrBox)
                    && ObjectUtils.equals(postalCode, o.postalCode)
                    && ObjectUtils.equals(postOffice, o.postOffice)
                    && ObjectUtils.equals(country, o.country);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + ObjectUtils.hashCode(streetOrBox);
        hash = hash * 31 + ObjectUtils.hashCode(postalCode);
        hash = hash * 31 + ObjectUtils.hashCode(postOffice);
        hash = hash * 31 + ObjectUtils.hashCode(country);
        return hash;
    }
}

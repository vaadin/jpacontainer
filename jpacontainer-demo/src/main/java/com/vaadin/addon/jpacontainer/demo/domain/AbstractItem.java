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
/*
JPAContainer
Copyright (C) 2009-2011 Oy Vaadin Ltd

This program is available under GNU Affero General Public License (version
3 or later at your option).

See the file licensing.txt distributed with this software for more
information about licensing.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addon.jpacontainer.demo.domain;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import org.apache.commons.lang.ObjectUtils;

/**
 * Example domain object for the JPAContainer demo application.
 *
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
@MappedSuperclass
public abstract class AbstractItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Version
    private Long version;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private Integer quantity = 0;
    @Column(nullable = false)
    private Integer price = 0;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getVersion() {
        return version;
    }

    public Integer getTotal() {
        return getQuantity() * getPrice();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == getClass()) {
            AbstractItem o = (AbstractItem) obj;
            return ObjectUtils.equals(description, o.description)
                    && ObjectUtils.equals(quantity, o.quantity)
                    && ObjectUtils.equals(price, o.price);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + ObjectUtils.hashCode(description);
        hash = hash * 31 + ObjectUtils.hashCode(quantity);
        hash = hash * 31 + ObjectUtils.hashCode(price);
        return hash;
    }
}

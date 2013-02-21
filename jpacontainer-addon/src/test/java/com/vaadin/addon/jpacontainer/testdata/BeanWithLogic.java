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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Entity Java bean for testing.
 * 
 * @since 2.0
 */
@SuppressWarnings("serial")
@Entity
public class BeanWithLogic implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BeanWithLogic) {
            BeanWithLogic p = (BeanWithLogic) obj;
            if (this == p) {
                return true;
            }
            if (id == null || p.id == null) {
                return false;
            }
            return id.equals(p.id);
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    private String name;

    public String getName() {
        return name;
    }

    /**
     * Sets the name field. Converts the given value to uppercase, null values are replaced with "NOT SET". 
     */
    public void setName(String name) {
        if(name == null || name.isEmpty()) {
            this.name = "NOT SET";
        } else {
            this.name = name.toUpperCase();
        }
    }

}

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
package com.vaadin.addon.jpacontainer.itest.targetentity.domain;

import java.util.Set;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(targetEntity = AbstractEconomicObject.class)
    @JoinColumn(name = "EconomicObject_ID")
    private EconomicObject economicObject;
    
    @ManyToMany(targetEntity = AbstractEconomicObject.class)
    private Set manyToMany;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EconomicObject getEconomicObject() {
        return economicObject;
    }

    public void setEconomicObject(EconomicObject economicObject) {
        this.economicObject = economicObject;
    }

    public Set getManyToMany() {
        return manyToMany;
    }

    public void setManyToMany(Set manyToMany) {
        this.manyToMany = manyToMany;
    }
}

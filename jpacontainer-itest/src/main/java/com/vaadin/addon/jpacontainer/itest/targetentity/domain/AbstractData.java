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

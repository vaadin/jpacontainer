package com.vaadin.addon.jpacontainer.testdata;

import java.util.Set;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToMany(targetEntity = AbstractEconomicObject.class)
    private Set manyToMany;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set getManyToMany() {
        return manyToMany;
    }

    public void setManyToMany(Set manyToMany) {
        this.manyToMany = manyToMany;
    }
}

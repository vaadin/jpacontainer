package com.vaadin.addon.jpacontainer.itest.targetentity.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class AbstractEconomicObject implements EconomicObject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.addon.jpacontainer.itest.targetentity.domain.EconomicObject
     * #foo()
     */
    @Override
    public void foo() {
        // TODO Auto-generated method stub

    }
}

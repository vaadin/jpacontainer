package com.vaadin.addon.jpacontainer.itest.targetentity;

import static org.junit.Assert.*;

import java.util.Set;

import javax.persistence.EntityManager;


import org.junit.Test;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.itest.targetentity.domain.AbstractEconomicObject;
import com.vaadin.addon.jpacontainer.itest.targetentity.domain.Data;
import com.vaadin.addon.jpacontainer.itest.targetentity.domain.EconomicObject;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

public class TargetEntity extends Window {

    static {
        EntityManager em = JPAContainerFactory
                .createEntityManagerForPersistenceUnit("targetentity");
        em.getTransaction().begin();
        for (int i = 0; i < 10; i++) {
            Data d = new Data();
            EconomicObject e = new AbstractEconomicObject();
            em.persist(e);
            d.setEconomicObject(e);
            em.persist(d);
        }
        em.getTransaction().commit();
    }

    public TargetEntity() {
        super();
        JPAContainer<Data> container = JPAContainerFactory.make(Data.class,
                "targetentity");
        Table t = new Table(null, container);
        addComponent(t);
    }
    
    @Test
    public void testCollectionType() {
        JPAContainer<Data> container = JPAContainerFactory.make(Data.class,
                "targetentity");
        Class<?> type = container.getType("manyToMany");
        
        assertEquals(Set.class, type);
        
    }

}

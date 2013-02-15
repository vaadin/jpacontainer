package com.vaadin.addon.jpacontainer.itest.targetentity;

import javax.persistence.EntityManager;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.itest.targetentity.domain.AbstractEconomicObject;
import com.vaadin.addon.jpacontainer.itest.targetentity.domain.Data;
import com.vaadin.addon.jpacontainer.itest.targetentity.domain.EconomicObject;
import com.vaadin.addon.jpacontainer.util.HibernateLazyLoadingDelegate;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;

public class TargetEntity extends UI {

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

    @Override
    protected void init(VaadinRequest request) {
        JPAContainer<Data> container = JPAContainerFactory.make(Data.class,
                "targetentity");
        container.getEntityProvider().setLazyLoadingDelegate(
                new HibernateLazyLoadingDelegate());
        Table t = new Table(null, container);
        setContent(t);
    }

}

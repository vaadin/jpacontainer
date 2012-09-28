package com.vaadin.addon.jpacontainer.itest.lazyhibernate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.addon.jpacontainer.util.EntityManagerPerRequestHelper;
import com.vaadin.server.VaadinRequest;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.UI;

public class LazyHibernate extends UI implements HttpServletRequestListener {
    EntityManagerPerRequestHelper emprHelper;

    static {
        LazyHibernateDataGenerator.create();
    }

    public LazyHibernate() {
        emprHelper = new EntityManagerPerRequestHelper();
    }

    @Override
    public void onRequestStart(HttpServletRequest request,
            HttpServletResponse response) {
        System.out.println("Start");
        emprHelper.requestStart();
    }

    @Override
    public void onRequestEnd(HttpServletRequest request,
            HttpServletResponse response) {
        System.out.println("End");
        emprHelper.requestEnd();
    }

    @Override
    protected void init(VaadinRequest request) {
        setContent(new LazyHibernateMainView(this));
    }
}

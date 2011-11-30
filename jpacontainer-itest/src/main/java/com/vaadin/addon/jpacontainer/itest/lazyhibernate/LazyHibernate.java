package com.vaadin.addon.jpacontainer.itest.lazyhibernate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.addon.jpacontainer.util.EntityManagerPerRequestHelper;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Window;

public class LazyHibernate extends Window implements HttpServletRequestListener {
    EntityManagerPerRequestHelper emprHelper;

    static {
        LazyHibernateDataGenerator.create();
    }

    public LazyHibernate() {
        emprHelper = new EntityManagerPerRequestHelper();
    }

    @Override
    public void attach() {
        super.attach();
        setContent(new LazyHibernateMainView(this));
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
}

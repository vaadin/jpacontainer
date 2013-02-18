package com.vaadin.addon.jpacontainer.itest.lazyhibernate;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

public class LazyHibernate extends UI {
    static {
        LazyHibernateDataGenerator.create();
    }

    // @Override
    // public void onRequestStart(HttpServletRequest request,
    // HttpServletResponse response) {
    // System.out.println("Start");
    // emprHelper.requestStart();
    // }
    //
    // @Override
    // public void onRequestEnd(HttpServletRequest request,
    // HttpServletResponse response) {
    // System.out.println("End");
    // emprHelper.requestEnd();
    // }

    @Override
    protected void init(VaadinRequest request) {
        setContent(new LazyHibernateMainView());
    }
}

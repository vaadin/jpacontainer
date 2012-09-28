package com.vaadin.addon.jpacontainer.itest;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.h2.tools.Server;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Link;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class TestLauncherUI extends UI implements HttpServletRequestListener {

    public static final String PERSISTENCE_UNIT = "addressbook";
    private Window currentWindow;
    private static Server server;

    @Override
    public void init(VaadinRequest request) {
        if (server == null) {
            try {
                server = org.h2.tools.Server.createWebServer();
                server.start();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String url = server.getURL();
        getContent().addComponent(
                new Link("H2DB admin condole", new ExternalResource(url)));
    }

    @Override
    public void onRequestStart(HttpServletRequest request,
            HttpServletResponse response) {
        if (currentWindow instanceof HttpServletRequestListener) {
            ((HttpServletRequestListener) currentWindow).onRequestStart(
                    request, response);
        }
    }

    @Override
    public void onRequestEnd(HttpServletRequest request,
            HttpServletResponse response) {
        if (currentWindow instanceof HttpServletRequestListener) {
            ((HttpServletRequestListener) currentWindow).onRequestEnd(request,
                    response);
        }
    }
}

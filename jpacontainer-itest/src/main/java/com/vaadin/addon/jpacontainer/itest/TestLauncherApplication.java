package com.vaadin.addon.jpacontainer.itest;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.h2.tools.Server;

import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Window;

public class TestLauncherApplication extends Application implements
        HttpServletRequestListener {

    public static final String PERSISTENCE_UNIT = "addressbook";
    private Window currentWindow;
    private static Server server;

    @Override
    public void init() {
        Window window = new Window();
        window.addComponent(new Label(
                "JPAContainer test and playground app, add the test class name to your url to start"));
        setMainWindow(window);
        if(server == null) {
            try {
                server = org.h2.tools.Server.createWebServer();
                server.start();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String url = server.getURL();
        window.addComponent(new Link("H2DB admin condole", new ExternalResource(url)));
    }

    @Override
    public Window getWindow(String name) {
        Window window = super.getWindow(name);
        if (window == null && name != null && !"".equals(name)
                && !name.contains(".ico") && name.matches("[a-z].*")) {
            try {
                String className;
                if (name.contains(".")) {
                    className = name;
                } else {
                    className = getClass().getPackage().getName() + "." + name;

                }
                Class<?> forName = Class.forName(className);
                if (forName != null) {
                    Window newInstance = (Window) forName.newInstance();
                    window = newInstance;
                    addWindow(window);
                }
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        currentWindow = window;
        return window;
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

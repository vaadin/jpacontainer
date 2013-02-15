package com.vaadin.addon.jpacontainer.itest;

import java.sql.SQLException;

import org.h2.tools.Server;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Link;
import com.vaadin.ui.UI;

public class TestLauncherUI extends UI {

    public static final String PERSISTENCE_UNIT = "addressbook";
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
        setContent(new Link("H2DB admin condole", new ExternalResource(url)));
    }

}

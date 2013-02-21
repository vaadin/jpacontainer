/**
 * Copyright 2009-2013 Oy Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.demo;

import com.vaadin.Application;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Window;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Main demo application.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
@Component(value = "demoApplication")
@Scope(value = "prototype")
public class DemoApp extends Application {

    private TabSheet tabs;
    @PersistenceContext
    private transient EntityManager entityManager;

    @Override
    public void init() {
        tabs = new TabSheet();
        tabs.setSizeFull();

        tabs.addTab(new CustomerView(), "Customers", null);
        tabs.addTab(new Label("Orders"), "Orders", null); // TODO Add OrdersView
        tabs.addTab(new Label("Invoices"), "Invoices", null); // TODO Add InvoicesView

        Window mainWindow = new Window("JPAContainer Demo Application");
        mainWindow.addComponent(tabs);
        setMainWindow(mainWindow);
    }
}

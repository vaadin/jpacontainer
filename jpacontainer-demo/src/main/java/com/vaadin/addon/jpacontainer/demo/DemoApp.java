/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addon.jpacontainer.demo;

import com.vaadin.Application;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Main demo application.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
@Component(value = "demoApplication")
@Scope(value = "session")
public class DemoApp extends Application {

    private TabSheet tabs;
    @Autowired
    private CustomerView customerView;
    @Autowired
    private OrderView orderView;
    @Autowired
    private InvoiceView invoiceView;

    @Override
    public void init() {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSizeFull();
        Label header = new Label("JPAContainer Demo Application " + getVersion());
        header.setStyleName("h1");
        layout.addComponent(header);

		Label infoLbl = new Label("The database is shared between all users, so everyone will see any changes that you make. Its contents will be <strong>regenerated every half hour</strong>. The generation process takes about a minute, during which the demo application might be acting a bit strangely.", Label.CONTENT_XHTML);
		layout.addComponent(infoLbl);

        tabs = new TabSheet();
        tabs.setSizeFull();
        layout.addComponent(tabs);
        layout.setExpandRatio(tabs, 1);

        tabs.addTab(
                customerView,
                "Customers", null);
        tabs.addTab(orderView,
                "Orders", null);
        tabs.addTab(invoiceView, "Invoices",
                null);

        Window mainWindow = new Window("JPAContainer Demo Application", layout);
        setMainWindow(mainWindow);
        setTheme("JPAContainerDemo");
    }

	@Override
	public String getVersion() {
		return "1.0.5"; // TODO Replace this with a version number from the build system
	}
}

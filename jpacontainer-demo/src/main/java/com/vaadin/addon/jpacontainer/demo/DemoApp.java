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
/*
JPAContainer
Copyright (C) 2009-2011 Oy Vaadin Ltd

This program is available under GNU Affero General Public License (version
3 or later at your option).

See the file licensing.txt distributed with this software for more
information about licensing.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * @author Petter Holmström (Vaadin Ltd)
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
		return "${project.version}";
	}
}

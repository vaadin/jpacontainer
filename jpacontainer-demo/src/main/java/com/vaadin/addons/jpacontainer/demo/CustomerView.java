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

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * View for browsing and editing customers
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class CustomerView extends CustomComponent {

    public CustomerView() {
        init();
    }

    protected void init() {
        VerticalLayout layout = new VerticalLayout();

        // TODO Complete me!

        Table customerTable = new Table("Customers");
        {
            customerTable.setSizeFull();

        }
        layout.addComponent(customerTable);
        layout.setSizeFull();

        setCompositionRoot(layout);
    }
    
}

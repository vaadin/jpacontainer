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

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.demo.domain.Customer;
import com.vaadin.addon.jpacontainer.demo.domain.Invoice;
import com.vaadin.data.Item;
import com.vaadin.data.util.filter.Between;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.data.util.filter.Compare.GreaterOrEqual;
import com.vaadin.data.util.filter.Compare.Less;
import com.vaadin.data.util.filter.Compare.LessOrEqual;
import com.vaadin.data.util.filter.IsNull;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * View for browsing invoices.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
@Component(value = "invoiceView")
@Scope(value = "session")
public class InvoiceView extends CustomComponent {

    // TODO Add editing support!
    @Resource(name = "invoiceProvider")
    private EntityProvider<Invoice> invoiceProvider;
    @Resource(name = "customerProvider")
    private EntityProvider<Customer> customerProvider;
    private JPAContainer<Invoice> invoiceContainer = new JPAContainer<Invoice>(
            Invoice.class);
    private JPAContainer<Customer> customerContainer = new JPAContainer<Customer>(
            Customer.class);
    private ComboBox filterCustomer = new ComboBox("Customer:") {

        @Override
        public String getItemCaption(Object itemId) {
            Item item = getItem(itemId);
            return String.format("%s (%d)", item
                    .getItemProperty("customerName").getValue(), item
                    .getItemProperty("custNo").getValue());
        }
    };
    private DateField filterFrom = new DateField("From:");
    private DateField filterTo = new DateField("To:");
    private CheckBox filterOverdue = new CheckBox("Overdue");
    private Button filterBtn = new Button("Filter");
    private Button resetBtn = new Button("Reset");

    @PostConstruct
    public void init() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(true);

        HorizontalLayout toolbar = new HorizontalLayout();
        {
            customerContainer.setEntityProvider(customerProvider);
            customerContainer.setApplyFiltersImmediately(true);
            customerContainer.sort(new Object[] { "customerName", "custNo" },
                    new boolean[] { true, true });
            customerContainer.setReadOnly(true);

            filterCustomer.setNullSelectionAllowed(true);
            filterCustomer.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
            filterCustomer.setContainerDataSource(customerContainer);
            filterCustomer.setImmediate(true);

            toolbar.addComponent(filterCustomer);

            filterFrom.setResolution(DateField.RESOLUTION_DAY);
            filterFrom.setDateFormat("yyyy-MM-dd");
            filterTo.setResolution(DateField.RESOLUTION_DAY);
            filterTo.setDateFormat("yyyy-MM-dd");

            // toolbar.addComponent(new Label("Filter by Order Date:"));
            toolbar.addComponent(filterFrom);
            // toolbar.addComponent(new Label("-"));
            toolbar.addComponent(filterTo);

            toolbar.addComponent(filterOverdue);
            toolbar.setComponentAlignment(filterOverdue, Alignment.BOTTOM_LEFT);

            resetBtn.setEnabled(false);
            toolbar.addComponent(filterBtn);
            toolbar.addComponent(resetBtn);
            toolbar.setSpacing(true);
            toolbar.setMargin(false, false, true, false);
            toolbar.setComponentAlignment(filterBtn, Alignment.BOTTOM_LEFT);
            toolbar.setComponentAlignment(resetBtn, Alignment.BOTTOM_LEFT);

            filterBtn.addListener(new Button.ClickListener() {

                public void buttonClick(Button.ClickEvent event) {
                    doFilter();
                    getWindow().showNotification("Filter applied");
                }
            });

            resetBtn.addListener(new Button.ClickListener() {

                public void buttonClick(ClickEvent event) {
                    doReset();
                    getWindow().showNotification("Filter reset");
                }
            });

        }
        layout.addComponent(toolbar);

        Table invoiceTable = new Table();
        {
            invoiceContainer.setEntityProvider(invoiceProvider);
            invoiceContainer.setApplyFiltersImmediately(false);
            // Remove unused properties
            invoiceContainer.removeContainerProperty("id");
            invoiceContainer.removeContainerProperty("version");
            invoiceContainer.removeContainerProperty("items");

            // Add some nested properties
            invoiceContainer.addNestedContainerProperty("order.orderNo");
            invoiceContainer
                    .addNestedContainerProperty("order.customer.customerName");
            invoiceContainer
                    .addNestedContainerProperty("order.customer.custNo");
            invoiceContainer.addNestedContainerProperty("order.customer");

            invoiceTable.setSizeFull();
            invoiceTable.setContainerDataSource(invoiceContainer);
            invoiceTable.setVisibleColumns(new String[] { "invoiceNo",
                    "order.orderNo", "order.customer.custNo",
                    "order.customer.customerName", "invoiceDate", "dueDate",
                    "paidDate", "total" });
            invoiceTable.setColumnHeaders(new String[] { "Invoice No",
                    "Order No", "Cust No", "Customer", "Invoice Date",
                    "Due Date", "Paid Date", "Total Amount" });
            invoiceTable.setColumnAlignment("total", Table.ALIGN_RIGHT);
            invoiceTable.setColumnCollapsingAllowed(true);
            invoiceTable.setSelectable(true);
            invoiceTable.setImmediate(true);
            invoiceTable.setSortContainerPropertyId("invoiceNo");
        }
        layout.addComponent(invoiceTable);
        layout.setExpandRatio(invoiceTable, 1);

        setCompositionRoot(layout);
        setSizeFull();
    }

    protected void doFilter() {
        Date from = (Date) filterFrom.getValue();
        Date to = (Date) filterTo.getValue();
        Object customerId = filterCustomer.getValue();
        boolean overdue = filterOverdue.booleanValue();

        if (customerId == null && from == null && to == null && !overdue) {
            getWindow().showNotification("Nothing to do");
            return;
        }
        invoiceContainer.removeAllContainerFilters();

        if (customerId != null) {
            Customer c = customerContainer.getItem(customerId).getEntity();
            invoiceContainer.addContainerFilter(new Equal("order.customer", c));
        }

        if (overdue) {
            invoiceContainer
                    .addContainerFilter(new Less("dueDate", new Date()));
            invoiceContainer.addContainerFilter(new IsNull("paidDate"));
        }

        if (from != null && to != null) {
            if (to.before(from)) {
                getWindow().showNotification("Please check the dates!",
                        Notification.TYPE_WARNING_MESSAGE);
                return;
            }
            invoiceContainer.addContainerFilter(new Between("invoiceDate",
                    from, to));
        } else if (from != null) {
            invoiceContainer.addContainerFilter(new GreaterOrEqual(
                    "invoiceDate", from));
        } else if (to != null) {
            invoiceContainer.addContainerFilter(new LessOrEqual("invoiceDate",
                    to));
        }
        invoiceContainer.applyFilters();
        resetBtn.setEnabled(true);
    }

    protected void doReset() {
        filterTo.setValue(null);
        filterFrom.setValue(null);
        filterCustomer.setValue(null);
        filterOverdue.setValue(false);
        invoiceContainer.removeAllContainerFilters();
        invoiceContainer.applyFilters();
        resetBtn.setEnabled(false);
    }

    public void showInvoicesForCustomer(Object customerId) {
        filterTo.setValue(null);
        filterFrom.setValue(null);
        filterOverdue.setValue(false);
        filterCustomer.setValue(customerId);
        doFilter();
        if (getParent() instanceof TabSheet) {
            TabSheet parent = (TabSheet) getParent();
            parent.setSelectedTab(this);
        }
    }
}

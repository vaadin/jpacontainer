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

import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.demo.domain.Customer;
import com.vaadin.addon.jpacontainer.demo.domain.Order;
import com.vaadin.addon.jpacontainer.filter.Filters;
import com.vaadin.data.Item;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * View for browsing orders.
 *
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
@Component(value = "orderView")
@Scope(value = "session")
public class OrderView extends CustomComponent {

    // TODO Add editing support!
    @Resource(name = "orderProvider")
    private EntityProvider<Order> entityProvider;
    @Resource(name = "customerProvider")
    private EntityProvider<Customer> customerProvider;
    private JPAContainer<Order> orderContainer = new JPAContainer(Order.class);
    private JPAContainer<Customer> customerContainer = new JPAContainer(
            Customer.class);
    private ComboBox filterCustomer = new ComboBox("Customer:") {

        @Override
        public String getItemCaption(Object itemId) {
            Item item = getItem(itemId);
            return String.format("%s (%d)", item.getItemProperty("customerName").
                    getValue(),
                    item.getItemProperty("custNo").getValue());
        }
    };
    private DateField filterFrom = new DateField("From:");
    private DateField filterTo = new DateField("To:");
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
            customerContainer.setApplyPredicatesImmediately(true);
            customerContainer.sort(new Object[]{"customerName", "custNo"},
                    new boolean[]{true, true});
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

            toolbar.addComponent(filterFrom);
            toolbar.addComponent(filterTo);
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

        Table orderTable = new Table();
        {
            orderContainer.setEntityProvider(entityProvider);
            orderContainer.setApplyPredicatesImmediately(false);
            // Remove unused properties
            orderContainer.removeContainerProperty("id");
            orderContainer.removeContainerProperty("version");
            orderContainer.removeContainerProperty("items");

            // Add some nested properties
            orderContainer.addNestedContainerProperty("customer.customerName");
            orderContainer.addNestedContainerProperty("customer.custNo");
            orderContainer.addNestedContainerProperty("billingAddress.*");
            orderContainer.addNestedContainerProperty("shippingAddress.*");

            orderTable.setSizeFull();
            orderTable.setContainerDataSource(orderContainer);
            orderTable.setVisibleColumns(
                    new String[]{"orderNo",
                        "orderDate",
                        "customer.custNo",
                        "customer.customerName",
                        "customerReference",
                        "salesReference",
                        "billingAddress.streetOrBox",
                        "billingAddress.postalCode",
                        "billingAddress.postOffice",
                        "billingAddress.country",
                        "billedDate",
                        "shippingAddress.streetOrBox",
                        "shippingAddress.postalCode",
                        "shippingAddress.postOffice",
                        "shippingAddress.country",
                        "shippedDate",
                        "total"
                    });
            orderTable.setColumnHeaders(
                    new String[]{"Order No",
                        "Order Date",
                        "Cust No",
                        "Customer",
                        "Customer Ref",
                        "Sales Ref",
                        "BillTo Address",
                        "BillTo Postal Code",
                        "BillTo Post Office",
                        "BillTo Country",
                        "Billed Date",
                        "ShipTo Address",
                        "ShipTo Postal Code",
                        "ShipTo Post Office",
                        "ShipTo Country",
                        "Shipped Date",
                        "Total Amount"
                    });
            orderTable.setColumnAlignment("total", Table.ALIGN_RIGHT);
            orderTable.setColumnCollapsingAllowed(true);
            orderTable.setSelectable(true);
            orderTable.setImmediate(true);
            try {
                orderTable.setColumnCollapsed("customerReference", true);
                orderTable.setColumnCollapsed("shippingAddress.streetOrBox",
                        true);
                orderTable.setColumnCollapsed("shippingAddress.postalCode",
                        true);
                orderTable.setColumnCollapsed("shippingAddress.postOffice",
                        true);
                orderTable.setColumnCollapsed("shippingAddress.country",
                        true);
                orderTable.setColumnCollapsed("billingAddress.streetOrBox",
                        true);
                orderTable.setColumnCollapsed("billingAddress.postalCode",
                        true);
                orderTable.setColumnCollapsed("billingAddress.postOffice",
                        true);
                orderTable.setColumnCollapsed("billingAddress.country",
                        true);
            } catch (IllegalStateException e) {
                // Ignore it
            }
            orderTable.setSortContainerPropertyId("orderNo");
        }
        layout.addComponent(orderTable);
        layout.setExpandRatio(orderTable, 1);

        setCompositionRoot(layout);
        setSizeFull();
    }

    private void doReset() {
        filterTo.setValue(null);
        filterFrom.setValue(null);
        filterCustomer.setValue(null);
        orderContainer.removeAllPredicates();
        orderContainer.applyPredicates();
        resetBtn.setEnabled(false);
    }

    private void doFilter() {
        Date from = (Date) filterFrom.getValue();
        Date to = (Date) filterTo.getValue();
        Object customerId = filterCustomer.getValue();

        if (customerId == null && from == null && to == null) {
            getWindow().showNotification("Nothing to do");
            return;
        }

        orderContainer.removeAllPredicates();

        if (customerId != null) {
            Customer c = customerContainer.getItem(customerId).
                    getEntity();
            orderContainer.addPredicate(Filters.eq("customer",
                    c));
        }

        if (from != null && to != null) {
            if (to.before(from)) {
                getWindow().showNotification(
                        "Please check the dates!",
                        Notification.TYPE_WARNING_MESSAGE);
                return;
            }
            orderContainer.addPredicate(Filters.between("orderDate",
                    from,
                    to, true, true));
        } else if (from != null) {
            orderContainer.addPredicate(Filters.gteq("orderDate", from));
        } else if (to != null) {
            orderContainer.addPredicate(Filters.lteq("orderDate", to));
        }
        orderContainer.applyPredicates();
        resetBtn.setEnabled(true);
    }

    public void showOrdersForCustomer(Object customerId) {
        filterTo.setValue(null);
        filterFrom.setValue(null);
        filterCustomer.setValue(customerId);
        doFilter();
        if (getParent() instanceof TabSheet) {
            TabSheet parent = (TabSheet) getParent();
            parent.setSelectedTab(this);
        }
    }
}

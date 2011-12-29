package com.vaadin.addon.jpacontainer.itest.fieldfactory.invoicer;

import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Customer;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.CustomerGroup;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Invoice;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.InvoiceRow;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Product;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Window;

public class InvoicerTestApp extends Window implements
        Property.ValueChangeListener {

    private Tree navTree = new Tree("Views");
    private HorizontalSplitPanel horizontalSplitPanel = new HorizontalSplitPanel();

    public InvoicerTestApp() {
        navTree.addListener(this);

        horizontalSplitPanel.setSplitPosition(200, UNITS_PIXELS);
        horizontalSplitPanel.addComponent(navTree);
        setContent(horizontalSplitPanel);

        addView(new WelcomeView());

        addView(new BasicCrudView<Product>(Product.class));

        BasicCrudView<Customer> customerCrudView = new BasicCrudView<Customer>(
                Customer.class);
        customerCrudView.setVisibleTableProperties("name");
        addView(customerCrudView);

        BasicCrudView<CustomerGroup> groupCrudView = new BasicCrudView<CustomerGroup>(
                CustomerGroup.class);
        groupCrudView.setVisibleTableProperties("name");
        addView(groupCrudView);

        BasicCrudView<Invoice> invoiceCrudView = new BasicCrudView<Invoice>(
                Invoice.class);
        invoiceCrudView.setVisibleTableProperties("date", "customer");
        invoiceCrudView.setVisibleFormProperties("customer", "date",
                "billingAddress", "rows");
        // configure the order of properties in invoicerow master-detail editor
        invoiceCrudView.getFieldFactory().setVisibleProperties(
                InvoiceRow.class, "product", "description", "amount", "unit",
                "unitPrice");
        addView(invoiceCrudView);

        navTree.setSelectable(true);
        navTree.setNullSelectionAllowed(false);
        navTree.setImmediate(true);
        navTree.setValue(navTree.getItemIds().iterator().next());

    }

    private void addView(Component view) {
        navTree.addItem(view);
        if (view.getCaption() != null) {
            navTree.setItemCaption(view, view.getCaption());
        }
        navTree.setChildrenAllowed(view, false);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void valueChange(ValueChangeEvent event) {
        Component value = (Component) event.getProperty().getValue();
        if (value instanceof BasicCrudView) {
            BasicCrudView cv = (BasicCrudView) value;
            cv.refreshContainer();
        }
        horizontalSplitPanel.setSecondComponent(value);
    }

}

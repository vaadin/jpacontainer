package com.vaadin.addon.jpacontainer.itest.fieldfactory.invoicer;

import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Customer;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.CustomerGroup;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Invoice;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.InvoiceRow;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Product;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.Window;

public class InvoicerTestApp extends Window implements Handler {

    private TabSheet tabSheet;

    public InvoicerTestApp() {
        tabSheet = new TabSheet();

        tabSheet.addTab(new WelcomeView());

        tabSheet.addTab(new BasicCrudView<Product>(Product.class));

        BasicCrudView<Customer> customerCrudView = new BasicCrudView<Customer>(
                Customer.class);
        customerCrudView.setVisibleTableProperties("name");
        tabSheet.addTab(customerCrudView);

        BasicCrudView<CustomerGroup> groupCrudView = new BasicCrudView<CustomerGroup>(
                CustomerGroup.class);
        groupCrudView.setVisibleTableProperties("name");
        tabSheet.addTab(groupCrudView);

        BasicCrudView<Invoice> invoiceCrudView = new BasicCrudView<Invoice>(
                Invoice.class);
        invoiceCrudView.setVisibleTableProperties("date", "customer");
        invoiceCrudView.setVisibleFormProperties("customer", "date",
                "billingAddress", "rows");
        // configure the order of properties in invoicerow master-detail editor
        invoiceCrudView.getFieldFactory().setVisibleProperties(
                InvoiceRow.class, "product", "description", "amount", "unit",
                "unitPrice");
        tabSheet.addTab(invoiceCrudView);

        tabSheet.setSizeFull();

        tabSheet.addListener(new SelectedTabChangeListener() {
            @SuppressWarnings("rawtypes")
            @Override
            public void selectedTabChange(SelectedTabChangeEvent event) {
                Component selectedTab = event.getTabSheet().getSelectedTab();
                if (selectedTab instanceof BasicCrudView) {
                    BasicCrudView cv = (BasicCrudView) selectedTab;
                    cv.refreshContainer();
                }
            }
        });
        setContent(tabSheet);

        // due to an inconvenience with std Vaadin tabsheet, form and actions, we
        // catch enter (saving form) and CTRL + N (new entity) shortcuts here on
        // top level and pass the action to the active view.
        addActionHandler(this);

    }

    private static final ShortcutAction SAVE = new ShortcutAction("Save", KeyCode.ENTER, null);
    private static final ShortcutAction SAVE2 = new ShortcutAction("^Save");
    private static final ShortcutAction NEW = new ShortcutAction("^New");
    private static final Action[] ACTIONS = new Action[] {SAVE, SAVE2, NEW};

    @Override
    public Action[] getActions(Object target, Object sender) {
        return ACTIONS;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void handleAction(Action action, Object sender, Object target) {
        Component selectedTab = tabSheet.getSelectedTab();
        if (selectedTab instanceof BasicCrudView) {
            BasicCrudView cv = (BasicCrudView) selectedTab;
            if(action == NEW) {
                cv.addItem();
            } else if (action== SAVE) {
                if(cv.getForm().isVisible()) {
                    cv.getForm().commit();
                }
            }
            
        }

    }

}

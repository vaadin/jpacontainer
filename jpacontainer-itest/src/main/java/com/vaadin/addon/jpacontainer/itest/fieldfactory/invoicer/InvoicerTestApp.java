package com.vaadin.addon.jpacontainer.itest.fieldfactory.invoicer;

import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Product;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.Window;

public class InvoicerTestApp extends Window {

    public InvoicerTestApp() {
        TabSheet tabSheet = new TabSheet();
        tabSheet.addTab(new WelcomeView());
        tabSheet.addTab(new BasicCrudView<Product>(Product.class));
        tabSheet.addTab(new CustomerView());
        tabSheet.addTab(new CustomerGroupView());
        tabSheet.addTab(new InvoiceView());
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
    }

}

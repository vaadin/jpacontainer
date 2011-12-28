package com.vaadin.addon.jpacontainer.itest.fieldfactory.invoicer;

import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Customer;

public class CustomerView extends BasicCrudView<Customer> {

    public CustomerView() {
        super(Customer.class);
    }
    
    @Override
    public void buildView() {
        super.buildView();
        getTable().setVisibleColumns(new Object[] {"name"});
    }
    
}

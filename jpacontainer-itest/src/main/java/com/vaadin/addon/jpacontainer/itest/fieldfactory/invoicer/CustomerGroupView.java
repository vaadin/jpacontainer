package com.vaadin.addon.jpacontainer.itest.fieldfactory.invoicer;

import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.CustomerGroup;

public class CustomerGroupView extends BasicCrudView<CustomerGroup> {
    
    public CustomerGroupView() {
        super(CustomerGroup.class);
    }
    
    @Override
    public void buildView() {
        super.buildView();
        getTable().setVisibleColumns(new Object[] {"name"});
    }

}

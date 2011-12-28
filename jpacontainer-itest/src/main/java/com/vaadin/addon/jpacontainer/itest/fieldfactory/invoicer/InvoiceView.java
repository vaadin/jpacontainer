package com.vaadin.addon.jpacontainer.itest.fieldfactory.invoicer;

import java.util.Date;

import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.Invoice;
import com.vaadin.addon.jpacontainer.itest.fieldfactory.domain.InvoiceRow;
import com.vaadin.data.Property.ValueChangeEvent;

public class InvoiceView extends BasicCrudView<Invoice> {

    public InvoiceView() {
        super(Invoice.class);
    }
    
    @Override
    public void buildView() {
        super.buildView();
        getTable().setVisibleColumns(new Object[] {"date", "customer"});
    }
    
    @Override
    public void valueChange(ValueChangeEvent event) {
        super.valueChange(event);
        getForm().setVisibleItemProperties(new Object[] {"customer","date","billingAddress","rows"});
    }
    
    @Override
    protected void initFieldFactory() {
        super.initFieldFactory();        
        getFieldFactory().setVisibleProperties(InvoiceRow.class, "product", "description", "amount", "unit", "unitPrice");
    }
    
    @Override
    protected Invoice newInstance() throws InstantiationException,
            IllegalAccessException {
        Invoice instance = super.newInstance();
        instance.setDate(new Date());
        return instance ;
    }
}

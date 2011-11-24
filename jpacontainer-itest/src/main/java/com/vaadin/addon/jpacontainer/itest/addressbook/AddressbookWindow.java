package com.vaadin.addon.jpacontainer.itest.addressbook;

import com.vaadin.ui.Window;

public class AddressbookWindow extends Window {
    
    static {
        DemoDataGenerator.create();
    }
    
    public AddressbookWindow() {
    }
    
    @Override
    public void attach() {
        super.attach();
        setContent(new AddressBookMainView());
    }

}

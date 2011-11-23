package com.vaadin.addon.jpacontainer.itest.addressbook;

import com.vaadin.ui.Window;

public class AddressbookWindow extends Window {
    
    static {
        DemoDataGenerator.create();
    }
    
    public AddressbookWindow() {
        setContent(new AddressBookMainView());
    }

}

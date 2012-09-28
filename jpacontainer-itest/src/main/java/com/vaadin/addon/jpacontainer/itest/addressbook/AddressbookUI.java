package com.vaadin.addon.jpacontainer.itest.addressbook;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

public class AddressbookUI extends UI {

    static {
        DemoDataGenerator.create();
    }

    @Override
    protected void init(VaadinRequest request) {
        setContent(new AddressBookMainView());
    }

}

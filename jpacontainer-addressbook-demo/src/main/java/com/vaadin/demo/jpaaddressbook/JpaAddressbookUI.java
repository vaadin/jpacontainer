package com.vaadin.demo.jpaaddressbook;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

public class JpaAddressbookUI extends UI {

    public static final String PERSISTENCE_UNIT = "addressbook";

    static {
        DemoDataGenerator.create();
    }

    @Override
    protected void init(VaadinRequest request) {
        setContent(new AddressBookMainView());
    }
}

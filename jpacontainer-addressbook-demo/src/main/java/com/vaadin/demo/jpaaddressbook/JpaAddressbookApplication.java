package com.vaadin.demo.jpaaddressbook;

import com.vaadin.Application;
import com.vaadin.ui.Window;

public class JpaAddressbookApplication extends Application {

    public static final String PERSISTENCY_UNIT = "addressbook";

    static {
        DemoDataGenerator.create();
    }

    @Override
    public void init() {
        Window window = new Window();
        setMainWindow(window);
        window.setContent(new AddressBookMainView());
    }

}

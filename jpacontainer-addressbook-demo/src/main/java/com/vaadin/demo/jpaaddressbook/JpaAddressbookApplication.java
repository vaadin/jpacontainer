package com.vaadin.demo.jpaaddressbook;

import com.vaadin.Application;
import com.vaadin.ui.Window;

public class JpaAddressbookApplication extends Application {

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

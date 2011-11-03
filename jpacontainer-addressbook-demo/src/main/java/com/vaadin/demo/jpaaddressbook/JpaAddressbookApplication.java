package com.vaadin.demo.jpaaddressbook;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Window;

public class JpaAddressbookApplication extends Application implements
        HttpServletRequestListener {

    static {
        DemoDataGenerator.create();
    }

    private static ThreadLocal<JpaAddressbookApplication> threadLocalApplication = new ThreadLocal<JpaAddressbookApplication>();

    private ContainerFactory containerFactory;

    public JpaAddressbookApplication() {
        containerFactory = new ContainerFactory();
        threadLocalApplication.set(this);
    }

    @Override
    public void init() {
        Window window = new Window();
        setMainWindow(window);
        window.setContent(new AddressBookMainView());
    }

    public ContainerFactory getContainerFactory() {
        return containerFactory;
    }

    /**
     * @return The Application instance for the request that is currently being
     *         processed.
     */
    public static JpaAddressbookApplication getInstance() {
        return threadLocalApplication.get();
    }

    /**
     * Updates the threadLocalApplication for the current request
     */
    @Override
    public void onRequestStart(HttpServletRequest request,
            HttpServletResponse response) {
        threadLocalApplication.set(this);
    }

    /**
     * Clears the threadLocalApplication after the request has been finished
     */
    @Override
    public void onRequestEnd(HttpServletRequest request,
            HttpServletResponse response) {
        threadLocalApplication.remove();
    }
}

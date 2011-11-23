package com.vaadin.addon.jpacontainer.itest;

import com.vaadin.Application;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

public class TestLauncherApplication extends Application {

    public static final String PERSISTENCY_UNIT = "addressbook";


    @Override
    public void init() {
        Window window = new Window();
        window.addComponent(new Label("JPAContainer test and playground app, add the test class name to your url to start"));
        setMainWindow(window);
    }
    
    @Override
    public Window getWindow(String name) {
        Window window = (Window) super.getWindow(name);
        if (window == null && name != null && !"".equals(name)
                && !name.contains(".ico") && name.matches("[a-z].*")) {
            try {
                String className;
                if(name.contains(".")) {
                    className = name;
                } else {
                    className = getClass().getPackage().getName() + "."
                            + name;
                    
                }
                Class<?> forName = Class.forName(className);
                if (forName != null) {
                    Window newInstance = (Window) forName
                            .newInstance();
                    window = newInstance;
                    addWindow(window);
                }
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return window;
    }
}

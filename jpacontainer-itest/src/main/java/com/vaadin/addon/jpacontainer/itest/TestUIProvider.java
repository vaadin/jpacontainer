package com.vaadin.addon.jpacontainer.itest;

import com.vaadin.server.DefaultUIProvider;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

public class TestUIProvider extends DefaultUIProvider {

    @Override
    public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
        VaadinRequest request = event.getRequest();

        // Only use UI from web.xml for requests to the root
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !"/".equals(pathInfo)) {
            if (pathInfo.startsWith("/")) {
                pathInfo = pathInfo.substring(1);
            }
            try {
                String className;
                if (pathInfo.contains(".")) {
                    className = pathInfo;
                } else {
                    className = getClass().getPackage().getName() + "."
                            + pathInfo;

                }
                Class<? extends UI> forName = (Class<? extends UI>) Class
                        .forName(className);
                if (forName != null) {
                    return forName;
                }
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return super.getUIClass(event);
    }
}

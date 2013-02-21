/**
 * Copyright 2009-2013 Oy Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

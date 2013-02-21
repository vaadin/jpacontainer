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
package com.vaadin.addon.jpacontainer.itest.lazyhibernate;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

public class LazyHibernate extends UI {
    static {
        LazyHibernateDataGenerator.create();
    }

    // @Override
    // public void onRequestStart(HttpServletRequest request,
    // HttpServletResponse response) {
    // System.out.println("Start");
    // emprHelper.requestStart();
    // }
    //
    // @Override
    // public void onRequestEnd(HttpServletRequest request,
    // HttpServletResponse response) {
    // System.out.println("End");
    // emprHelper.requestEnd();
    // }

    @Override
    protected void init(VaadinRequest request) {
        setContent(new LazyHibernateMainView());
    }
}

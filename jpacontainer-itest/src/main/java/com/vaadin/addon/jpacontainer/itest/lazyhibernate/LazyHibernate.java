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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.addon.jpacontainer.util.EntityManagerPerRequestHelper;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Window;

public class LazyHibernate extends Window implements HttpServletRequestListener {
    EntityManagerPerRequestHelper emprHelper;

    static {
        LazyHibernateDataGenerator.create();
    }

    public LazyHibernate() {
        emprHelper = new EntityManagerPerRequestHelper();
    }

    @Override
    public void attach() {
        super.attach();
        setContent(new LazyHibernateMainView(this));
    }

    @Override
    public void onRequestStart(HttpServletRequest request,
            HttpServletResponse response) {
        System.out.println("Start");
        emprHelper.requestStart();
    }

    @Override
    public void onRequestEnd(HttpServletRequest request,
            HttpServletResponse response) {
        System.out.println("End");
        emprHelper.requestEnd();
    }
}

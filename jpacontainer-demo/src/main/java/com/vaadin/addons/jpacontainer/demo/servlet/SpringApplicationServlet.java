/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.demo.servlet;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Custom servlet that fetches the application instance from the Spring
 * application context.
 * <p>
 * This class is based on the <a href="http://dev.vaadin.com/browser/incubator/SpringApplication">SpringApplication example</a>
 * by Petri Hakala.

 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class SpringApplicationServlet extends AbstractApplicationServlet {

    // TODO Add logging and maybe some additional comments

    private WebApplicationContext applicationContext;
    private Class<? extends Application> applicationClass;
    private String applicationBean;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        applicationBean = servletConfig.getInitParameter("applicationBean");
        if (applicationBean == null) {
            throw new ServletException(
                    "ApplicationBean not specified in servlet parameters");
        }
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(
                servletConfig.getServletContext());
        applicationClass = (Class<? extends Application>) applicationContext.
                getType(applicationBean);
    }

    @Override
    protected Class<? extends Application> getApplicationClass() throws
            ClassNotFoundException {
        return applicationClass;
    }

    @Override
    protected Application getNewApplication(HttpServletRequest request) {
        return (Application) applicationContext.getBean(applicationBean);
    }
}

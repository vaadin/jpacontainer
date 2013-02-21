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
/*
JPAContainer
Copyright (C) 2009-2011 Oy Vaadin Ltd

This program is available under GNU Affero General Public License (version
3 or later at your option).

See the file licensing.txt distributed with this software for more
information about licensing.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addon.jpacontainer.demo.servlet;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Custom servlet that fetches the application instance from the Spring
 * application context. It takes one servlet configuration parameter, <code>applicationBean</code>,
 * which should contain the name of the Vaadin application prototype bean defined in the
 * web application context.
 * <p>
 * This class is based on the <a href="http://dev.vaadin.com/browser/incubator/SpringApplication">SpringApplication example</a>
 * by Petri Hakala.
 *
 * @see WebApplicationContext
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class SpringApplicationServlet extends AbstractApplicationServlet {

    protected final Log logger = LogFactory.getLog(getClass());
    private WebApplicationContext applicationContext;
    private Class<? extends Application> applicationClass;
    private String applicationBean;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        /*
         * Look up the name of the Vaadin application prototype bean.
         */
        applicationBean = servletConfig.getInitParameter("applicationBean");
        if (applicationBean == null) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "ApplicationBean not specified in servlet parameters");
            }
            throw new ServletException(
                    "ApplicationBean not specified in servlet parameters");
        }

        if (logger.isInfoEnabled()) {
            logger.info("Found Vaadin ApplicationBean [" + applicationBean + "]");
        }
        /*
         * Fetch the Spring web application context
         */
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(
                servletConfig.getServletContext());

        if (!applicationContext.isPrototype(applicationBean)) {
            if (logger.isWarnEnabled()) {
                logger.warn(
                        "ApplicationBean not configured as a prototype");
            }
        }

        applicationClass = (Class<? extends Application>) applicationContext.
                getType(applicationBean);
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Vaadin application class is [" + applicationClass + "]");
        }
    }

    @Override
    protected Class<? extends Application> getApplicationClass() throws
            ClassNotFoundException {
        return applicationClass;
    }

    @Override
    protected Application getNewApplication(HttpServletRequest request) {
        /*
         * As the application bean should be defined as a prototype,
         * this call should always return a new application instance.
         */
        return (Application) applicationContext.getBean(applicationBean);
    }
}

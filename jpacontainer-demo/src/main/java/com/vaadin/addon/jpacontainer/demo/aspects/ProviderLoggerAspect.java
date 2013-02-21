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
package com.vaadin.addon.jpacontainer.demo.aspects;

import com.vaadin.addon.jpacontainer.provider.LocalEntityProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Aspect that logs all method calls to {@link LocalEntityProvider} using Apache Commons Logging.
 * Useful for debugging, but should not be included in production environments.
 *
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
@Aspect
public class ProviderLoggerAspect {

    protected final Log logger = LogFactory.getLog(getClass());

    @Around("methodsToBeLogged()")
    public Object log(ProceedingJoinPoint pjp) throws Throwable {
        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder(pjp.getSignature().getName());
            sb.append("(");
            if (pjp.getArgs().length > 0) {
                for (int i = 0; i < pjp.getArgs().length - 2; i++) {
                    sb.append(pjp.getArgs()[i]);
                    sb.append(",");
                }
                sb.append(pjp.getArgs()[pjp.getArgs().length - 1]);
            }
            sb.append(")");
            logger.debug("Calling method: " + sb.toString());
        }
        Object result = pjp.proceed();
        if (logger.isDebugEnabled()) {
            logger.debug("Result of method " + pjp.getSignature().getName() + ": " + result);
        }
        return result;
    }

    @Pointcut("execution(public * com.vaadin.addon.jpacontainer.provider.LocalEntityProvider.*(..))")
    public void methodsToBeLogged() {
    }
}

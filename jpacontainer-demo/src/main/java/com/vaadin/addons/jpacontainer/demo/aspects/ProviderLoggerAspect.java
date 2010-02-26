/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.demo.aspects;

import com.vaadin.addons.jpacontainer.provider.LocalEntityProvider;
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
 * @author Petter Holmström (IT Mill)
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

    @Pointcut("execution(public * com.vaadin.addons.jpacontainer.provider.LocalEntityProvider.*(..))")
    public void methodsToBeLogged() {
    }
}

/*
 * JPAContainer
 * Copyright (C) 2010-2011 Oy Vaadin Ltd
 *
 * This program is available both under Commercial Vaadin Add-On
 * License 2.0 (CVALv2) and under GNU Affero General Public License (version
 * 3 or later) at your option.
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and CVALv2 along with this program.  If not, see
 * <http://www.gnu.org/licenses/> and <http://vaadin.com/license/cval-2.0>.
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

    @Pointcut("execution(public * com.vaadin.addon.jpacontainer.provider.LocalEntityProvider.*(..))")
    public void methodsToBeLogged() {
    }
}

package com.vaadin.addon.jpacontainer.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility methods for finding Hibernate specific information about entities
 * without forcing a compile-time dependency on the Hibernate framework.
 */
public class HibernateUtil {
    private static final Logger logger = Logger.getLogger(HibernateUtil.class
            .getName());

    /**
     * Reflectively finds out if the passed in exception is a Hibernate
     * LazyInitializationException.
     * 
     * @param e
     * @return true if the exception is a Hibernate LazyInitializationException.
     */
    public static boolean isLazyInitializationException(RuntimeException e) {
        return "LazyInitializationException".equals(e.getClass()
                .getSimpleName());
    }

    /**
     * Reflectively finds out whether an object is an uninitialized Hibernate
     * proxy and unattached.
     * 
     * @param obj
     * @return true if the object is an uninitialized and unattached Hibernate
     *         proxy.
     */
    public static boolean isUninitializedAndUnattachedProxy(Object obj) {
        try {
            Class<?> hibernateProxyCls = Class
                    .forName("org.hibernate.proxy.HibernateProxy");
            boolean isAProxy = hibernateProxyCls.isInstance(obj);
            if (isAProxy) {
                Method lazyInitializerGetter = obj.getClass().getMethod(
                        "getHibernateLazyInitializer");
                Object lazyInitializer = lazyInitializerGetter.invoke(obj);
                Method isUninitialized = lazyInitializer.getClass().getMethod(
                        "isUninitialized");
                Method getSession = lazyInitializer.getClass().getMethod(
                        "getSession");
                Boolean uninited = (Boolean) isUninitialized
                        .invoke(lazyInitializer);
                boolean hasSession = getSession.invoke(lazyInitializer) == null;
                return uninited && hasSession;
            }
        } catch (ClassNotFoundException e) {
            // Hibernate is not in use.
            logger.log(Level.FINEST, "Hibernate not in use", e);
        } catch (SecurityException e) {
            // Should never happen, since the ClassNotFoundException would be
            // triggered first.
            logger.log(
                    Level.FINEST,
                    "Something happened when trying to figure out "
                            + "if an object is an uninitialized hibernate proxy. "
                            + "This shouldn't happen.", e);
        } catch (NoSuchMethodException e) {
            // Should never happen, since the ClassNotFoundException would be
            // triggered first.
            logger.log(
                    Level.FINEST,
                    "Something happened when trying to figure out "
                            + "if an object is an uninitialized hibernate proxy. "
                            + "This shouldn't happen.", e);
        } catch (IllegalArgumentException e) {
            // Should never happen, since the ClassNotFoundException would be
            // triggered first.
            logger.log(
                    Level.FINEST,
                    "Something happened when trying to figure out "
                            + "if an object is an uninitialized hibernate proxy. "
                            + "This shouldn't happen.", e);
        } catch (IllegalAccessException e) {
            // Should never happen, since the ClassNotFoundException would be
            // triggered first.
            logger.log(
                    Level.FINEST,
                    "Something happened when trying to figure out "
                            + "if an object is an uninitialized hibernate proxy. "
                            + "This shouldn't happen.", e);
        } catch (InvocationTargetException e) {
            // Should never happen, since the ClassNotFoundException would be
            // triggered first.
            logger.log(
                    Level.FINEST,
                    "Something happened when trying to figure out "
                            + "if an object is an uninitialized hibernate proxy. "
                            + "This shouldn't happen.", e);
        }
        return false;
    }
}

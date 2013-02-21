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
package com.vaadin.addon.jpacontainer.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.OneToMany;

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

    /**
     * Finds the property's "mappedBy" value.
     * 
     * @param entity
     *            the entity containing the property
     * @param propertyName
     *            the name of the property to find the "mappedBy" value for.
     * @return the value of mappedBy in an annotation.
     */
    public static String getMappedByProperty(Object entity, String propertyName) {
        Class<?> entityClass = findActualEntityClass(entity);
        OneToMany otm = getAnnotationForProperty(OneToMany.class, entityClass,
                propertyName);
        if (otm != null && !"".equals(otm.mappedBy())) {
            return otm.mappedBy();
        }
        // Fall back on convention
        return entityClass.getSimpleName().toLowerCase();
    }

    /**
     * Finds a given annotation on a property.
     * 
     * @param annotationType
     *            the annotation to find.
     * @param entityClass
     *            the class declaring the property
     * @param propertyName
     *            the name of the property for which to find the annotation.
     * @return the annotation
     */
    private static <A extends Annotation> A getAnnotationForProperty(
            Class<A> annotationType, Class<?> entityClass, String propertyName) {
        A annotation = getAnnotationFromPropertyGetter(annotationType,
                entityClass, propertyName);
        if (annotation == null) {
            annotation = getAnnotationFromField(annotationType, entityClass,
                    propertyName);
        }
        return annotation;
    }

    /**
     * Finds a given annotation on a property getter method.
     */
    private static <A extends Annotation> A getAnnotationFromPropertyGetter(
            Class<A> annotationType, Class<?> entityClass, String propertyName) {
        // TODO: support for private getters? -> need to recursively search
        // superclasses as well.
        Method getter = null;
        try {
            getter = entityClass.getMethod("get"
                    + propertyName.substring(0, 1).toUpperCase()
                    + propertyName.substring(1));
        } catch (Exception e) {
            // Try isXXX
            try {
                getter = entityClass.getMethod("is"
                        + propertyName.substring(0, 1).toUpperCase()
                        + propertyName.substring(1));
            } catch (Exception e1) {
                // No getter found.
            }
        }
        if (getter != null && getter.isAnnotationPresent(annotationType)) {
            return getter.getAnnotation(annotationType);
        }
        return null;
    }

    /**
     * Finds a given annotation on a field.
     */
    private static <A extends Annotation> A getAnnotationFromField(
            Class<A> annotationType, Class<?> entityClass, String propertyName) {
        Field field = null;
        try {
            // TODO: get fields from @mappedsuperclasses as well.
            field = entityClass.getDeclaredField(propertyName);
        } catch (Exception e) {
            // Field not found
        }

        if (field != null && field.isAnnotationPresent(annotationType)) {
            return field.getAnnotation(annotationType);
        }
        return null;
    }

    /**
     * Find the actual entity class of an entity object. If Hibernate is in use,
     * entity.getClass() might return an instance of HibernateProxy, which does
     * not contain any annotations.
     * 
     * @param entity
     *            the entity to get the real class for.
     * @return the real class for the entity.
     */
    private static Class<?> findActualEntityClass(Object entity) {
        Class<?> cls = entity.getClass();
        try {
            Class<?> hibernateProxyCls = Class
                    .forName("org.hibernate.proxy.HibernateProxy");
            if (hibernateProxyCls.isAssignableFrom(cls)) {
                Method getHibernateLazyInitializer = cls
                        .getMethod("getHibernateLazyInitializer");
                Object lazyInitializer = getHibernateLazyInitializer
                        .invoke(entity);
                Method getImplementation = lazyInitializer.getClass()
                        .getMethod("getImplementation");
                return getImplementation.invoke(lazyInitializer).getClass();
            }
        } catch (ClassNotFoundException e) {
            // Hibernate libraries not found
        } catch (SecurityException e) {
            // Don't worry, use entity.getClass()
        } catch (NoSuchMethodException e) {
            // Don't worry, use entity.getClass()
        } catch (IllegalArgumentException e) {
            // Don't worry, use entity.getClass()
        } catch (IllegalAccessException e) {
            // Don't worry, use entity.getClass()
        } catch (InvocationTargetException e) {
            // Don't worry, use entity.getClass()
        }
        return cls;
    }
}

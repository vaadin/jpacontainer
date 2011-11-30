/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.metadata;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.persistence.Embeddable;
import javax.persistence.Entity;

/**
 * This class provides a way of accessing the JPA mapping metadata of
 * {@link Entity} and {@link Embeddable} classes. This information may be used
 * to construct queries or decide whether a property is sortable or not.
 * 
 * @see EntityClassMetadata
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class ClassMetadata<T> implements Serializable {

    private static final long serialVersionUID = 2569781449737488799L;
    private final Class<T> mappedClass;
    private final Map<String, PropertyMetadata> allProperties = new LinkedHashMap<String, PropertyMetadata>();
    private final Map<String, PersistentPropertyMetadata> persistentProperties = new LinkedHashMap<String, PersistentPropertyMetadata>();

    /**
     * Constructs a new <code>ClassMetadata</code> instance. Properties can be
     * added using the
     * {@link #addProperties(com.vaadin.addons.jpacontainer.metadata.PropertyMetadata[]) }
     * method.
     * 
     * @param mappedClass
     *            the mapped class (must not be null).
     */
    ClassMetadata(Class<T> mappedClass) {
        assert mappedClass != null : "mappedClass must not be null";
        this.mappedClass = mappedClass;
    }

    /**
     * Adds the specified property metadata to the class. Any existing
     * properties with the same names will be overwritten.
     * 
     * @param properties
     *            an array of properties to add.
     */
    final void addProperties(PropertyMetadata... properties) {
        assert properties != null : "properties must not be null";
        for (PropertyMetadata pm : properties) {
            allProperties.put(pm.getName(), pm);
            if (pm instanceof PersistentPropertyMetadata) {
                persistentProperties.put(pm.getName(),
                        (PersistentPropertyMetadata) pm);
            } else {
                // If we have a previous property and want to overwrite
                // it with another that is not persistent
                persistentProperties.remove(pm.getName());
            }
        }
    }

    /**
     * Gets the mapped class.
     * 
     * @return the class (never null).
     */
    public Class<T> getMappedClass() {
        return mappedClass;
    }

    /**
     * Gets all the persistent properties of the class.
     * 
     * @return an unmodifiable collection of property metadata.
     */
    public Collection<PersistentPropertyMetadata> getPersistentProperties() {
        return Collections
                .unmodifiableCollection(persistentProperties.values());
    }

    /**
     * Gets the names of all persistent properties of this class.
     * 
     * @see #getPersistentProperties()
     * @return an unmodifiable collection of property names.
     */
    public Collection<String> getPersistentPropertyNames() {
        return Collections
                .unmodifiableCollection(persistentProperties.keySet());
    }

    /**
     * Gets all the properties of the class. In addition to the persistent
     * properties, all public JavaBean properties are also included (even those
     * who do not have setter methods).
     * 
     * @return an unmodifiable collection of property metadata.
     */
    public Collection<PropertyMetadata> getProperties() {
        return Collections.unmodifiableCollection(allProperties.values());
    }

    /**
     * Gets the names of all the properties of this class.
     * 
     * @see #getProperties()
     * @return an unmodifiable collection of property names.
     */
    public Collection<String> getPropertyNames() {
        return Collections.unmodifiableCollection(allProperties.keySet());
    }

    /**
     * Gets the metadata of the named property.
     * 
     * @param propertyName
     *            the name of the property (must not be null).
     * @return the property metadata, or null if not found.
     */
    public PropertyMetadata getProperty(String propertyName) {
        return allProperties.get(propertyName);
    }

    /**
     * Gets the value of <code>property</code> from <code>object</code>.
     * 
     * @param object
     *            the object from which the property will be retrieved (must not
     *            be null).
     * @param property
     *            the metadata of the property (must not be null).
     * @return the property value.
     * @throws IllegalArgumentException
     *             if the property could not be retrieved.
     */
    protected Object getPropertyValue(T object, PropertyMetadata property)
            throws IllegalArgumentException {
        assert object != null : "object must not be null";
        assert property != null : "property must not be null";
        try {
            if (property instanceof PersistentPropertyMetadata) {
                PersistentPropertyMetadata ppmd = (PersistentPropertyMetadata) property;
                if (ppmd.field != null) {
                    return getPropertyValueFromField(object, ppmd);
                }
            }
            return property.getter.invoke(object);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(
                    "Cannot access the property value", e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(
                    "Cannot access the property value", e);
        }
    }

    private Object getPropertyValueFromField(T object,
            PersistentPropertyMetadata ppmd) throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        // First we try to find a getter for the field in order to
        // make getter-based lazy loading work.
        Class clazz = ppmd.field.getDeclaringClass();
        Method getter = null;
        try {
            getter = clazz.getMethod("get" + capitalize(ppmd.fieldName));
        } catch (Exception e) {
            try {
                getter = clazz.getMethod("is" + capitalize(ppmd.fieldName));
            } catch (Exception e1) {
            }
        }
        if (getter == null) {
            try {
                ppmd.field.setAccessible(true);
                return ppmd.field.get(object);
            } finally {
                ppmd.field.setAccessible(false);
            }
        }
        return getter.invoke(object);
    }

    private String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    /**
     * Sets the value of <code>property</code> to <code>value</code> on
     * <code>object</code>.
     * 
     * @param object
     *            the object to which the property will be set (must not be
     *            null).
     * @param property
     *            the metadata of the property (must not be null).
     * @param value
     *            the property value.
     * @throws IllegalArgumentException
     *             if the property could not be set.
     */
    protected void setPropertyValue(T object, PropertyMetadata property,
            Object value) throws IllegalArgumentException {
        assert object != null : "object must not be null";
        assert property != null : "property must not be null";
        if (property != null && property.isWritable()) {
            try {
                if (property instanceof PersistentPropertyMetadata) {
                    PersistentPropertyMetadata ppmd = (PersistentPropertyMetadata) property;
                    if (ppmd.field != null) {
                        try {
                            ppmd.field.setAccessible(true);
                            ppmd.field.set(object, value);
                            return;
                        } finally {
                            ppmd.field.setAccessible(false);
                        }
                    }
                }
                property.setter.invoke(object, value);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(
                        "Cannot set the property value", e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException(
                        "Cannot set the property value", e);
            }
        } else {
            throw new IllegalArgumentException("No such writable property: "
                    + property.getName());
        }
    }

    /**
     * Gets the getter method for <code>propertyName</code> from
     * <code>parent</code>.
     * 
     * @param propertyName
     *            the JavaBean property name (must not be null).
     * @param parent
     *            the class from which to get the getter method (must not be
     *            null).
     * @return the getter method, or null if not found.
     */
    protected Method getGetterMethod(String propertyName, Class<?> parent) {
        String methodName = "get" + propertyName.substring(0, 1).toUpperCase()
                + propertyName.substring(1);
        try {
            Method m = parent.getMethod(methodName);
            if (m.getReturnType() != Void.TYPE) {
                return m;
            } else {
                return null;
            }
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Gets the setter method for <code>propertyName</code> from
     * <code>parent</code>.
     * 
     * @param propertyName
     *            the JavaBean property name (must not be null).
     * @param parent
     *            the class from which to get the setter method (must not be
     *            null).
     * @param propertyType
     *            the type of the property (must not be null).
     * @return the setter method, or null if not found.
     */
    protected Method getSetterMethod(String propertyName, Class<?> parent,
            Class<?> propertyType) {
        String methodName = "set" + propertyName.substring(0, 1).toUpperCase()
                + propertyName.substring(1);
        try {
            Method m = parent.getMethod(methodName, propertyType);
            if (m.getReturnType() == Void.TYPE) {
                return m;
            } else {
                return null;
            }
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Gets the value of <code>object.propertyName</code>. The property name may
     * be nested.
     * 
     * @param object
     *            the entity object from which the property value should be
     *            fetched (must not be null).
     * @param propertyName
     *            the name of the property (must not be null).
     * @return the property value.
     * @throws IllegalArgumentException
     *             if the property value could not be fetched, e.g. due to
     *             <code>propertyName</code> being invalid.
     */
    @SuppressWarnings("unchecked")
    public Object getPropertyValue(T object, String propertyName)
            throws IllegalArgumentException {
        assert object != null : "object must not be null";
        assert propertyName != null : "propertyName must not be null";

        StringTokenizer st = new StringTokenizer(propertyName, ".");
        ClassMetadata<Object> typeMetadata = (ClassMetadata<Object>) this;
        Class<?> type = null;
        Object currentObject = object;
        while (st.hasMoreTokens()) {
            String propName = st.nextToken();
            if (typeMetadata != null) {
                PropertyMetadata pmd = typeMetadata.getProperty(propName);
                if (pmd == null) {
                    throw new IllegalArgumentException("Invalid property name");
                }
                currentObject = typeMetadata.getPropertyValue(currentObject,
                        pmd);
                if (currentObject == null) {
                    return null;
                }
                if (pmd instanceof PersistentPropertyMetadata) {
                    typeMetadata = (ClassMetadata<Object>) ((PersistentPropertyMetadata) pmd)
                            .getTypeMetadata();
                } else {
                    typeMetadata = null;
                }
                if (typeMetadata == null) {
                    type = pmd.getType();
                } else {
                    type = null;
                }
            } else if (type != null) {
                Method getter = getGetterMethod(propName, type);
                if (getter == null) {
                    throw new IllegalArgumentException("Invalid property name");
                }
                try {
                    currentObject = getter.invoke(currentObject);
                    if (currentObject == null) {
                        return null;
                    }
                    type = getter.getReturnType();
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                            "Could not access a nested property", e);
                }
            }
        }
        return currentObject;
    }

    /**
     * Sets the value of <code>object.propertyName</code> to <code>value</code>.
     * The property name may be nested.
     * 
     * @param object
     *            the object whose property should be set (must not be null).
     * @param propertyName
     *            the name of the property to set (must not be null).
     * @param value
     *            the value to set.
     * @throws IllegalArgumentException
     *             if the value could not be set, e.g. due to
     *             <code>propertyName</code> being invalid or the property being
     *             read only.
     * @throws IllegalStateException
     *             if a nested property name is used and one of the nested
     *             properties (other than the last one) is null.
     */
    @SuppressWarnings("unchecked")
    public void setPropertyValue(T object, String propertyName, Object value)
            throws IllegalArgumentException, IllegalStateException {
        assert object != null : "object must not be null";
        assert propertyName != null : "propertyName must not be null";

        StringTokenizer st = new StringTokenizer(propertyName, ".");
        ClassMetadata<Object> typeMetadata = (ClassMetadata<Object>) this;
        Class<?> type = null;
        Object currentObject = object;
        while (st.hasMoreTokens()) {
            String propName = st.nextToken();
            if (typeMetadata != null) {
                PropertyMetadata pmd = typeMetadata.getProperty(propName);
                if (pmd == null) {
                    throw new IllegalArgumentException("Invalid property name");
                }
                if (!st.hasMoreTokens()) {
                    // We have reached the end of the chain
                    typeMetadata.setPropertyValue(currentObject, pmd, value);
                } else {
                    currentObject = typeMetadata.getPropertyValue(
                            currentObject, pmd);
                    if (currentObject == null) {
                        throw new IllegalStateException(
                                "A null value was found in the chain of nested properties");
                    }
                    if (pmd instanceof PersistentPropertyMetadata) {
                        typeMetadata = (ClassMetadata<Object>) ((PersistentPropertyMetadata) pmd)
                                .getTypeMetadata();
                    } else {
                        typeMetadata = null;
                    }
                    if (typeMetadata == null) {
                        type = pmd.getType();
                    } else {
                        type = null;
                    }
                }
            } else if (type != null) {
                Method getter = getGetterMethod(propName, type);
                if (getter == null) {
                    throw new IllegalArgumentException("Invalid property name");
                }
                if (!st.hasMoreTokens()) {
                    // We have reached the end of the chain
                    Method setter = getSetterMethod(propName, type,
                            getter.getReturnType());
                    if (setter == null) {
                        throw new IllegalArgumentException(
                                "Property is read only");
                    }
                    try {
                        setter.invoke(currentObject, value);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(
                                "Could not set the value");
                    }
                } else {
                    try {
                        currentObject = getter.invoke(currentObject);
                        if (currentObject == null) {
                            throw new IllegalStateException(
                                    "A null value was found in the chain of nested properties");
                        }
                        type = getter.getReturnType();
                    } catch (Exception e) {
                        throw new IllegalArgumentException(
                                "Could not access a nested property", e);
                    }
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == getClass()) {
            ClassMetadata<?> other = (ClassMetadata<?>) obj;
            return mappedClass.equals(other.mappedClass)
                    && allProperties.equals(other.allProperties)
                    && persistentProperties.equals(other.persistentProperties);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + mappedClass.hashCode();
        hash = hash * 31 + allProperties.hashCode();
        hash = hash * 31 + persistentProperties.hashCode();
        return hash;
    }
}

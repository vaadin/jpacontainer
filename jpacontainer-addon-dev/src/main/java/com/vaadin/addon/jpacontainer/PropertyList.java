/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer;

import java.beans.Introspector;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vaadin.addon.jpacontainer.metadata.ClassMetadata;
import com.vaadin.addon.jpacontainer.metadata.PersistentPropertyMetadata;
import com.vaadin.addon.jpacontainer.metadata.PropertyKind;
import com.vaadin.addon.jpacontainer.metadata.PropertyMetadata;

/**
 * Helper class to make it easier to work with nested properties. Intended to be
 * used by {@link JPAContainer}. This class is not part of the public API and
 * hence should not be used directly by client applications.
 * <p>
 * Property lists can be chained. A child property list will always include all
 * the properties of its parent in addition to its own. A child list cannot be
 * used to add or remove properties to/from its parent.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
final class PropertyList<T> implements Serializable {

    private static final long serialVersionUID = 372287057799712177L;
    private ClassMetadata<T> metadata;
    private Set<String> propertyNames = new HashSet<String>();
    private Set<String> persistentPropertyNames = new HashSet<String>();
    // map from property name to the name of the property to be used to sort by
    // that property (in a format usable in JPQL - e.g. address.street)
    private Map<String, String> sortablePropertyMap = new HashMap<String, String>();
    private Set<String> nestedPropertyNames = new HashSet<String>();
    private Set<String> allPropertyNames = new HashSet<String>();

    /**
     * Creates a new <code>PropertyList</code> for the specified metadata.
     * Initially, all the properties of <code>metadata</code> will be added to
     * the list.
     * 
     * @param metadata
     *            the class metadata (must not be null).
     */
    public PropertyList(ClassMetadata<T> metadata) {
        assert metadata != null : "metadata must not be null";
        this.metadata = metadata;
        for (PropertyMetadata pm : metadata.getProperties()) {
            propertyNames.add(pm.getName());
            allPropertyNames.add(pm.getName());
            if (pm instanceof PersistentPropertyMetadata) {
                persistentPropertyNames.add(pm.getName());
                if (PropertyKind.SIMPLE
                        .equals(((PersistentPropertyMetadata) pm)
                                .getPropertyKind())) {
                    sortablePropertyMap.put(pm.getName(), pm.getName());
                }
            }
        }
    }

    private PropertyList<T> parentList;

    /**
     * Creates a new <code>PropertyList</code> with the specified parent list.
     * Initially, all the properties of the parent list will be available.
     * 
     * @param parentList
     *            the parent list (must not be null).
     */
    public PropertyList(PropertyList<T> parentList) {
        assert parentList != null : "parentList must not be null";
        this.parentList = parentList;
        this.metadata = parentList.getClassMetadata();
    }

    /**
     * Gets the metadata for the class from which the properties should be
     * fetched.
     * 
     * @return the class metadata (never null).
     */
    public ClassMetadata<T> getClassMetadata() {
        return metadata;
    }

    /**
     * Gets the parent property list, if any.
     * 
     * @return the parent list, or null if the list has no parent.
     */
    public PropertyList<T> getParentList() {
        return parentList;
    }

    /**
     * Configures a property to be sortable based on another property, typically
     * a sub-property.
     * <p>
     * For example, let's say there is a property named <code>address</code> and
     * that this property's type in turn has the property <code>street</code>.
     * Addresses are not directly sortable as they are not simple properties.
     * <p>
     * If we want to be able to sort addresses based on the street property, we
     * can set the sort property for <code>address</code> to be
     * <code>address.street</code> using this method. Sort properties must be
     * persistent and usable in JPQL, but need not be registered as separate
     * properties in the PropertyList.
     * <p>
     * Note that the sort property is not checked when this method is called. If
     * it is not a valid sort property, an exception will be thrown when trying
     * to sort a container.
     * 
     * @param propertyName
     *            the property for which sorting is to be customized (must not
     *            be null).
     * @param sortPropertyName
     *            the property based on which sorting should be performed - this
     *            need not be a separate property in the container but needs to
     *            be usable in JPQL
     * @throws IllegalArgumentException
     *             if <code>propertyName</code> does not refer to a persistent
     *             property.
     * @since 1.2.1
     */
    public void setSortProperty(String propertyName, String sortPropertyName)
            throws IllegalArgumentException {
        if (persistentPropertyNames.contains(propertyName)) {
            sortablePropertyMap.put(propertyName, sortPropertyName);
        } else {
            throw new IllegalArgumentException("Property " + propertyName
                    + " cannot be sorted based on " + sortPropertyName
                    + ": not a persistent property");
        }
    }

    /**
     * Adds the nested property <code>propertyName</code> to the set of
     * properties. An asterisk can be used as a wildcard to indicate all
     * leaf-properties.
     * <p>
     * For example, let's say there is a property named <code>address</code> and
     * that this property's type in turn has the properties <code>street</code>,
     * <code>postalCode</code> and <code>city</code>.
     * <p>
     * If we want to be able to access the street property directly, we can add
     * the nested property <code>address.street</code> using this method. The
     * method will figure out whether the nested property is persistent (can be
     * used in queries) or transient (can only be used to display data).
     * <p>
     * However, if we want to add all the address properties, we can also use
     * <code>address.*</code>. This will cause the nested properties
     * <code>address.street</code>, <code>address.postalCode</code> and
     * <code>address.city</code> to be added to the set of properties.
     * 
     * @param propertyName
     *            the nested property to add (must not be null).
     * @throws IllegalArgumentException
     *             if <code>propertyName</code> was invalid.
     */
    public void addNestedProperty(String propertyName)
            throws IllegalArgumentException {
        assert propertyName != null : "propertyName must not be null";

        if (propertyName.indexOf('.') == -1) {
            throw new IllegalArgumentException(propertyName + " is not nested");
        }

        if (getAllAvailablePropertyNames().contains(propertyName)) {
            return; // Do nothing, the property already exists.
        }

        if (propertyName.endsWith("*")) {
            // We add a whole bunch of properties
            String parentPropertyName = propertyName.substring(0,
                    propertyName.length() - 2);
            NestedProperty parentProperty = getNestedProperty(parentPropertyName);
            if (parentProperty.getTypeMetadata() != null) {
                // The parent property is persistent and contains metadata
                for (PropertyMetadata pm : parentProperty.getTypeMetadata()
                        .getProperties()) {
                    String newName = parentPropertyName + "." + pm.getName();
                    if (!getAllAvailablePropertyNames().contains(newName)) {
                        if (pm instanceof PersistentPropertyMetadata) {
                            persistentPropertyNames.add(newName);
                            if (PropertyKind.SIMPLE
                                    .equals(((PersistentPropertyMetadata) pm)
                                            .getPropertyKind())) {
                                sortablePropertyMap.put(newName, newName);
                            }
                        }
                        propertyNames.add(newName);
                        allPropertyNames.add(newName);
                        nestedPropertyNames.add(newName);
                    }
                }
            } else {
                // The parent property is transient or is a simple property that
                // does not contain any nestable properties
                for (Method m : parentProperty.getType().getMethods()) {
                    if (m.getName().startsWith("get")
                            && !Modifier.isStatic(m.getModifiers())
                            && m.getReturnType() != Void.TYPE
                            && m.getDeclaringClass() != Object.class) {
                        String newName = parentPropertyName
                                + "."
                                + Introspector.decapitalize(m.getName()
                                        .substring(3));
                        if (!getAllAvailablePropertyNames().contains(newName)) {
                            propertyNames.add(newName);
                            nestedPropertyNames.add(newName);
                            allPropertyNames.add(newName);
                        }
                    }
                }
            }
        } else {
            // We add a single property
            NestedProperty np = getNestedProperty(propertyName);
            if (np.getKind() == NestedPropertyKind.PERSISTENT) {
                persistentPropertyNames.add(propertyName);

                PropertyMetadata propertyMetadata = np.getPropertyMetadata();
                if (propertyMetadata instanceof PersistentPropertyMetadata
                        && PropertyKind.SIMPLE
                                .equals(((PersistentPropertyMetadata) propertyMetadata)
                                        .getPropertyKind())) {
                    sortablePropertyMap.put(propertyName, propertyName);
                }
            }
            // Transient property
            propertyNames.add(propertyName);
            nestedPropertyNames.add(propertyName);
            allPropertyNames.add(propertyName);
        }
    }

    /*
     * TODO The current way of handling nested properties was designed to also
     * support getting and setting values of nested properties. However, this
     * responsibility was later moved to ClassMetadata. Therefore, this design
     * may be more complex than would actually be required. In a future version
     * it should be cleaned up.
     */
    private static enum NestedPropertyKind {

        PERSISTENT, TRANSIENT
    }

    private static class NestedProperty implements Serializable {

        private static final long serialVersionUID = -430502035392444897L;
        final NestedProperty parent;
        private final String name;
        final ClassMetadata<? extends Object> parentClassMetadata;
        final Method propertyGetterMethod;

        NestedProperty(String name,
                ClassMetadata<? extends Object> parentClassMetadata) {
            this.name = name;
            this.parentClassMetadata = parentClassMetadata;
            this.parent = null;
            this.propertyGetterMethod = null;
        }

        /*
         * NestedProperty(String name, Method propertyGetterMethod) { this.name
         * = name; this.parentClassMetadata = null; this.parent = null;
         * this.propertyGetterMethod = propertyGetterMethod; }
         */

        NestedProperty(String name,
                ClassMetadata<? extends Object> parentClassMetadata,
                NestedProperty parent) {
            this.name = name;
            this.parentClassMetadata = parentClassMetadata;
            this.parent = parent;
            this.propertyGetterMethod = null;
        }

        NestedProperty(String name, Method propertyGetterMethod,
                NestedProperty parent) {
            this.name = name;
            this.parentClassMetadata = null;
            this.parent = parent;
            this.propertyGetterMethod = propertyGetterMethod;
        }

        String getName() {
            if (parent == null) {
                return name;
            } else {
                return parent.getName() + "." + name;
            }
        }

        Class<?> getType() {
            if (parentClassMetadata != null) {
                return parentClassMetadata.getProperty(name).getType();
            } else {
                return propertyGetterMethod.getReturnType();
            }
        }

        PropertyMetadata getPropertyMetadata() {
            if (parentClassMetadata != null) {
                return parentClassMetadata.getProperty(name);
            }
            return null;
        }

        ClassMetadata<?> getTypeMetadata() {
            PropertyMetadata pm = getPropertyMetadata();
            if (pm instanceof PersistentPropertyMetadata) {
                return ((PersistentPropertyMetadata) pm).getTypeMetadata();
            }
            return null;
        }

        NestedPropertyKind getKind() {
            if (parentClassMetadata != null
                    && parentClassMetadata.getProperty(name) instanceof PersistentPropertyMetadata) {
                return NestedPropertyKind.PERSISTENT;
            } else {
                return NestedPropertyKind.TRANSIENT;
            }
        }

        boolean isWritable() {
            if (parentClassMetadata != null) {
                return parentClassMetadata.getProperty(name).isWritable();
            } else {
                /*
                 * There are cases when this may not work. For example, if the
                 * setter is declared in a subclass.
                 */
                try {
                    propertyGetterMethod.getDeclaringClass().getMethod(
                            "s" + propertyGetterMethod.getName().substring(1),
                            getType());
                    return true;
                } catch (NoSuchMethodException e) {
                    return false;
                }
            }
        }
    }

    private Map<String, NestedProperty> nestedPropertyMap = new HashMap<String, NestedProperty>();

    private NestedProperty getNestedProperty(String propertyName)
            throws IllegalArgumentException {
        if (nestedPropertyMap.containsKey(propertyName)) {
            return nestedPropertyMap.get(propertyName);
        } else {
            try {
                if (propertyName.indexOf('.') != -1) {
                    // Try with the parent
                    int offset = propertyName.lastIndexOf('.');
                    String parentName = propertyName.substring(0, offset);
                    String name = propertyName.substring(offset + 1);
                    NestedProperty parentProperty = getNestedProperty(parentName);
                    NestedProperty property;
                    if (parentProperty.getTypeMetadata() != null) {
                        PropertyMetadata pm = parentProperty.getTypeMetadata()
                                .getProperty(name);
                        if (pm == null) {
                            throw new IllegalArgumentException(
                                    "Invalid property name");
                        } else {
                            property = new NestedProperty(pm.getName(),
                                    parentProperty.getTypeMetadata(),
                                    parentProperty);
                        }
                    } else {
                        Method getter = getGetterMethod(name,
                                parentProperty.getType());
                        if (getter == null) {
                            throw new IllegalArgumentException(
                                    "Invalid property name");
                        } else {
                            property = new NestedProperty(name, getter,
                                    parentProperty);
                        }
                    }
                    nestedPropertyMap.put(propertyName, property);
                    return property;
                } else {
                    // There are no more parent properties
                    PropertyMetadata pm = metadata.getProperty(propertyName);
                    if (pm == null) {
                        throw new IllegalArgumentException(
                                "Invalid property name");
                    } else {
                        NestedProperty property = new NestedProperty(
                                pm.getName(), metadata);
                        nestedPropertyMap.put(propertyName, property);
                        return property;
                    }
                }
            } catch (IllegalArgumentException e) {
                if (parentList == null) {
                    throw e;
                } else {
                    return parentList.getNestedProperty(propertyName);
                }
            }
        }
    }

    private Method getGetterMethod(String prop, Class<?> parent) {
        String propertyName = prop.substring(0, 1).toUpperCase()
                + prop.substring(1);
        try {
            Method m = parent.getMethod("get" + propertyName);
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
     * Removes <code>propertyName</code> from the set of properties. If the
     * property is contained in the parent list, nothing happens.
     * 
     * @param propertyName
     *            the property name to remove, must not be null.
     * @return true if a property was removed, false if not (i.e. it did not
     *         exist in the first place).
     */
    public boolean removeProperty(String propertyName) {
        assert propertyName != null : "propertyName must not be null";
        boolean result = propertyNames.remove(propertyName);
        persistentPropertyNames.remove(propertyName);
        sortablePropertyMap.remove(propertyName);
        if (nestedPropertyNames.remove(propertyName)) {
            allPropertyNames.remove(propertyName);
        }
        // Do not remove from map of nested properties in case the property
        // is referenced by other nested properties.
        return result;
    }

    /**
     * Gets the set of all available property names, i.e. the union of
     * {@link ClassMetadata#getPropertyNames() } and
     * {@link #getNestedPropertyNames() }. Only nested property names can be
     * added to or removed from this set.
     * 
     * @return an unmodifiable set of property names (never null).
     */
    public Set<String> getAllAvailablePropertyNames() {
        return Collections.unmodifiableSet(doGetAllAvailablePropertyNames());
    }

    private <E> Set<E> union(Set<E>... sets) {
        HashSet<E> newSet = new HashSet<E>();
        for (Set<E> s : sets) {
            newSet.addAll(s);
        }
        return newSet;
    }

    private <K, V> Map<K, V> union(Map<K, V>... maps) {
        HashMap<K, V> newMap = new HashMap<K, V>();
        for (Map<K, V> s : maps) {
            newMap.putAll(s);
        }
        return newMap;
    }

    @SuppressWarnings("unchecked")
    protected Set<String> doGetAllAvailablePropertyNames() {
        if (parentList == null) {
            return allPropertyNames;
        } else {
            return union(allPropertyNames,
                    parentList.doGetAllAvailablePropertyNames());
        }
    }

    /**
     * Gets the set of all property names. If no properties have been explicitly
     * removed using {@link #removeProperty(java.lang.String) }, this set is
     * equal to {@link #getAllAvailablePropertyNames() }. Otherwise, this set is
     * a subset of {@link #getAllAvailablePropertyNames()}.
     * 
     * @return an unmodifiable set of property names (never null).
     */
    public Set<String> getPropertyNames() {
        return Collections.unmodifiableSet(doGetPropertyNames());
    }

    @SuppressWarnings("unchecked")
    protected Set<String> doGetPropertyNames() {
        if (parentList == null) {
            return propertyNames;
        } else {
            return union(propertyNames, parentList.doGetPropertyNames());
        }
    }

    /**
     * Gets the set of persistent property names. This set is a subset of
     * {@link #getPropertyNames() }.
     * 
     * @return an unmodifiable set of property names (never null).
     */
    public Set<String> getPersistentPropertyNames() {
        return Collections.unmodifiableSet(doGetPersistentPropertyNames());
    }

    @SuppressWarnings("unchecked")
    protected Set<String> doGetPersistentPropertyNames() {
        if (parentList == null) {
            return persistentPropertyNames;
        } else {
            return union(persistentPropertyNames,
                    parentList.doGetPersistentPropertyNames());
        }
    }

    /**
     * Gets the map of all sortable property names and their corresponding sort
     * properties. The keys of this map also show up in
     * {@link #getPropertyNames() } and {@link #getPersistentPropertyNames() }.
     * 
     * @return an unmodifiable map from property names (never null) to sort
     *         properties (not necessarily in the list).
     */
    public Map<String, String> getSortablePropertyMap() {
        return Collections.unmodifiableMap(doGetSortablePropertyMap());
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> doGetSortablePropertyMap() {
        if (parentList == null) {
            return sortablePropertyMap;
        } else {
            return union(sortablePropertyMap,
                    parentList.doGetSortablePropertyMap());
        }
    }

    /**
     * Gets the set of all nested property names. These names also show up in
     * {@link #getPropertyNames() } and {@link #getPersistentPropertyNames() }.
     * 
     * @return an unmodifiable set of property names (never null).
     */
    public Set<String> getNestedPropertyNames() {
        return Collections.unmodifiableSet(doGetNestedPropertyNames());
    }

    @SuppressWarnings("unchecked")
    protected Set<String> doGetNestedPropertyNames() {
        if (parentList == null) {
            return nestedPropertyNames;
        } else {
            return union(nestedPropertyNames,
                    parentList.doGetNestedPropertyNames());
        }
    }

    /**
     * Gets the type of <code>propertyName</code>. Nested properties are
     * supported. This method works with property names in the
     * {@link #getAllAvailablePropertyNames() } set.
     * 
     * @param propertyName
     *            the name of the property (must not be null).
     * @return the type of the property (never null).
     * @throws IllegalArgumentException
     *             if <code>propertyName</code> is illegal.
     */
    public Class<?> getPropertyType(String propertyName)
            throws IllegalArgumentException {
        assert propertyName != null : "propertyName must not be null";
        if (!getAllAvailablePropertyNames().contains(propertyName)) {
            throw new IllegalArgumentException("Illegal property name: "
                    + propertyName);
        }
        if (propertyName.indexOf('.') != -1) {
            return getNestedProperty(propertyName).getType();
        } else {
            return metadata.getProperty(propertyName).getType();
        }
    }

    /**
     * Checks if <code>propertyName</code> is writable. Nested properties are
     * supported. This method works with property names in the
     * {@link #getAllAvailablePropertyNames() } set.
     * 
     * @param propertyName
     *            the name of the property (must not be null).
     * @return true if the property is writable, false otherwise.
     * @throws IllegalArgumentException
     *             if <code>propertyName</code> is illegal.
     */
    public boolean isPropertyWritable(String propertyName)
            throws IllegalArgumentException {
        assert propertyName != null : "propertyName must not be null";
        if (!getAllAvailablePropertyNames().contains(propertyName)) {
            throw new IllegalArgumentException("Illegal property name: "
                    + propertyName);
        }
        if (propertyName.indexOf('.') != -1) {
            return getNestedProperty(propertyName).isWritable();
        } else {
            return metadata.getProperty(propertyName).isWritable();
        }
    }

    /**
     * Gets the value of <code>propertyName</code> from the instance
     * <code>object</code>. The property name may be nested, but must be in the
     * {@link #getAllAvailablePropertyNames() } set.
     * <p>
     * When using nested properties and one of the properties in the chain is
     * null, this method will return null without throwing any exceptions.
     * 
     * @param object
     *            the object that the property value is fetched from (must not
     *            be null).
     * @param propertyName
     *            the property name (must not be null).
     * @return the property value.
     * @throws IllegalArgumentException
     *             if the property name was illegal.
     */
    public Object getPropertyValue(T object, String propertyName)
            throws IllegalArgumentException {
        assert propertyName != null : "propertyName must not be null";
        assert object != null : "object must not be null";
        if (!getAllAvailablePropertyNames().contains(propertyName)) {
            throw new IllegalArgumentException("Illegal property name: "
                    + propertyName);
        }
        return metadata.getPropertyValue(object, propertyName);
    }

    /**
     * Sets the value of <code>propertyName</code> to <code>propertyValue</code>
     * . The property name may be nested, but must be in the
     * {@link #getAllAvailablePropertyNames() } set.
     * 
     * @param object
     *            the object to which the property is set (must not be null).
     * @param propertyName
     *            the property name (must not be null).
     * @param propertyValue
     *            the property value to set.
     * @throws IllegalArgumentException
     *             if the property name was illegal.
     * @throws IllegalStateException
     *             if one of the properties in the chain of nested properties
     *             was null.
     */
    public void setPropertyValue(T object, String propertyName,
            Object propertyValue) throws IllegalArgumentException,
            IllegalStateException {
        assert propertyName != null : "propertyName must not be null";
        assert object != null : "object must not be null";
        if (!getAllAvailablePropertyNames().contains(propertyName)) {
            throw new IllegalArgumentException("Illegal property name: "
                    + propertyName);
        }
        metadata.setPropertyValue(object, propertyName, propertyValue);
    }

    public PropertyKind getPropertyKind(String propertyName) {
        assert propertyName != null : "propertyName must not be null";
        if (!getAllAvailablePropertyNames().contains(propertyName)) {
            throw new IllegalArgumentException("Illegal property name: "
                    + propertyName);
        }
        if (propertyName.indexOf('.') != -1) {
            return getNestedProperty(propertyName).getPropertyMetadata().getPropertyKind();
        } else {
            return metadata.getProperty(propertyName).getPropertyKind();
        }
    }
}

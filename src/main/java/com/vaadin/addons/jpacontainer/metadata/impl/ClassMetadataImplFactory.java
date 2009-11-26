/*
 * JPAContainer
 * Copyright (C) 2009 Oy IT Mill Ltd
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.metadata.impl;

import com.vaadin.addons.jpacontainer.metadata.ClassMetadata;
import com.vaadin.addons.jpacontainer.metadata.MetadataFactory;
import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata;
import java.beans.Introspector;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

/**
 * Factory for {@link ClassMetadataImpl}.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public final class ClassMetadataImplFactory implements MetadataFactory {

    // TODO Document this class a bit better to make maintenance easier
    @Override
    public <T> ClassMetadata<T> getClassMetadata(Class<T> entityClass) {
        assert entityClass != null : "entityClass must not be null";
        // Check that the class is actually an entity class
        Entity entity = entityClass.getAnnotation(Entity.class);
        if (entity == null) {
            throw new IllegalArgumentException(
                    "The class is not an entity class");
        }

        // Get identifier properties
        Collection<PropertyMetadata> identifierProperties = getIdentifierProperties(
                entityClass);
        if (identifierProperties.isEmpty()) {
            throw new IllegalArgumentException("No identifier properties found");
        }

        // Get the rest of the properties
        Collection<PropertyMetadata> otherProperties =
                new LinkedList<PropertyMetadata>();
        loadProperties(entityClass, otherProperties,
                identifierProperties.iterator().next().getAccessType());

        // Create map of properties
        HashMap<String, PropertyMetadata> properties =
                new HashMap<String, PropertyMetadata>();
        for (PropertyMetadata idProp : identifierProperties) {
            properties.put(idProp.getName(), idProp);
        }
        for (PropertyMetadata prop : otherProperties) {
            properties.put(prop.getName(), prop);
        }

        // Create metadata instance
        String entityName = entity.name().isEmpty()
                ? entityClass.getSimpleName() : entity.name();
        ClassMetadataImpl<T> metadata = new ClassMetadataImpl<T>(entityName,
                entityClass, properties, identifierProperties);
        return metadata;
    }

    static void loadProperties(Class<?> type,
            Collection<PropertyMetadata> properties,
            PropertyMetadata.AccessType accessType) {

        if (accessType == PropertyMetadata.AccessType.FIELD) {
            extractPropertiesFromFields(type, properties, null, true);
        } else {
            extractPropertiesFromMethods(type, properties, null, true);
        }

        Class<?> superclass = type.getSuperclass();
        if (superclass != null && (superclass.getAnnotation(
                MappedSuperclass.class) != null || superclass.getAnnotation(
                Entity.class) != null)) {
            loadProperties(superclass, properties, accessType);
        }
    }

    static Collection<PropertyMetadata> getIdentifierProperties(
            Class<?> type) {
        // Start by looking for annotated fields
        for (Field f : type.getDeclaredFields()) {
            if (f.getAnnotation(Id.class) != null) {
                // We have a single ID property
                LinkedList<PropertyMetadata> l =
                        new LinkedList<PropertyMetadata>();
                l.add(new PropertyMetadataImpl(f.getName(), f.getType(), false,
                        false, false, f, null, null));
                return l;
            } else if (f.getAnnotation(EmbeddedId.class) != null) {
                // We have an embedded ID
                LinkedList<PropertyMetadata> l =
                        new LinkedList<PropertyMetadata>();
                extractPropertiesFromFields(f.getType(), l, f.getName(), true);
                return l;
            }
        }

        // Then look for annotated getter methods
        for (Method m : type.getDeclaredMethods()) {
            if (m.getName().startsWith("get") && m.getReturnType() != Void.TYPE) {
                if (m.getAnnotation(Id.class) != null) {
                    // We have a single ID property
                    try {
                        LinkedList<PropertyMetadata> l =
                                new LinkedList<PropertyMetadata>();
                        Method setter = type.getDeclaredMethod("set" + m.getName().
                                substring(3), m.getReturnType());
                        l.add(new PropertyMetadataImpl(Introspector.decapitalize(
                                m.getName().substring(3)), m.getReturnType(),
                                false,
                                false, false, null, m, setter));
                        return l;
                    } catch (NoSuchMethodException e) {
                        // We have no corresponding setter method!
                        return null;
                    }
                } else if (m.getAnnotation(EmbeddedId.class) != null) {
                    // We have an embedded ID
                    LinkedList<PropertyMetadata> l =
                            new LinkedList<PropertyMetadata>();
                    extractPropertiesFromMethods(m.getReturnType(), l, Introspector.
                            decapitalize(
                            m.getName().substring(3)), true);
                    return l;
                }
            }
        }

        // Nothing found? Try with the superclass!
        Class<?> superclass = type.getSuperclass();
        if (superclass != null && (superclass.getAnnotation(
                MappedSuperclass.class) != null || superclass.getAnnotation(
                Entity.class) != null)) {
            return getIdentifierProperties(superclass);
        }
        return null;
    }

    static boolean isReference(AccessibleObject ab) {
        return (ab.getAnnotation(OneToOne.class) != null || ab.getAnnotation(
                ManyToOne.class) != null);
    }

    static boolean isCollection(AccessibleObject ab) {
        return (ab.getAnnotation(OneToMany.class) != null || ab.getAnnotation(
                ManyToMany.class) != null);
    }

    static void extractPropertiesFromFields(Class<?> type,
            Collection<PropertyMetadata> properties, String owner,
            boolean ignoreIdentifier) {
        for (Field f : type.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (!Modifier.isFinal(mod) && !Modifier.isStatic(mod) && !Modifier.
                    isTransient(mod) && f.getAnnotation(Transient.class) == null) {

                if (!ignoreIdentifier || (f.getAnnotation(Id.class) == null && f.
                        getAnnotation(EmbeddedId.class) == null)) {

                    String name;
                    if (owner == null) {
                        name = f.getName();
                    } else {
                        name = owner + "." + f.getName();
                    }

                    if (f.getAnnotation(Embedded.class) != null) {
                        // The field is embedded
                        extractPropertiesFromFields(f.getType(), properties,
                                name, ignoreIdentifier);
                    } else {
                        // Are we dealing with a reference?
                        boolean reference = isReference(f);
                        // Are we dealing with a collection?
                        boolean collection = isCollection(f);

                        properties.add(
                                new PropertyMetadataImpl(name,
                                f.getType(), owner
                                != null, reference, collection, f, null, null));
                    }
                }
            }
        }
    }

    static void extractPropertiesFromMethods(Class<?> type,
            Collection<PropertyMetadata> properties, String owner,
            boolean ignoreIdentifier) {
        for (Method m : type.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (m.getName().startsWith("get") && !Modifier.isStatic(mod) && m.
                    getAnnotation(Transient.class) == null && m.getReturnType()
                    != Void.TYPE) {
                if (!ignoreIdentifier || (m.getAnnotation(Id.class) == null && m.
                        getAnnotation(EmbeddedId.class) == null)) {
                    try {
                        // Check if we have a setter
                        Method setter = type.getDeclaredMethod("set" + m.getName().
                                substring(3), m.getReturnType());

                        String name;
                        if (owner == null) {
                            name = Introspector.decapitalize(
                                    m.getName().substring(3));
                        } else {
                            name = owner + "." + Introspector.decapitalize(m.
                                    getName().
                                    substring(3));
                        }

                        if (m.getAnnotation(Embedded.class) != null) {
                            // The property is embedded
                            extractPropertiesFromMethods(m.getReturnType(),
                                    properties,
                                    name, ignoreIdentifier);
                        } else {
                            // Are we dealing with a reference?
                            boolean reference = isReference(m);
                            // Are we dealing with a collection?
                            boolean collection = isCollection(m);

                            properties.add(new PropertyMetadataImpl(name, m.
                                    getReturnType(), owner != null, reference,
                                    collection, null, m, setter));
                        }
                    } catch (NoSuchMethodException ignoreit) {
                        // No setter <=> no persistent property
                    }
                }
            }
        }
    }
}

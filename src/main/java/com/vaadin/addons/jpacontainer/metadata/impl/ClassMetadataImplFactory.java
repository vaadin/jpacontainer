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
import com.vaadin.addons.jpacontainer.metadata.EntityClassMetadata;
import com.vaadin.addons.jpacontainer.metadata.MetadataFactory;
import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata;
import com.vaadin.addons.jpacontainer.metadata.PropertyMetadata.AccessType;
import java.beans.Introspector;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Embeddable;
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
import javax.persistence.Version;

/**
 * Factory for {@link ClassMetadataImpl}.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public final class ClassMetadataImplFactory implements MetadataFactory {

    private Map<Class<?>, ClassMetadataImpl> metadataMap =
            new HashMap<Class<?>, ClassMetadataImpl>();

    @Override
    public ClassMetadata getClassMetadata(Class<?> mappedClass,
            AccessType accessType) throws IllegalArgumentException {
        assert mappedClass != null : "mappedClass must not be null";
        assert accessType != null : "accessType must not be null";

        // Check if we already have the metadata in cache
        ClassMetadataImpl metadata = metadataMap.get(mappedClass);
        if (metadata != null) {
            return metadata;
        }

        // Check if we are dealing with an entity class or an embeddable class
        Entity entity = mappedClass.getAnnotation(Entity.class);
        Embeddable embeddable = mappedClass.getAnnotation(Embeddable.class);
        if (entity != null) {
            // We have an entity class
            String entityName = entity.name().isEmpty()
                    ? mappedClass.getSimpleName() : entity.name();
            metadata = new EntityClassMetadataImpl(
                    mappedClass, entityName);
            // Put the metadata instance in the cache in case it is referenced
            // from loadProperties()
            metadataMap.put(mappedClass, metadata);
            loadProperties(mappedClass, metadata, accessType);

            // Locate the version and identifier properties
            EntityClassMetadataImpl entityMetadata =
                    (EntityClassMetadataImpl) metadata;
            for (PropertyMetadata pm : entityMetadata.getMappedProperties()) {
                if (pm.getAnnotation(Version.class) != null) {
                    entityMetadata.setVersionProperty(
                            pm.getName());
                } else if (pm.getAnnotation(Id.class) != null || pm.
                        getAnnotation(EmbeddedId.class) != null) {
                    entityMetadata.setIdentifierProperty(
                            pm.getName());
                }
                if (entityMetadata.hasIdentifierProperty() && entityMetadata.
                        hasVersionProperty()) {
                    // No use continuing the loop if both the version
                    // and the identifier property have already been found.
                    break;
                }
            }
        } else if (embeddable != null) {
            // We have an embeddable class
            metadata = new ClassMetadataImpl(mappedClass);
            // Put the metadata instance in the cache in case it is referenced
            // from loadProperties()
            metadataMap.put(mappedClass, metadata);
            loadProperties(mappedClass, metadata, accessType);
        } else {
            throw new IllegalArgumentException(
                    "The class is nether an entity nor embeddable");
        }

        return metadata;
    }

    @Override
    public EntityClassMetadata getEntityClassMetadata(Class<?> mappedClass)
            throws IllegalArgumentException {
        assert mappedClass != null : "mappedClass must not be null";
        if (mappedClass.getAnnotation(Entity.class) == null) {
            throw new IllegalArgumentException("The class is not an entity");
        }
        PropertyMetadata.AccessType accessType =
                determineAccessType(mappedClass);
        if (accessType == null) {
            throw new IllegalArgumentException(
                    "The access type could not be determined");
        } else {
            return (EntityClassMetadata) getClassMetadata(mappedClass,
                    accessType);
        }
    }

    void loadProperties(Class<?> type,
            ClassMetadataImpl metadata,
            PropertyMetadata.AccessType accessType) {

        if (accessType == PropertyMetadata.AccessType.FIELD) {
            extractPropertiesFromFields(type, metadata);
        } else {
            extractPropertiesFromMethods(type, metadata);
        }

        // Also check superclass for metadata
        Class<?> superclass = type.getSuperclass();
        if (superclass != null && (superclass.getAnnotation(
                MappedSuperclass.class) != null || superclass.getAnnotation(
                Entity.class) != null) || superclass.getAnnotation(
                Embeddable.class) != null) {
            loadProperties(superclass, metadata, accessType);
        }
    }

    PropertyMetadata.AccessType determineAccessType(Class<?> type) {
        // Start by looking for annotated fields
        for (Field f : type.getDeclaredFields()) {
            if (f.getAnnotation(Id.class) != null || f.getAnnotation(
                    EmbeddedId.class) != null) {
                return AccessType.FIELD;
            }
        }

        // Then look for annotated getter methods
        for (Method m : type.getDeclaredMethods()) {
            if (m.getAnnotation(Id.class) != null || m.getAnnotation(
                    EmbeddedId.class) != null) {
                return AccessType.METHOD;
            }
        }

        // Nothing found? Try with the superclass!
        Class<?> superclass = type.getSuperclass();
        if (superclass != null && (superclass.getAnnotation(
                MappedSuperclass.class) != null || superclass.getAnnotation(
                Entity.class) != null)) {
            return determineAccessType(superclass);
        }

        // The access type could not be determined;
        return null;
    }

    boolean isReference(AccessibleObject ab) {
        return (ab.getAnnotation(OneToOne.class) != null || ab.getAnnotation(
                ManyToOne.class) != null);
    }

    boolean isCollection(AccessibleObject ab) {
        return (ab.getAnnotation(OneToMany.class) != null || ab.getAnnotation(
                ManyToMany.class) != null);
    }

    boolean isEmbedded(AccessibleObject ab) {
        return (ab.getAnnotation(Embedded.class) != null || ab.getAnnotation(
                EmbeddedId.class) != null);
    }

    void extractPropertiesFromFields(Class<?> type,
            ClassMetadataImpl metadata) {
        for (Field f : type.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (!Modifier.isFinal(mod) && !Modifier.isStatic(mod) && !Modifier.
                    isTransient(mod) && f.getAnnotation(Transient.class) == null) {
                if (isEmbedded(f)) {
                    ClassMetadata cm = getClassMetadata(f.getType(),
                            AccessType.FIELD);
                    metadata.addEmbeddedProperty(f.getName(), cm, f, null, null);
                } else if (isReference(f)) {
                    ClassMetadata cm = getClassMetadata(f.getType(),
                            AccessType.FIELD);
                    metadata.addReferenceProperty(f.getName(), cm, f, null, null);
                } else if (isCollection(f)) {
                    metadata.addCollectionProperty(f.getName(), f.getType(), f,
                            null, null);
                } else {
                    metadata.addProperty(f.getName(), f.getType(), f, null, null);
                }
            }
        }
    }

    void extractPropertiesFromMethods(Class<?> type,
            ClassMetadataImpl metadata) {
        for (Method m : type.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (m.getName().startsWith("get") && !Modifier.isStatic(mod) && m.
                    getAnnotation(Transient.class) == null && m.getReturnType()
                    != Void.TYPE) {
                try {
                    // Check if we have a setter
                    Method setter = type.getDeclaredMethod("set" + m.getName().
                            substring(3), m.getReturnType());

                    String name = Introspector.decapitalize(m.getName().
                            substring(3));
                    if (isEmbedded(m)) {
                        ClassMetadata cm = getClassMetadata(m.getReturnType(),
                                AccessType.METHOD);
                        metadata.addEmbeddedProperty(name, cm, null, m,
                                setter);
                    } else if (isReference(m)) {
                        ClassMetadata cm = getClassMetadata(m.getReturnType(),
                                AccessType.METHOD);
                        metadata.addReferenceProperty(name, cm, null, m,
                                setter);
                    } else if (isCollection(m)) {
                        metadata.addCollectionProperty(name, m.getReturnType(),
                                null, m,
                                setter);
                    } else {
                        metadata.addProperty(name, m.getReturnType(), null, m,
                                setter);
                    }
                } catch (NoSuchMethodException ignoreit) {
                    // No setter <=> transient property
                }
            }
        }
    }
}

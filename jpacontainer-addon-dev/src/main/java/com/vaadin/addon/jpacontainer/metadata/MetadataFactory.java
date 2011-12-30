/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.metadata;

import java.beans.Introspector;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.ElementCollection;
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

import com.vaadin.addon.jpacontainer.metadata.PersistentPropertyMetadata.AccessType;

/**
 * Factory for creating and populating {@link ClassMetadata} and
 * {@link EntityClassMetadata} instances.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class MetadataFactory {

    private static MetadataFactory INSTANCE;
    private Map<Class<?>, ClassMetadata<?>> metadataMap = new HashMap<Class<?>, ClassMetadata<?>>();

    protected MetadataFactory() {
        // NOP
    }

    /**
     * Gets the singleton instance of this factory.
     * 
     * @return the factory instance (never null).
     */
    public static MetadataFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MetadataFactory();
        }
        return INSTANCE;
    }

    /**
     * Extracts the entity class metadata from <code>mappedClass</code>. The
     * access type (field or method) will be determined from the location of the
     * {@link Id} or {@link EmbeddedId} annotation. If both of these are
     * missing, this method will fail. This method will also fail if
     * <code>mappedClass</code> lacks the {@link Entity} annotation.
     * 
     * @param mappedClass
     *            the mapped class (must not be null).
     * @return the class metadata.
     * @throws IllegalArgumentException
     *             if no metadata could be extracted.
     */
    public <T> EntityClassMetadata<T> getEntityClassMetadata(
            Class<T> mappedClass) throws IllegalArgumentException {
        assert mappedClass != null : "mappedClass must not be null";
        if (mappedClass.getAnnotation(Entity.class) == null) {
            throw new IllegalArgumentException("The class is not an entity");
        }
        PersistentPropertyMetadata.AccessType accessType = determineAccessType(mappedClass);
        if (accessType == null) {
            throw new IllegalArgumentException(
                    "The access type could not be determined");
        } else {
            return (EntityClassMetadata<T>) getClassMetadata(mappedClass,
                    accessType);
        }
    }

    /**
     * Extracts the class metadata from <code>mappedClass</code>. If
     * <code>mappedClass</code> is {@link Embeddable}, the result will be an
     * instance of {@link ClassMetadata}. If <code>mappedClass</code> is an
     * {@link Entity}, the result will be an instance of
     * {@link EntityClassMetadata}.
     * <p>
     * <code>accessType</code> instructs the factory where to look for
     * annotations and which defaults to assume if there are no annotations.
     * 
     * @param mappedClass
     *            the mapped class (must not be null).
     * @param accessType
     *            the location where to look for annotations (must not be null).
     * @return the class metadata.
     * @throws IllegalArgumentException
     *             if no metadata could be extracted.
     */
    @SuppressWarnings("unchecked")
    public <T> ClassMetadata<T> getClassMetadata(Class<T> mappedClass,
            PersistentPropertyMetadata.AccessType accessType)
            throws IllegalArgumentException {
        assert mappedClass != null : "mappedClass must not be null";
        assert accessType != null : "accessType must not be null";

        // Check if we already have the metadata in cache
        ClassMetadata<T> metadata = (ClassMetadata<T>) metadataMap
                .get(mappedClass);
        if (metadata != null) {
            return metadata;
        }

        // Check if we are dealing with an entity class or an embeddable class
        Entity entity = mappedClass.getAnnotation(Entity.class);
        Embeddable embeddable = mappedClass.getAnnotation(Embeddable.class);
        if (entity != null) {
            // We have an entity class
            String entityName = entity.name().length() == 0 ? mappedClass
                    .getSimpleName() : entity.name();
            metadata = new EntityClassMetadata<T>(mappedClass, entityName);
            // Put the metadata instance in the cache in case it is referenced
            // from loadProperties()
            metadataMap.put(mappedClass, metadata);
            loadProperties(mappedClass, metadata, accessType);

            // Locate the version and identifier properties
            EntityClassMetadata<T> entityMetadata = (EntityClassMetadata<T>) metadata;
            for (PersistentPropertyMetadata pm : entityMetadata
                    .getPersistentProperties()) {

                if (pm.getAnnotation(Version.class) != null) {
                    entityMetadata.setVersionPropertyName(pm.getName());
                } else if (pm.getAnnotation(Id.class) != null
                        || pm.getAnnotation(EmbeddedId.class) != null) {
                    entityMetadata.setIdentifierPropertyName(pm.getName());
                }
                if (entityMetadata.hasIdentifierProperty()
                        && entityMetadata.hasVersionProperty()) {
                    // No use continuing the loop if both the version
                    // and the identifier property have already been found.
                    break;
                }
            }
        } else if (embeddable != null) {
            // We have an embeddable class
            metadata = new ClassMetadata<T>(mappedClass);
            // Put the metadata instance in the cache in case it is referenced
            // from loadProperties()
            metadataMap.put(mappedClass, metadata);
            loadProperties(mappedClass, metadata, accessType);
        } else {
            throw new IllegalArgumentException("The class "
                    + mappedClass.getName()
                    + " is nether an entity nor embeddable");
        }

        return metadata;
    }

    protected void loadProperties(Class<?> type, ClassMetadata<?> metadata,
            PersistentPropertyMetadata.AccessType accessType) {

        // Also check superclass for metadata
        Class<?> superclass = type.getSuperclass();
        if (superclass != null
                && (superclass.getAnnotation(MappedSuperclass.class) != null || superclass
                        .getAnnotation(Entity.class) != null)
                || superclass.getAnnotation(Embeddable.class) != null) {
            loadProperties(superclass, metadata, accessType);
        }

        if (accessType == PersistentPropertyMetadata.AccessType.FIELD) {
            extractPropertiesFromFields(type, metadata);
        } else {
            extractPropertiesFromMethods(type, metadata);
        }
    }

    protected PersistentPropertyMetadata.AccessType determineAccessType(
            Class<?> type) {
        // Start by looking for annotated fields
        for (Field f : type.getDeclaredFields()) {
            if (f.getAnnotation(Id.class) != null
                    || f.getAnnotation(EmbeddedId.class) != null) {
                return AccessType.FIELD;
            }
        }

        // Then look for annotated getter methods
        for (Method m : type.getDeclaredMethods()) {
            if (m.getAnnotation(Id.class) != null
                    || m.getAnnotation(EmbeddedId.class) != null) {
                return AccessType.METHOD;
            }
        }

        // Nothing found? Try with the superclass!
        Class<?> superclass = type.getSuperclass();
        if (superclass != null
                && (superclass.getAnnotation(MappedSuperclass.class) != null || superclass
                        .getAnnotation(Entity.class) != null)) {
            return determineAccessType(superclass);
        }

        // The access type could not be determined;
        return null;
    }

    protected boolean isReference(AccessibleObject ab) {
        return ab.getAnnotation(ManyToOne.class) != null;
    }

    private boolean isOneToOne(AccessibleObject ab) {
        return ab.getAnnotation(OneToOne.class) != null;
    }

    protected boolean isCollection(AccessibleObject ab) {
        return ab.getAnnotation(OneToMany.class) != null;
    }

    private boolean isManyToMany(AccessibleObject ab) {
        return ab.getAnnotation(ManyToMany.class) != null;
    }

    protected boolean isEmbedded(AccessibleObject ab) {
        return (ab.getAnnotation(Embedded.class) != null || ab
                .getAnnotation(EmbeddedId.class) != null);
    }

    protected void extractPropertiesFromFields(Class<?> type,
            ClassMetadata<?> metadata) {
        for (Field f : type.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (!Modifier.isFinal(mod) && !Modifier.isStatic(mod)
                    && !Modifier.isTransient(mod)
                    && f.getAnnotation(Transient.class) == null) {
                Class<?> fieldType = getFieldType(f);
                if (isEmbedded(f)) {
                    ClassMetadata<?> cm = getClassMetadata(fieldType,
                            AccessType.FIELD);
                    metadata.addProperties(new PersistentPropertyMetadata(f
                            .getName(), cm, PropertyKind.EMBEDDED, f));
                } else if (isReference(f)) {
                    ClassMetadata<?> cm = getClassMetadata(fieldType,
                            AccessType.FIELD);
                    metadata.addProperties(new PersistentPropertyMetadata(f
                            .getName(), cm, PropertyKind.MANY_TO_ONE, f));
                } else if (isOneToOne(f)) {
                    ClassMetadata<?> cm = getClassMetadata(fieldType,
                            AccessType.FIELD);
                    metadata.addProperties(new PersistentPropertyMetadata(f
                            .getName(), cm, PropertyKind.ONE_TO_ONE, f));
                } else if (isCollection(f)) {
                    metadata.addProperties(new PersistentPropertyMetadata(f
                            .getName(), fieldType, PropertyKind.ONE_TO_MANY, f));
                } else if (isManyToMany(f)) {
                    metadata.addProperties(new PersistentPropertyMetadata(f
                            .getName(), fieldType, PropertyKind.MANY_TO_MANY, f));
                } else if (isElementCollection(f)) {
                    metadata.addProperties(new PersistentPropertyMetadata(f
                            .getName(), fieldType,
                            PropertyKind.ELEMENT_COLLECTION, f));
                } else {
                    metadata.addProperties(new PersistentPropertyMetadata(f
                            .getName(), convertPrimitiveType(fieldType),
                            PropertyKind.SIMPLE, f));
                }
            }
        }
        // Find the transient properties
        for (Method m : type.getDeclaredMethods()) {
            int mod = m.getModifiers();
            // Synthetic methods are excluded (#4590).
            // In theory, this could filter out too much in some special cases,
            // in which case the subclass could re-declare the accessor methods
            // with the correct annotations as a workaround.
            if (m.getName().startsWith("get") && !Modifier.isStatic(mod)
                    && !m.isSynthetic() && m.getReturnType() != Void.TYPE) {
                Method setter = null;
                try {
                    // Check if we have a setter
                    setter = type.getDeclaredMethod("set"
                            + m.getName().substring(3), m.getReturnType());
                } catch (NoSuchMethodException ignoreit) {
                }
                String name = Introspector.decapitalize(m.getName()
                        .substring(3));

                if (metadata.getProperty(name) == null) {
                    // No previous property has been added with the same name
                    metadata.addProperties(new PropertyMetadata(name, m
                            .getReturnType(), m, setter));
                }
            }
        }
    }

    protected boolean isElementCollection(AccessibleObject ab) {
        return (ab.getAnnotation(ElementCollection.class) != null);
    }

    /**
     * Finds the actual pointed-to type of the field. The concrete type may be
     * other than the declared type if the targetEntity parameter is specified
     * in certain annotations.
     * 
     * @param f
     *            the field.
     * @return the type of the field.
     */
    private Class<?> getFieldType(Field f) {
        Class<?> targetEntity = void.class;
        if (isReference(f)) {
            targetEntity = f.getAnnotation(ManyToOne.class).targetEntity();
        } else if (isOneToOne(f)) {
            targetEntity = f.getAnnotation(OneToOne.class).targetEntity();
        } else if (isCollection(f)) {
            targetEntity = f.getAnnotation(OneToMany.class).targetEntity();
        } else if (isManyToMany(f)) {
            targetEntity = f.getAnnotation(ManyToMany.class).targetEntity();
        }
        if (targetEntity != void.class) {
            return targetEntity;
        }
        return f.getType();
    }

    private Class<?> convertPrimitiveType(Class<?> type) {
        // Vaadin fields don't work with primitive values, use wrapper types for
        // primitives
        if (type.isPrimitive()) {
            if (type.equals(Boolean.TYPE)) {
                type = Boolean.class;
            } else if (type.equals(Integer.TYPE)) {
                type = Integer.class;
            } else if (type.equals(Float.TYPE)) {
                type = Float.class;
            } else if (type.equals(Double.TYPE)) {
                type = Double.class;
            } else if (type.equals(Byte.TYPE)) {
                type = Byte.class;
            } else if (type.equals(Character.TYPE)) {
                type = Character.class;
            } else if (type.equals(Short.TYPE)) {
                type = Short.class;
            } else if (type.equals(Long.TYPE)) {
                type = Long.class;
            }
        }
        return type;
    }

    protected void extractPropertiesFromMethods(Class<?> type,
            ClassMetadata<?> metadata) {
        for (Method m : type.getDeclaredMethods()) {
            int mod = m.getModifiers();
            // Synthetic methods are excluded (#4590) - otherwise you could e.g.
            // have a synthetic and a concrete id getter (with different
            // declared return types) in TestClasses.Integer_ConcreteId_M, and
            // the synthetic method could override the concrete one and its
            // annotations.
            // In theory, this could filter out too much in some special cases,
            // in which case the subclass could re-declare the accessor methods
            // with the correct annotations as a workaround.
            if (m.getName().startsWith("get") && !Modifier.isStatic(mod)
                    && !m.isSynthetic() && m.getReturnType() != Void.TYPE) {
                Method setter = null;
                try {
                    // Check if we have a setter
                    setter = type.getDeclaredMethod("set"
                            + m.getName().substring(3), m.getReturnType());
                } catch (NoSuchMethodException ignoreit) {
                    // No setter <=> transient property
                }
                String name = Introspector.decapitalize(m.getName()
                        .substring(3));

                if (setter != null && m.getAnnotation(Transient.class) == null) {
                    // Persistent property
                    if (isEmbedded(m)) {
                        ClassMetadata<?> cm = getClassMetadata(
                                m.getReturnType(), AccessType.METHOD);
                        metadata.addProperties(new PersistentPropertyMetadata(
                                name, cm, PropertyKind.EMBEDDED, m, setter));
                    } else if (isReference(m)) {
                        ClassMetadata<?> cm = getClassMetadata(
                                m.getReturnType(), AccessType.METHOD);
                        metadata.addProperties(new PersistentPropertyMetadata(
                                name, cm, PropertyKind.MANY_TO_ONE, m, setter));
                    } else if (isOneToOne(m)) {
                        ClassMetadata<?> cm = getClassMetadata(
                                m.getReturnType(), AccessType.METHOD);
                        metadata.addProperties(new PersistentPropertyMetadata(
                                name, cm, PropertyKind.ONE_TO_ONE, m, setter));
                    } else if (isCollection(m)) {
                        metadata.addProperties(new PersistentPropertyMetadata(
                                name, m.getReturnType(),
                                PropertyKind.ONE_TO_MANY, m, setter));
                    } else if (isManyToMany(m)) {
                        metadata.addProperties(new PersistentPropertyMetadata(
                                name, m.getReturnType(),
                                PropertyKind.MANY_TO_MANY, m, setter));
                    } else if (isElementCollection(m)) {
                        metadata.addProperties(new PersistentPropertyMetadata(
                                name, m.getReturnType(),
                                PropertyKind.ELEMENT_COLLECTION, m, setter));
                    } else {
                        metadata.addProperties(new PersistentPropertyMetadata(
                                name, m.getReturnType(), PropertyKind.SIMPLE,
                                m, setter));
                    }
                } else {
                    // Transient property
                    metadata.addProperties(new PropertyMetadata(name, m
                            .getReturnType(), m, setter));
                }
            }
        }
    }
}

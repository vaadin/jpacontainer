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
package com.vaadin.addon.jpacontainer.metadata;

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

import com.vaadin.addon.jpacontainer.metadata.PersistentPropertyMetadata.AccessType;

/**
 * Factory for creating and populating {@link ClassMetadata} and
 * {@link EntityClassMetadata} instances.
 * 
 * @author Petter Holmström (IT Mill)
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
			throw new IllegalArgumentException(
					"The class is nether an entity nor embeddable");
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
		return (ab.getAnnotation(OneToOne.class) != null || ab
				.getAnnotation(ManyToOne.class) != null);
	}

	protected boolean isCollection(AccessibleObject ab) {
		return (ab.getAnnotation(OneToMany.class) != null || ab
				.getAnnotation(ManyToMany.class) != null);
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
				if (isEmbedded(f)) {
					ClassMetadata<?> cm = getClassMetadata(f.getType(),
							AccessType.FIELD);
					metadata
							.addProperties(new PersistentPropertyMetadata(
									f.getName(),
									cm,
									PersistentPropertyMetadata.PropertyKind.EMBEDDED,
									f));
				} else if (isReference(f)) {
					ClassMetadata<?> cm = getClassMetadata(f.getType(),
							AccessType.FIELD);
					metadata.addProperties(new PersistentPropertyMetadata(f
							.getName(), cm,
							PersistentPropertyMetadata.PropertyKind.REFERENCE,
							f));
				} else if (isCollection(f)) {
					metadata.addProperties(new PersistentPropertyMetadata(f
							.getName(), f.getType(),
							PersistentPropertyMetadata.PropertyKind.COLLECTION,
							f));
				} else {
					metadata.addProperties(new PersistentPropertyMetadata(f
							.getName(), f.getType(),
							PersistentPropertyMetadata.PropertyKind.SIMPLE, f));
				}
			}
		}
		// Find the transient properties
		for (Method m : type.getDeclaredMethods()) {
			int mod = m.getModifiers();
			if (m.getName().startsWith("get") && !Modifier.isStatic(mod)
					&& m.getReturnType() != Void.TYPE) {
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

	protected void extractPropertiesFromMethods(Class<?> type,
			ClassMetadata<?> metadata) {
		for (Method m : type.getDeclaredMethods()) {
			int mod = m.getModifiers();
			if (m.getName().startsWith("get") && !Modifier.isStatic(mod)
					&& m.getReturnType() != Void.TYPE) {
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
						ClassMetadata<?> cm = getClassMetadata(m
								.getReturnType(), AccessType.METHOD);
						metadata
								.addProperties(new PersistentPropertyMetadata(
										name,
										cm,
										PersistentPropertyMetadata.PropertyKind.EMBEDDED,
										m, setter));
					} else if (isReference(m)) {
						ClassMetadata<?> cm = getClassMetadata(m
								.getReturnType(), AccessType.METHOD);
						metadata
								.addProperties(new PersistentPropertyMetadata(
										name,
										cm,
										PersistentPropertyMetadata.PropertyKind.REFERENCE,
										m, setter));
					} else if (isCollection(m)) {
						metadata
								.addProperties(new PersistentPropertyMetadata(
										name,
										m.getReturnType(),
										PersistentPropertyMetadata.PropertyKind.COLLECTION,
										m, setter));
					} else {
						metadata.addProperties(new PersistentPropertyMetadata(
								name, m.getReturnType(),
								PersistentPropertyMetadata.PropertyKind.SIMPLE,
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

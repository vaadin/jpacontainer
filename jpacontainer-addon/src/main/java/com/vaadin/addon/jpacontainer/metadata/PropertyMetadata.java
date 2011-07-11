/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.metadata;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * This class represents the metadata of a property. If the property is
 * transient, this is an ordinary JavaBean property consisting of a getter
 * method and optionally a setter method. If the property is persistent,
 * additional information is provided by the {@link PersistentPropertyMetadata}
 * interface.
 * 
 * @see ClassMetadata
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class PropertyMetadata implements Serializable {

	private static final long serialVersionUID = -6500231861860229121L;
	private final String name;
	private final Class<?> type;
	transient final Method getter;
	transient final Method setter;
	// Required for serialization:
	protected final String getterName;
	protected final String setterName;
	protected final Class<?> getterDeclaringClass;
	protected final Class<?> setterDeclaringClass;

	/**
	 * Creates a new instance of <code>PropertyMetadata</code>.
	 * 
	 * @param name
	 *            the name of the property (must not be null).
	 * @param type
	 *            the type of the property (must not be null).
	 * @param getter
	 *            the getter method that can be used to read the value of the
	 *            property (must not be null).
	 * @param setter
	 *            the setter method that can be used to set the value of the
	 *            property (may be null).
	 */
	PropertyMetadata(String name, Class<?> type, Method getter, Method setter) {
		assert name != null : "name must not be null";
		assert type != null : "type must not be null";
		/*
		 * If we assert that getter != null, PersistentPropertyMetadata will not
		 * work.
		 */
		this.name = name;
		this.type = type;
		this.getter = getter;
		this.setter = setter;
		/*
		 * The getter may also be null, e.g. if PersistentPropertyMetadata uses
		 * a field instead of a getter to access the property.
		 */
		if (getter != null) {
			this.getterName = getter.getName();
			this.getterDeclaringClass = getter.getDeclaringClass();
		} else {
			this.getterName = null;
			this.getterDeclaringClass = null;
		}
		if (setter != null) {
			this.setterName = setter.getName();
			this.setterDeclaringClass = setter.getDeclaringClass();
		} else {
			this.setterName = null;
			this.setterDeclaringClass = null;
		}
	}

	public Object readResolve() throws ObjectStreamException {
		try {
			Method getterM = null;
			if (getterName != null) {
				getterM = getterDeclaringClass.getDeclaredMethod(getterName);
			}
			Method setterM = null;
			if (setterName != null) {
				setterM = setterDeclaringClass.getDeclaredMethod(setterName,
						type);
			}
			return new PropertyMetadata(name, type, getterM, setterM);
		} catch (Exception e) {
			throw new InvalidObjectException(e.getMessage());
		}
	}

	/**
	 * The name of the property.
	 */
	public String getName() {
		return name;
	}

	/**
	 * The type of the property.
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * The annotations of the property, if any.
	 * 
	 * @see #getAnnotation(java.lang.Class)
	 */
	public Annotation[] getAnnotations() {
		return getter.getAnnotations();
	}

	/**
	 * Gets the annotation of the specified annotation class, if available.
	 * 
	 * @see #getAnnotations()
	 * @see Class#getAnnotation(java.lang.Class)
	 * @param annotationClass
	 *            the annotation class.
	 * @return the annotation, or null if not found.
	 */
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return getter.getAnnotation(annotationClass);
	}

	/**
	 * Returns whether the property is writable or not. Transient properties
	 * (i.e. JavaBean properties) are only writable if they have a setter
	 * method.
	 * 
	 * @return true if the property is writable, false if it is not.
	 */
	public boolean isWritable() {
		return setter != null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() == getClass()) {
			PropertyMetadata other = (PropertyMetadata) obj;
			return name.equals(other.name)
					&& type.equals(other.type)
					&& (getter == null ? other.getter == null : getter
							.equals(other.getter))
					&& (setter == null ? other.setter == null : setter
							.equals(other.setter));
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = hash * 31 + name.hashCode();
		hash = hash * 31 + type.hashCode();
		if (getter != null) {
			hash = hash * 31 + getter.hashCode();
		}
		if (setter != null) {
			hash = hash * 31 + setter.hashCode();
		}
		return hash;
	}
}

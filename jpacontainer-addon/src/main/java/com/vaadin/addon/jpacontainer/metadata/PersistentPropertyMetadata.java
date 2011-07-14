/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.metadata;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * An extended version of {@link PropertyMetadata} that provides additional
 * information about persistent properties.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public class PersistentPropertyMetadata extends PropertyMetadata {

	private static final long serialVersionUID = -4097189601179456814L;

	/**
	 * Enumeration defining the property kind.
	 * 
	 * @author Petter Holmström (Vaadin Ltd)
	 */
	public enum PropertyKind {

		/**
		 * The property is embedded.
		 * 
		 * @see javax.persistence.Embeddable
		 * @see javax.persistence.Embedded
		 */
		EMBEDDED,
		/**
		 * The property is a reference.
		 * 
		 * @see javax.persistence.OneToOne
		 * @see javax.persistence.ManyToOne
		 */
		REFERENCE,
		/**
		 * The property is a collection.
		 * 
		 * @see javax.persistence.OneToMany
		 * @see javax.persistence.ManyToMany
		 */
		COLLECTION,
		/**
		 * The property is of a simple datatype.
		 */
		SIMPLE
	}

	/**
	 * Enumeration defining the property access types.
	 * 
	 * @author Petter Holmström (Vaadin Ltd)
	 */
	public enum AccessType {

		/**
		 * The property is accessed as a JavaBean property using getters and
		 * setters.
		 */
		METHOD,
		/**
		 * The property is accessed directly as a field.
		 */
		FIELD
	}

	private final PropertyKind propertyKind;
	private final ClassMetadata<?> typeMetadata;
	transient final Field field;
	// Required for serialization
	protected final String fieldName;
	protected final Class<?> fieldDeclaringClass;

	/**
	 * Creates a new instance of <code>PersistentPropertyMetadata</code>.
	 * 
	 * @param name
	 *            the name of the property (must not be null).
	 * @param type
	 *            the type of the property (must not be null).
	 * @param propertyKind
	 *            the kind of the property, must be either
	 *            {@link PropertyKind#COLLECTION} or {@link PropertyKind#SIMPLE}
	 *            .
	 * @param field
	 *            the field that can be used to access the property (must not be
	 *            null).
	 */
	PersistentPropertyMetadata(String name, Class<?> type,
			PropertyKind propertyKind, Field field) {
		super(name, type, null, null);
		assert propertyKind == PropertyKind.COLLECTION
				|| propertyKind == PropertyKind.SIMPLE : "propertyKind must be COLLECTION or SIMPLE";
		assert field != null : "field must not be null";
		this.propertyKind = propertyKind;
		this.typeMetadata = null;
		this.field = field;
		this.fieldName = field.getName();
		this.fieldDeclaringClass = field.getDeclaringClass();
	}

	/**
	 * Creates a new instance of <code>PersistentPropertyMetadata</code>.
	 * 
	 * @param name
	 *            the name of the property (must not be null).
	 * @param type
	 *            type type of the property (must not be null).
	 * @param propertyKind
	 *            the kind of the property, must be either
	 *            {@link PropertyKind#COLLECTION} or {@link PropertyKind#SIMPLE}
	 *            .
	 * @param getter
	 *            the getter method that can be used to read the property value
	 *            (must not be null).
	 * @param setter
	 *            the setter method that can be used to set the property value
	 *            (must not be null).
	 */
	PersistentPropertyMetadata(String name, Class<?> type,
			PropertyKind propertyKind, Method getter, Method setter) {
		super(name, type, getter, setter);
		assert propertyKind == PropertyKind.COLLECTION
				|| propertyKind == PropertyKind.SIMPLE : "propertyKind must be COLLECTION or SIMPLE";
		assert getter != null : "getter must not be null";
		assert setter != null : "setter must not be null";
		this.propertyKind = propertyKind;
		this.typeMetadata = null;
		this.field = null;
		this.fieldName = null;
		this.fieldDeclaringClass = null;
	}

	/**
	 * Creates a new instance of <code>PersistentPropertyMetadata</code>.
	 * 
	 * @param name
	 *            the name of the property (must not be null).
	 * @param type
	 *            the type metadata of the property (must not be null).
	 * @param propertyKind
	 *            the kind of the property, must be either
	 *            {@link PropertyKind#REFERENCE} or
	 *            {@link PropertyKind#EMBEDDED}.
	 * @param field
	 *            the field that can be used to access the property (must not be
	 *            null).
	 */
	PersistentPropertyMetadata(String name, ClassMetadata<?> type,
			PropertyKind propertyKind, Field field) {
		super(name, type.getMappedClass(), null, null);
		assert type != null : "type must not be null";
		assert propertyKind == PropertyKind.REFERENCE
				|| propertyKind == PropertyKind.EMBEDDED : "propertyKind must be REFERENCE or EMBEDDED";
		assert field != null : "field must not be null";
		this.propertyKind = propertyKind;
		this.typeMetadata = type;
		this.field = field;
		this.fieldName = field.getName();
		this.fieldDeclaringClass = field.getDeclaringClass();
	}

	/**
	 * Creates a new instance of <code>PersistentPropertyMetadata</code>.
	 * 
	 * @param name
	 *            the name of the property (must not be null).
	 * @param type
	 *            the type metadata of the property (must not be null).
	 * @param propertyKind
	 *            the kind of the property, must be either
	 *            {@link PropertyKind#REFERENCE} or
	 *            {@link PropertyKind#EMBEDDED}.
	 * @param getter
	 *            the getter method that can be used to read the property value
	 *            (must not be null).
	 * @param setter
	 *            the setter method that can be used to set the property value
	 *            (must not be null).
	 */
	PersistentPropertyMetadata(String name, ClassMetadata<?> type,
			PropertyKind propertyKind, Method getter, Method setter) {
		super(name, type.getMappedClass(), getter, setter);
		assert type != null : "type must not be null";
		assert propertyKind == PropertyKind.REFERENCE
				|| propertyKind == PropertyKind.EMBEDDED : "propertyKind must be REFERENCE or EMBEDDED";
		assert getter != null : "getter must not be null";
		assert setter != null : "setter must not be null";
		this.propertyKind = propertyKind;
		this.typeMetadata = type;
		this.field = null;
		this.fieldName = null;
		this.fieldDeclaringClass = null;
	}

	/**
	 * This constructor is used when deserializing the object.
	 * 
	 * @see #readResolve()
	 */
	private PersistentPropertyMetadata(String name,
			ClassMetadata<?> typeMetadata, Class<?> type,
			PropertyKind propertyKind, Method getter, Method setter, Field field) {
		super(name, type, getter, setter);
		this.propertyKind = propertyKind;
		this.typeMetadata = typeMetadata;
		this.field = field;
		if (this.field == null) {
			this.fieldName = null;
			this.fieldDeclaringClass = null;
		} else {
			this.fieldName = field.getName();
			this.fieldDeclaringClass = field.getDeclaringClass();
		}
	}

	/**
	 * The metadata of the property type, if it is embedded or a reference.
	 * Otherwise, this method returns null.
	 * 
	 * @see #getPropertyKind()
	 */
	public ClassMetadata<?> getTypeMetadata() {
		return typeMetadata;
	}

	/**
	 * The kind of the property.
	 */
	public PropertyKind getPropertyKind() {
		return propertyKind;
	}

	/**
	 * The way the property value is accessed (as a JavaBean property or as a
	 * field).
	 */
	public AccessType getAccessType() {
		return field != null ? AccessType.FIELD : AccessType.METHOD;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		if (field != null) {
			return field.getAnnotation(annotationClass);
		} else {
			return super.getAnnotation(annotationClass);
		}
	}

	@Override
	public Annotation[] getAnnotations() {
		if (field != null) {
			return field.getAnnotations();
		} else {
			return super.getAnnotations();
		}
	}

	@Override
	public Object readResolve() throws ObjectStreamException {
		try {
			Field f = null;
			if (fieldName != null) {
				f = fieldDeclaringClass.getDeclaredField(fieldName);
			}
			Method getterM = null;
			if (getterName != null) {
				getterM = getterDeclaringClass.getDeclaredMethod(getterName);
			}
			Method setterM = null;
			if (setterName != null) {
				setterM = setterDeclaringClass.getDeclaredMethod(setterName,
						getType());
			}
			return new PersistentPropertyMetadata(getName(), typeMetadata,
					getType(), propertyKind, getterM, setterM, f);
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidObjectException(e.getMessage());
		}
	}

	/**
	 * Persistent properties are always writable.
	 * <p>
	 * {@inheritDoc }.
	 */
	@Override
	public boolean isWritable() {
		return true; // field != null || super.isWritable();
	}

	/*
	 * Note, that we only compare the mapped classes of the typeMetadata
	 * fields. If we compared the typeMetadata fields themselves, we could
	 * run into an infinite loop if there are circular references (e.g.
	 * a parent-property of the same type) in the metadata.
	 */

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) { // Includes check of parameter type
			PersistentPropertyMetadata other = (PersistentPropertyMetadata) obj;
			return propertyKind.equals(other.propertyKind)
					&& (typeMetadata == null ? other.typeMetadata == null
							: typeMetadata.getMappedClass().equals(other.typeMetadata.getMappedClass()))
					&& (field == null ? other.field == null : field
							.equals(other.field));
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();
		hash = hash * 31 + propertyKind.hashCode();
		if (typeMetadata != null) {
			hash = hash * 31 + typeMetadata.getMappedClass().hashCode();
		}
		if (field != null) {
			hash = hash * 31 + field.hashCode();
		}
		return hash;
	}
}

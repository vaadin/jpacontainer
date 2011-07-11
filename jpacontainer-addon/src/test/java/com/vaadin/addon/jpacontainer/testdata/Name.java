/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.testdata;

import java.io.Serializable;
import javax.persistence.Embeddable;
import org.apache.commons.lang.ObjectUtils;

/**
 * Embeddable JavaBean for testing.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
@Embeddable
public class Name implements Serializable, Cloneable {

	private String firstName;
	private String lastName;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Override
	public String toString() {
		return this.firstName + " " + this.lastName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() == getClass()) {
			Name other = (Name) obj;
			return ObjectUtils.equals(this.firstName, other.firstName)
					&& ObjectUtils.equals(this.lastName, other.lastName);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 89 * hash + ObjectUtils.hashCode(firstName);
		hash = 89 * hash + ObjectUtils.hashCode(lastName);
		return hash;
	}

	@Override
	public Name clone() {
		Name n = new Name();
		n.firstName = firstName;
		n.lastName = lastName;
		return n;
	}
}

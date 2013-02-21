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

package com.vaadin.addon.jpacontainer.testdata;

import java.io.Serializable;
import javax.persistence.Embeddable;
import org.apache.commons.lang.ObjectUtils;

/**
 * Embeddable JavaBean for testing.
 *
 * @author Petter Holmström (Vaadin Ltd)
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

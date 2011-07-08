/*
 * JPAContainer
 * Copyright (C) 2010-2011 Oy Vaadin Ltd
 *
 * This program is available both under Commercial Vaadin Add-On
 * License 2.0 (CVALv2) and under GNU Affero General Public License (version
 * 3 or later) at your option.
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and CVALv2 along with this program.  If not, see
 * <http://www.gnu.org/licenses/> and <http://vaadin.com/license/cval-2.0>.
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

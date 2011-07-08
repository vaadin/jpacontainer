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
import java.util.Date;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.time.DateUtils;

/**
 * Entity Java bean with Embedded ID for testing.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
@Entity
public class EmbeddedIdPerson implements Serializable, Cloneable {

	@EmbeddedId
	private Name name;
	@Version
	private Long version;
	@Embedded
	private Address address;
	@Temporal(TemporalType.DATE)
	private Date dateOfBirth;

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public Name getName() {
		return name;
	}

	public void setName(Name name) {
		this.name = name;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() == getClass()) {
			EmbeddedIdPerson other = (EmbeddedIdPerson) obj;
			return ObjectUtils.equals(name, other.name)
					&& ObjectUtils.equals(address, other.address)
					&& (ObjectUtils.equals(dateOfBirth, other.dateOfBirth) || (dateOfBirth != null && other.dateOfBirth != null && DateUtils.
					isSameDay(dateOfBirth, other.dateOfBirth)));
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = hash * 11 + ObjectUtils.hashCode(name);
		hash = hash * 11 + ObjectUtils.hashCode(address);
		hash = hash * 11 + ObjectUtils.hashCode(dateOfBirth);
		return hash;
	}

	@Override
	public EmbeddedIdPerson clone() {
		EmbeddedIdPerson c = new EmbeddedIdPerson();
		c.name = (name == null ? null : name.clone());
		c.address = (address == null ? null : address.clone());
		c.dateOfBirth = dateOfBirth;
		return c;
	}
}

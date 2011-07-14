/*
${license.header.text}
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
 * @author Petter Holmström (Vaadin Ltd)
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

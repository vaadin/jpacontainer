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
package com.vaadin.addon.jpacontainer.testdata;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.time.DateUtils;

/**
 * Entity Java bean for testing.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints =
@UniqueConstraint(columnNames = {"lastName",
	"firstName"}))
public class Person implements Serializable, Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Version
	private Long version;
	private String firstName;
	private String lastName;
	@Temporal(TemporalType.DATE)
	private Date dateOfBirth;
	@Embedded
	private Address address;
	private transient String tempData;
	@ManyToOne
	private Person manager;
	@OneToMany(cascade=CascadeType.ALL)
	@JoinColumn(name="person_fk")
	private Set<PersonSkill> skills = new HashSet<PersonSkill>();

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

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getFullName() {
		return getFirstName() + " " + getLastName();
	}

	public Address getTransientAddress() {
		return address;
	}

	public String getTempData() {
		return tempData;
	}

	public void setTempData(String tempData) {
		this.tempData = tempData;
	}

	public Person getManager() {
		return manager;
	}

	public void setManager(Person manager) {
		this.manager = manager;
	}

	public Set<PersonSkill> getSkills() {
		return skills;
	}

	public void setSkills(Set<PersonSkill> skills) {
		this.skills = skills;
	}

	public void addSkill(Skill skill, Integer level) {
		PersonSkill ps = new PersonSkill();
		ps.setLevel(level);
		ps.setSkill(skill);
		ps.setPerson(this);
		skills.add(ps);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Person) {
			Person p = (Person) obj;
			return ObjectUtils.equals(address, p.address)
					&& (ObjectUtils.equals(dateOfBirth, p.dateOfBirth) || (dateOfBirth != null && p.dateOfBirth != null && DateUtils.
					isSameDay(dateOfBirth, p.dateOfBirth)))
					&& ObjectUtils.equals(firstName, p.firstName)
					&& ObjectUtils.equals(lastName, p.lastName)
					&& ObjectUtils.equals(manager, p.manager)
					&& ObjectUtils.equals(skills, p.skills);
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(address)
				+ 7 * ObjectUtils.hashCode(dateOfBirth)
				+ 7 * ObjectUtils.hashCode(firstName)
				+ 7 * ObjectUtils.hashCode(lastName)
				+ 7 * ObjectUtils.hashCode(manager)
				+ 7 * ObjectUtils.hashCode(skills);
	}

	@Override
	public String toString() {
		String dob = null;
		if (dateOfBirth != null) {
			dob = DateFormat.getDateInstance(
					DateFormat.SHORT).format(dateOfBirth);
		}
		return lastName + ", " + firstName + ", " + dob + ", " + address + " (ID: " + id + ")";
	}

	@Override
	public Person clone() {
		Person p = new Person();
		p.address = address == null ? null : address.clone();
		p.dateOfBirth = dateOfBirth;
		p.firstName = firstName;
		p.id = id;
		p.lastName = lastName;
		p.tempData = tempData;
		p.version = version;
		p.manager = manager;
		p.skills = new HashSet<PersonSkill>();
		for (PersonSkill ps : skills) {
			PersonSkill cps = ps.clone();
			cps.setPerson(p);
			p.skills.add(cps);
		}
		return p;
	}
}

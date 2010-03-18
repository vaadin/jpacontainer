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
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

/**
 * Entity Java bean for testing.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
@Entity
@Table(uniqueConstraints =
@UniqueConstraint(columnNames = {"person_fk",
	"skill_fk"}))
public class PersonSkill implements Serializable, Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Version
	private Long version;
	@ManyToOne
	@JoinColumn(name = "skill_fk", nullable = false)
	private Skill skill;
	@ManyToOne
	@JoinColumn(name = "person_fk", nullable = false)
	private Person person;
	private Integer level;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Skill getSkill() {
		return skill;
	}

	public void setSkill(Skill skill) {
		this.skill = skill;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PersonSkill other = (PersonSkill) obj;
		if (this.skill != other.skill && (this.skill == null || !this.skill.
				equals(other.skill))) {
			return false;
		}
		/*if (this.person != other.person && (this.person == null || !this.person.equals(other.person))) {
		return false;
		}*/
		if (this.level != other.level && (this.level == null || !this.level.
				equals(other.level))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 67 * hash + (this.skill != null ? this.skill.hashCode() : 0);
		//hash = 67 * hash + (this.person != null ? this.person.hashCode() : 0);
		hash = 67 * hash + (this.level != null ? this.level.hashCode() : 0);
		return hash;
	}

	@Override
	public PersonSkill clone() {
		PersonSkill ps = new PersonSkill();
		ps.id = id;
		ps.level = level;
		ps.person = person;
		ps.skill = skill;
		ps.version = version;
		return ps;
	}
}

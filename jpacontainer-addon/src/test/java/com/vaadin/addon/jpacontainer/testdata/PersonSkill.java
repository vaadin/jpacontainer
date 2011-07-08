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

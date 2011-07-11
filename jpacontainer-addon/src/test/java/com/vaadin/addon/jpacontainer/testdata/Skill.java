/*
${license.header.text}
 */
package com.vaadin.addon.jpacontainer.testdata;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * Entity Java bean for testing.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
@Entity
public class Skill implements Serializable, Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Version
	private Long version;
	@Column(unique = true)
	private String skillName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getSkillName() {
		return skillName;
	}

	public void setSkillName(String skillName) {
		this.skillName = skillName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Skill other = (Skill) obj;
		if ((this.skillName == null) ? (other.skillName != null) : !this.skillName.
				equals(other.skillName)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + (this.skillName != null ? this.skillName.hashCode() : 0);
		return hash;
	}

	@Override
	public Skill clone() {
		Skill s = new Skill();
		s.id = id;
		s.skillName = skillName;
		s.version = version;
		return s;
	}
}

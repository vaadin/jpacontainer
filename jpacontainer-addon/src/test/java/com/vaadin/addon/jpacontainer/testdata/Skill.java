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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * Entity Java bean for testing.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
@Entity
public class Skill implements Serializable, Cloneable, Comparable<Skill> {

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
		if ((skillName == null) ? (other.skillName != null) : !skillName
				.equals(other.skillName)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + (skillName != null ? skillName.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return skillName;
	}

	@Override
	public Skill clone() {
		Skill s = new Skill();
		s.id = id;
		s.skillName = skillName;
		s.version = version;
		return s;
	}

	@Override
	public int compareTo(Skill o) {
		return skillName.compareTo(o.getSkillName());
	}
}

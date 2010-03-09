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
package com.vaadin.addon.jpacontainer;

import com.vaadin.data.Container;

/**
 * This is a preliminary interface for adding hierarchical support to JPAContainer.
 * It works if the entities can be nested by means of a parent property, e.g. like
 * this:
 * <code>
 * <pre>
 * class MyNodeEntity {
 *   MyNodeEntity parent;
 *   ...
 * }
 * </pre>
 * </code>
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public interface HierarchicalEntityContainer<T> extends EntityContainer<T>, Container.Hierarchical  {

	/**
	 * Sets the persistent property (may be nested) that contains the reference
	 * to the parent entity. The parent entity is expected to be of the same
	 * type as the child entity.
	 *
	 * @param parentProperty the name of the parent property.
	 */
	public void setParentProperty(String parentProperty);

	/**
	 * Gets the name of the persistent property that contains the reference
	 * to the parent entity.
	 *
	 * @return the name of the parent property, or null if not specified.
	 */
	public String getParentProperty();
}

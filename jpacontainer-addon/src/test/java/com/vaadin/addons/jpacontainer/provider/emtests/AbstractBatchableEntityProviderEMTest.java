/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addons.jpacontainer.provider.emtests;

import com.vaadin.addons.jpacontainer.BatchableEntityProvider;

/**
 * Abstract test case for {@link BatchableEntityProvider} that should work with
 * any entity manager that follows the specifications. Subclasses should provide
 * a concrete entity manager implementation to test. If the test passes, the
 * entity manager implementation should work with JPAContainer.
 * <p>
 * Note, that the test data used will not contain circular references that might
 * cause problems when committing buffered changes.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public abstract class AbstractBatchableEntityProviderEMTest extends
		AbstractMutableEntityProviderEMTest {
	// TODO Add tests
}

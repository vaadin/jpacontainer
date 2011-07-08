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
package com.vaadin.addon.jpacontainer.provider.emtests;

import com.vaadin.addon.jpacontainer.EntityProvider;
import com.vaadin.addon.jpacontainer.provider.CachingMutableLocalEntityProvider;
import com.vaadin.addon.jpacontainer.testdata.EmbeddedIdPerson;
import com.vaadin.addon.jpacontainer.testdata.Person;

/**
 * Base class for the {@link CachingMutableLocalEntityProvider} Entity Manager tests.
 * 
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public abstract class AbstractCachingMutableLocalEntityProviderEMTest extends
		AbstractMutableLocalEntityProviderEMTest {

	@Override
	protected EntityProvider<Person> createEntityProvider() throws Exception {
		CachingMutableLocalEntityProvider<Person> provider = new CachingMutableLocalEntityProvider<Person>(
				Person.class, getEntityManager());
		provider.setCacheInUse(true);
		provider.setCloneCachedEntities(true);
		provider.setEntityCacheMaxSize(400);
		provider.setTransactionsHandledByProvider(true);
		return provider;
	}

	@Override
	protected EntityProvider<EmbeddedIdPerson> createEntityProvider_EmbeddedId() throws Exception {
		CachingMutableLocalEntityProvider<EmbeddedIdPerson> provider = new CachingMutableLocalEntityProvider<EmbeddedIdPerson>(
				EmbeddedIdPerson.class, getEntityManager());
		provider.setCacheInUse(true);
		provider.setCloneCachedEntities(true);
		provider.setTransactionsHandledByProvider(true);
		return provider;
	}

	// TODO Add some test cases that try out the caching features as well
}

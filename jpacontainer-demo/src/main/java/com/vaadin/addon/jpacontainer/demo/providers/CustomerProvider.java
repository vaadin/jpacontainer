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
package com.vaadin.addon.jpacontainer.demo.providers;

import com.vaadin.addon.jpacontainer.demo.domain.Customer;
import org.springframework.stereotype.Repository;

/**
 * Entity provider for {@link Customer}s that uses Spring's declarative
 * transaction annotations. It is also annotated with the {@link Repository} annotation,
 * which means that the Spring container will automatically detect it and
 * add it to the container.
 *
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
@Repository(value = "customerProvider")
public class CustomerProvider extends LocalEntityProviderBean<Customer> {

    /**
     * Creates a new <code>CustomerProvider</code>.
     */
    public CustomerProvider() {
        super(Customer.class);
    }
}

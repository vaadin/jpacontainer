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
/*
JPAContainer
Copyright (C) 2009-2011 Oy Vaadin Ltd

This program is available under GNU Affero General Public License (version
3 or later at your option).

See the file licensing.txt distributed with this software for more
information about licensing.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addon.jpacontainer.demo.providers;

import org.springframework.stereotype.Repository;

import com.vaadin.addon.jpacontainer.demo.domain.Order;

/**
 * Entity provider for {@link Order}s that uses Spring's declarative transaction
 * annotations. It is also annotated with the {@link Repository} annotation,
 * which means that the Spring container will automatically detect it and add it
 * to the container.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
@Repository(value = "orderProvider")
public class OrderProvider extends LocalEntityProviderBean<Order> {

    /**
     * Creates a new <code>OrderProvider</code>.
     */
    public OrderProvider() {
        super(Order.class);
    }
}

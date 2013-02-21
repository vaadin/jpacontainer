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
package com.vaadin.addon.jpacontainer.fieldfactory;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.ui.AbstractSelect;

/**
 * Wrapper property that translates entities to identifiers and visa versa.
 * Expects that the translator is used in a select backed by a jpacontainer.
 */
@SuppressWarnings("unchecked")
public class SingleSelectTranslator extends PropertyTranslator {

    private final AbstractSelect select;

    public SingleSelectTranslator(AbstractSelect select) {
        this.select = select;
    }

    @SuppressWarnings("rawtypes")
    private EntityContainer getContainer() {
        return (EntityContainer) select.getContainerDataSource();
    }

    @Override
    public Object translateFromDatasource(Object value) {
        // Value here is entity, should be transformed to identifier
        return getContainer().getEntityProvider().getIdentifier(value);
    }

    @Override
    public Object translateToDatasource(Object formattedValue) throws Exception {
        // formattedValue here is identifier, to be formatted to entity
        return getContainer().getEntityProvider().getEntity(getContainer(), formattedValue);
    }

}

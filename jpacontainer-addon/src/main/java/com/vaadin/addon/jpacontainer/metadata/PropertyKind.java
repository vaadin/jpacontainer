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
package com.vaadin.addon.jpacontainer.metadata;

import javax.persistence.Embeddable;

/**
 * Enumeration defining the property kind.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 */
public enum PropertyKind {

    /**
     * The property is embedded.
     * 
     * @see javax.persistence.Embeddable
     * @see javax.persistence.Embedded
     */
    EMBEDDED,
    /**
     * The property is a reference.
     * 
     * @see javax.persistence.OneToOne
     * @see javax.persistence.ManyToOne
     */
    MANY_TO_ONE,
    /**
     * The property is a reference.
     * 
     * @see javax.persistence.ManyToOne
     */
    ONE_TO_ONE,
     /**
     * The property is a collection.
     * 
     * @see javax.persistence.OneToMany
     */
    ONE_TO_MANY,
    /**
     * The property is a reference.
     * 
     * @see javax.persistence.ManyToMany
     */
    MANY_TO_MANY,
    /**
     * The property is a collection {@link Embeddable}s or basic data types.
     * 
     * @see javax.persistence.ElementCollection
     */
    ELEMENT_COLLECTION,
    /**
     * The property is of a simple datatype.
     */
    SIMPLE,
    /**
     * The property is not persistent property.
     */
    NONPERSISTENT
}
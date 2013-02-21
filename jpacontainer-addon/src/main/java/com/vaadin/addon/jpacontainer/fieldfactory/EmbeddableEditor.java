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

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import com.vaadin.addon.jpacontainer.EntityContainer;

/**
 * A field that edits {@link Embeddable} or {@link ElementCollection} should
 * implement this interface. {@link FieldFactory} can then provide a rudimentary
 * support for relations from the {@link Embedded} object.
 * 
 */
public interface EmbeddableEditor {
    
    @SuppressWarnings("rawtypes")
    EntityContainer getMasterEntityContainer();
    
    Class<?> getEmbeddedClassType();

}

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

package com.vaadin.addon.jpacontainer;

import com.vaadin.data.Property;

/**
 * Interface defining the Properties that are contained in a {@link EntityItem}.
 * 
 * @author Petter Holmström (Vaadin Ltd)
 * @since 1.0
 */
public interface EntityItemProperty extends Property,
        Property.ValueChangeNotifier {

    /**
     * Gets the EntityItem that owns this property.
     * 
     * @return the item (never null).
     */
    public EntityItem<?> getItem();
    
    /**
     * Gets the property id of this property.
     * 
     * @return the identifier of the property
     */
    public String getPropertyId();

    /**
     * Fires value change event for this property
     */
    void fireValueChangeEvent();
}

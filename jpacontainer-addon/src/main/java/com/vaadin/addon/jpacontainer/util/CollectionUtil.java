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
package com.vaadin.addon.jpacontainer.util;

import java.lang.reflect.Array;
import java.util.Collection;

public class CollectionUtil {

    /**
     * Converts a typed {@link Collection} to a typed array.
     * 
     * @param type
     *            the type class
     * @param collection
     *            the collection to convert to an array.
     * @return the array.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Class<T> type, Collection<T> collection) {
        return collection.toArray((T[]) Array.newInstance(type,
                collection.size()));
    }
}

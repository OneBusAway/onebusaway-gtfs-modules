/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs.services;

import org.onebusaway.gtfs.model.Translation;

public interface TranslationService {
    /**
     * Get a cloned version of a GTFS entity object with fields translated as per the GTFS
     * Translations proposed spec addition. See {@link Translation}
     *
     * @param language language to translate to
     * @param entity entity type
     * @param instance instance to clone and translate
     * @param <T> entity type
     * @return cloned instance with the proper fields changed
     */
    <T> T getTranslatedEntity(String language, Class<T> entity, T instance);
}

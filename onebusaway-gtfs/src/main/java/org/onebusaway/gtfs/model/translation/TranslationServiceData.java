/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs.model.translation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationServiceData implements Serializable {

    private static final long serialVersionUID = 1L;

    // Map: list of translations by entity type and language
    private Map<TypeAndLanguage, List<PropertyTranslation>> _translationMap = new HashMap<>();;

    private String _feedLanguage;

    public List<PropertyTranslation> getTranslationsByTypeAndLanguage(Class<?> type, String language) {
        return _translationMap.get(new TypeAndLanguage(type, language));
    }

    public void putTranslation(Class<?> type, String language, PropertyTranslation translation) {
        TypeAndLanguage key = new TypeAndLanguage(type, language);
        List<PropertyTranslation> translations = _translationMap.computeIfAbsent(key, k -> new ArrayList<>());
        translations.add(translation);
    }

    public String getFeedLanguage() {
        return _feedLanguage;
    }

    public void setFeedLanguage(String feedLanguage) {
        _feedLanguage = feedLanguage;
    }
}

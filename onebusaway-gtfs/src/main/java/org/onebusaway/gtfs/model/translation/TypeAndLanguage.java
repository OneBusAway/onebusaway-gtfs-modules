/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs.model.translation;

import java.io.Serializable;
import java.util.Objects;

public class TypeAndLanguage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Class<?> type;

    private String language;

    TypeAndLanguage(Class<?> type, String language) {
        this.type = type;
        this.language = language;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeAndLanguage that = (TypeAndLanguage) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(language, that.language);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, language);
    }
}

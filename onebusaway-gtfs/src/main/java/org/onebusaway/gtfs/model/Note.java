/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;

@CsvFields(filename = "notes.txt", required = false)
public final class Note extends IdentityBean<AgencyAndId> {

    private static final long serialVersionUID = 1L;

    @CsvField(name = "note_id", optional = true)
    private String noteId;

    @CsvField(name = "note_mark", optional = true)
    private String mark;

    @CsvField(name = "note_title", optional = true)
    private String title;

    @CsvField(name = "note_description", optional = true)
    private String desc;

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String id) {
        this.noteId = id;
        System.out.println(this.noteId);
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
        System.out.println(this.mark);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        System.out.println(this.title);
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
        System.out.println(this.desc);
    }



    private AgencyAndId id = null;

    @Override
    public String toString() {
        return "<TimetableNote " + this.noteId + ">";
    }

    public AgencyAndId getId() {
        return id;
    }

    public void setId(AgencyAndId id) {
        System.out.println(id);
        this.id = id;
        System.out.println(this.id);
    }
}

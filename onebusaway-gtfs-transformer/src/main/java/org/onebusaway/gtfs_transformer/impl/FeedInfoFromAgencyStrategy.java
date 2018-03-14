/**
 * Copyright (C) 2017 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.FeedInfo;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

import java.util.Scanner;

public class FeedInfoFromAgencyStrategy implements GtfsTransformStrategy {

  private String agencyId;

  @CsvField(optional = true)
  private String defaultLang = "en";

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    for (Agency agency : dao.getAllAgencies()) {
      if (agency.getId().equals(agencyId)) {
        FeedInfo info = new FeedInfo();
        Scanner scanner = new Scanner(agencyId);
        if (scanner.hasNextInt()) {
          info.setId(scanner.nextInt());
        }
        info.setPublisherName(agency.getName());
        info.setPublisherUrl(agency.getUrl());
        if (agency.getLang() == null || agency.getLang().isEmpty()) {
          info.setLang(defaultLang);
        } else {
          info.setLang(agency.getLang());
        }
        dao.saveEntity(info);
      }
    }
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public void setDefaultLang(String lang) {
    this.defaultLang = lang;
  }
}

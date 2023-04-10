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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.FeedInfo;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class FeedInfoFromAgencyStrategy implements GtfsTransformStrategy {

  private static Logger _log = LoggerFactory.getLogger(FeedInfoFromAgencyStrategy.class);
  private String agencyId;
  @CsvField(optional = true)
  private String feedVersion;

  @CsvField(optional = true)
  private String defaultLang = "en";

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

    boolean foundAgency = false;
    for (Agency agency : dao.getAllAgencies()) {
      _log.info("comparing agency " + agency.getId() + " to " + agencyId);
      if (agency.getId().equals(agencyId)) {
        foundAgency = true;
        _log.info("creating feed info from matched agency " + agencyId);
        FeedInfo info = getFeedInfoFromAgency(dao, agency);
        // if version already present leave it alone
        if (info.getVersion() == null) {
          addCreationTime(info, context);
          dao.saveOrUpdateEntity(info);
        } else {
          _log.info("found feedVersion " + info.getVersion() + ", abandoning");
        }
      }
    }
    if (!foundAgency) {
      // we didn't find the expected agency, try a default agency / first agency
      Agency agency = dao.getAllAgencies().iterator().next();
      FeedInfo info = getFeedInfoFromAgency(dao, agency);
      _log.info("creating feed info from unmatched agency " + agency.getId());
      addCreationTime(info, context);
      dao.saveOrUpdateEntity(info);
    }
  }

  private FeedInfo getFeedInfoFromAgency(GtfsMutableRelationalDao dao, Agency agency) {
    // cannot just use dao.getFeedInfoFromAgencyForId if it needs to be compatable with "update" SimpleModificationStrategy
    FeedInfo info = dao.getAllFeedInfos().stream().
            filter(feed->feed.getId().equals(agencyId))
            .collect(Collectors.toMap(feed->feed.getId(), feed -> feed))
            .get(agency.getId());
    if (info==null) {
      info = new FeedInfo();
    }
    info.setId(agencyId);
    info.setPublisherName(agency.getName());
    info.setPublisherUrl(agency.getUrl());
    if (agency.getLang() == null || agency.getLang().isEmpty()) {
      info.setLang(defaultLang);
    } else {
      info.setLang(agency.getLang());
    }
    return info;
  }

  private void addCreationTime(FeedInfo feedInfo, TransformContext context) {
    Long creationTime = (Long)context.getReader().getContext().get("lastModifiedTime");
    SimpleDateFormat df = new SimpleDateFormat("zzz: dd-MMM-yyyy HH:mm");
    if (creationTime != null) {
      _log.info("setting version to lastModifiedTime of " + new Date(creationTime));
      feedInfo.setVersion(df.format(new Date(creationTime)));
    }
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public void setDefaultLang(String lang) {
    this.defaultLang = lang;
  }

  public void setFeedVersion(String feedVersion){this.feedVersion = feedVersion;}
}

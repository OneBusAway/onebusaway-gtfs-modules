/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_merge.strategies;

import java.util.Collection;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;

/** Merge metadata files such as modifications.txt */
public class MetadataMergeStrategy implements EntityMergeStrategy {
  @Override
  public void getEntityTypes(Collection<Class<?>> entityTypes) {
    // no-op, metadata is not represented via entityTypes
  }

  @Override
  public void merge(GtfsMergeContext context) {
    GtfsRelationalDao source = context.getSource();
    GtfsMutableRelationalDao target = context.getTarget();
    String agencyStr = getAgencyStr(source);
    for (String filename : source.getOptionalMetadataFilenames()) {
      if (source.hasMetadata(filename)) {
        StringBuffer content = new StringBuffer();
        content.append("\n====== ");
        content.append(agencyStr);
        content.append(" ======\n");
        content.append(source.getMetadata(filename));
        if (target.hasMetadata(filename)) {
          content.append(target.getMetadata(filename));
        }
        target.addMetadata(filename, content.toString());
      }
    }
  }

  private String getAgencyStr(GtfsRelationalDao source) {
    int agencyCount = source.getAllAgencies().size();
    if (agencyCount == 0) return "";
    if (agencyCount == 1) return source.getAllAgencies().iterator().next().getId();
    StringBuffer sb = new StringBuffer();
    sb.append("[");
    for (Agency agency : source.getAllAgencies()) {
      sb.append(agency).append(",");
    }
    return sb.substring(0, sb.length() - 2) + "]";
  }
}

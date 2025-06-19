/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_transformer.impl;

import java.util.Map;
import java.util.regex.PatternSyntaxException;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Clean up the stop names based on input file considerations */
public class MergeStopNamesFromReferenceStrategy implements GtfsTransformStrategy {

  private final Logger _log = LoggerFactory.getLogger(MergeStopNamesFromReferenceStrategy.class);

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run(TransformContext context, GtfsMutableRelationalDao dao) {
    Map<String, String> map =
        new InputLibrary().readOrderedMap((String) context.getParameter(("stopMappingFile")));

    Map<String, String> regexMap =
        new InputLibrary().readOrderedMap((String) context.getParameter(("regexFile")));

    for (Stop stop : dao.getAllStops()) {
      String upperCaseName = stop.getName();
      String beautifiedName = "";
      if (upperCaseName == null) continue;
      for (String s : upperCaseName.split(" ")) {
        beautifiedName += toProperCase(s) + " ";
      }
      beautifiedName = beautifiedName.substring(0, beautifiedName.length() - 1);

      for (String regex : regexMap.keySet()) {
        try {
          beautifiedName = beautifiedName.replaceAll(regex, regexMap.get(regex));
        } catch (PatternSyntaxException pse) {
          _log.error(" invalid regex combination |{}|=|{}|", regex, regexMap.get(regex), pse);
        }
      }

      if (map.containsKey(beautifiedName)) {
        _log.trace("swapped |{}| for |{}|", beautifiedName, map.get(beautifiedName));
        beautifiedName = map.get(beautifiedName);
      }
      stop.setName(beautifiedName);
    }
  }

  private String toProperCase(String s) {
    if (s == null || s.length() < 2) return s;
    return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
  }
}

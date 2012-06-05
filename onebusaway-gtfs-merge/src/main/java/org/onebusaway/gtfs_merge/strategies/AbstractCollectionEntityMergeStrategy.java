/**
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.gtfs_merge.strategies;

import java.io.Serializable;

import org.onebusaway.csv_entities.exceptions.CsvException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs_merge.GtfsMergeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCollectionEntityMergeStrategy<KEY extends Serializable>
    extends AbstractEntityMergeStrategy {

  private static final Logger _log = LoggerFactory.getLogger(AbstractCollectionEntityMergeStrategy.class);

  private final String _keyDescription;

  public AbstractCollectionEntityMergeStrategy(String keyDescription) {
    _keyDescription = keyDescription;
  }

  @Override
  public void merge(GtfsMergeContext context) {
    for (KEY key : getKeys(context)) {
      processKey(context, key);
    }
  }

  private void processKey(GtfsMergeContext context, KEY key) {
    KEY duplicate = getDuplicate(context, key);
    if (duplicate != null) {
      switch (_duplicatesStrategy) {
        case DROP: {
          logDuplicateKey(key);
          if (!duplicate.equals(key)) {
            renameKey(context, key, duplicate);
          }
          return;
        }
        case RENAME: {
          KEY newKey = getRenamedKey(context, key);
          renameKey(context, key, newKey);
          key = newKey;
          MergeSupport.clearCaches(context.getSource());
          break;
        }
      }
    }
    String rawKey = getRawKey(key);
    context.putEntityWithRawId(rawKey, key);
    saveElementsForKey(context, key);
  }

  protected abstract Iterable<KEY> getKeys(GtfsMergeContext context);

  private KEY getDuplicate(GtfsMergeContext context, KEY key) {
    switch (_duplicateDetectionStrategy) {
      case IDENTITY:
        return getIdentityDuplicate(context, key);
      case FUZZY:
        return getFuzzyDuplicate(context, key);
      default:
        throw new IllegalStateException(
            "unknown duplicate detection strategy: "
                + _duplicateDetectionStrategy);
    }
  }

  @SuppressWarnings("unchecked")
  private KEY getIdentityDuplicate(GtfsMergeContext context, KEY key) {
    String rawKey = getRawKey(key);
    return (KEY) context.getEntityForRawId(rawKey);
  }

  private KEY getFuzzyDuplicate(GtfsMergeContext context, KEY key) {
    return null;
  }

  protected String getRawKey(KEY key) {
    if (key instanceof AgencyAndId) {
      return ((AgencyAndId) key).getId();
    }
    throw new UnsupportedOperationException(
        "cannot generate raw key for type: " + key.getClass());
  }

  private void logDuplicateKey(KEY key) {
    switch (_logDuplicatesStrategy) {
      case NONE:
        break;
      case WARNING:
        _log.warn("duplicate key: type=" + _keyDescription + " key=" + key);
        break;
      case ERROR:
        throw new CsvException("duplicate key: type=" + _keyDescription
            + " key=" + key);
    }
  }

  protected abstract void renameKey(GtfsMergeContext context, KEY oldId,
      KEY newId);

  protected abstract void saveElementsForKey(GtfsMergeContext context, KEY key);

  @SuppressWarnings("unchecked")
  private KEY getRenamedKey(GtfsMergeContext context, KEY key) {
    if (key instanceof String) {
      return (KEY) (context.getPrefix() + key);
    } else if (key instanceof AgencyAndId) {
      return (KEY) MergeSupport.renameAgencyAndId(context, (AgencyAndId) key);
    }
    throw new UnsupportedOperationException("uknown key type: "
        + key.getClass());
  }
}

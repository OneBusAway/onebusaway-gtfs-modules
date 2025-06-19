/**
 * Copyright (C) 2012 Google, Inc.
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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.onebusaway.csv_entities.exceptions.CsvException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_merge.GtfsMergeContext;
import org.onebusaway.gtfs_merge.strategies.scoring.DuplicateScoringSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class that defines common methods and properties for merging collection-like GTFS
 * entities. Collection-like entities are entity types where a collection of entries are identified
 * by a common identifier. That includes entities like {@link ShapePoint} entries in shapes.txt,
 * where one {@code shapeId} identifies a series of shape points. It also includes entries like
 * {@link ServiceCalendar} and {@link ServiceCalendarDate} entries from calendar.txt and
 * calendar_dates.txt, where one {@code service_id} potentially covers multiple calendar entries.
 *
 * @author bdferris
 * @param <KEY> the type for the id object class that is used to uniquely identify a collection
 *     entity
 */
public abstract class AbstractCollectionEntityMergeStrategy<KEY extends Serializable>
    extends AbstractEntityMergeStrategy {

  private static final Logger _log =
      LoggerFactory.getLogger(AbstractCollectionEntityMergeStrategy.class);

  /** Returned by {@link #getDescription()} */
  private final String _keyDescription;

  public AbstractCollectionEntityMergeStrategy(String keyDescription) {
    _keyDescription = keyDescription;
  }

  @Override
  public void merge(GtfsMergeContext context) {
    for (KEY key : getKeys(context.getSource())) {
      processKey(context, key);
    }
  }

  /**
   * We process each entity collection with a particular id in turn, looking for duplicates and
   * taking appropriate action to merge the resulting entities into the output feed.
   *
   * @param context
   * @param key the identifier of the current entity collection to process
   */
  private void processKey(GtfsMergeContext context, KEY key) {
    KEY duplicate = getDuplicate(context, key);
    if (duplicate != null) {
      logDuplicateKey(key);
      if (!duplicate.equals(key)) {
        renameKey(context, key, duplicate);
      }
      return;
    }
    String rawKey = getRawKey(key);

    /**
     * If we've already saved elements using this key previously, we need to rename this key to
     * avoid duplication.
     */
    if (context.getEntityForRawId(rawKey) != null) {
      KEY newKey = getRenamedKey(context, key);
      renameKey(context, key, newKey);
      key = newKey;
      rawKey = getRawKey(key);
      MergeSupport.clearCaches(context.getSource());
    }

    context.putEntityWithRawId(rawKey, key);
    saveElementsForKey(context, key);
  }

  /**
   * An entity-specific method to determine the set of unique identifiers used by collection
   * entities in the specified GTFS feed.
   *
   * @param dao
   * @return the set of unique identifiers
   */
  protected abstract Collection<KEY> getKeys(GtfsRelationalDao dao);

  /**
   * Determines if the entity collection with the specified id overlaps with an entity collection
   * already in the merged output feed. If a duplicate is found, the id of the already-present
   * entity collection is returned. If no duplicate is found, returns null.
   *
   * @param context
   * @param key
   * @return the id of an existing, duplicate entity collection in the output feed, or null if none
   *     exists
   */
  private KEY getDuplicate(GtfsMergeContext context, KEY key) {
    EDuplicateDetectionStrategy duplicateDetectionStrategy =
        determineDuplicateDetectionStrategy(context);
    switch (duplicateDetectionStrategy) {
      case IDENTITY:
        return getIdentityDuplicate(context, key);
      case FUZZY:
        return getFuzzyDuplicate(context, key);
      case NONE:
        return null;
      default:
        throw new IllegalStateException(
            "unknown duplicate detection strategy: " + _duplicateDetectionStrategy);
    }
  }

  @Override
  protected EDuplicateDetectionStrategy pickBestDuplicateDetectionStrategy(
      GtfsMergeContext context) {

    Collection<KEY> targetKeys = getKeys(context.getTarget());
    Collection<KEY> sourceKeys = getKeys(context.getSource());

    /** If there are no entities, then we can't have identifier overlap. */
    if (targetKeys.isEmpty() || sourceKeys.isEmpty()) {
      return EDuplicateDetectionStrategy.NONE;
    }

    if (hasLikelyIdentifierOverlap(context, sourceKeys, targetKeys)) {
      return EDuplicateDetectionStrategy.IDENTITY;
    } else if (hasLikelyFuzzyOverlap(context, sourceKeys, targetKeys)) {
      return EDuplicateDetectionStrategy.FUZZY;
    } else {
      return EDuplicateDetectionStrategy.NONE;
    }
  }

  /**
   * Determines if the two set of collection identifiers have enough overlap between entities with
   * the same id to indicate that {@link EDuplicateDetectionStrategy#IDENTITY} duplicate detection
   * can be used.
   *
   * @param context
   * @param sourceKeys
   * @param targetKeys
   * @return true if identity duplicate detection seems appropriate
   */
  private boolean hasLikelyIdentifierOverlap(
      GtfsMergeContext context, Collection<KEY> sourceKeys, Collection<KEY> targetKeys) {

    /**
     * There needs to be a reasonable number of overlapping identifiers in the first place for us to
     * consider using identifier-based duplicate detection.
     */
    Set<KEY> commonKeys = new HashSet<KEY>();
    double elementOvelapScore =
        DuplicateScoringSupport.scoreElementOverlap(sourceKeys, targetKeys, commonKeys);
    if (commonKeys.isEmpty() || elementOvelapScore < _minElementsInCommonScoreForAutoDetect) {
      return false;
    }

    /**
     * We score each entity pair with a common key between the two feeds to determine if entities
     * with the same key really are duplicates.
     */
    double totalScore = 0.0;
    for (KEY key : commonKeys) {
      totalScore += scoreDuplicateKey(context, key);
    }
    totalScore /= commonKeys.size();
    return totalScore > _minElementsDuplicateScoreForAutoDetect;
  }

  /**
   * Given an id identifying an entity collection in both the source input feed and the merged
   * output feed, produce a score between 0.0 and 1.0 identifying how likely it is that the two
   * entity collections are one and the same, where 0.0 means they having nothing in common and 1.0
   * meaning they are exactly the same.
   *
   * @param context
   * @param key
   * @return
   */
  protected abstract double scoreDuplicateKey(GtfsMergeContext context, KEY key);

  /**
   * Determines if the collection entities in source input feed and the target merged output feed
   * appear to have fuzzy duplicates. Sub-classes can override this method to provide a
   * fuzzy-duplicate detection strategy.
   *
   * @param context
   * @param sourceKeys
   * @param targetKeys
   * @return true if the two feeds appear to have fuzzy duplicates
   */
  private boolean hasLikelyFuzzyOverlap(
      GtfsMergeContext context, Collection<KEY> sourceKeys, Collection<KEY> targetKeys) {
    return false;
  }

  /**
   * Find the id of an existing entity collection in the merged output feed with the specified id.
   * Returns null if no identifier-based duplicate exists.
   *
   * <p>Why don't we just do an identifier equality check? In the case of identifiers like {@link
   * AgencyAndId}, two ids might have different agency ids when their raw GTFS ids are the same.
   * Thus the "equal" identifer may not actual be equal in the strict Java sense.
   *
   * @param context
   * @param key
   * @return the id of the identifier-based duplicate entity collection, or null if not found
   */
  @SuppressWarnings("unchecked")
  private KEY getIdentityDuplicate(GtfsMergeContext context, KEY key) {
    String rawKey = getRawKey(key);
    return (KEY) context.getEntityForRawId(rawKey);
  }

  private KEY getFuzzyDuplicate(GtfsMergeContext context, KEY key) {
    return null;
  }

  /**
   * Converts the entity collection identifier into a raw GTFS identifier string. This is what we
   * actually use for identity duplicate detection.
   *
   * @param key
   * @return
   */
  protected String getRawKey(KEY key) {
    if (key instanceof AgencyAndId) {
      return ((AgencyAndId) key).getId();
    }
    throw new UnsupportedOperationException("cannot generate raw key for type: " + key.getClass());
  }

  private void logDuplicateKey(KEY key) {
    switch (_logDuplicatesStrategy) {
      case NONE:
        break;
      case WARNING:
        _log.warn("duplicate key: type=" + _keyDescription + " key=" + key);
        break;
      case ERROR:
        throw new CsvException("duplicate key: type=" + _keyDescription + " key=" + key);
    }
  }

  /**
   * If we detect that an entity collection in the source input feed duplicates an entity collection
   * in the merged output feed, we rename all references to the old id in the source feed to use the
   * id of the entity in the merged feed. That way, when examining other entities in the source feed
   * that referenced the original entity collection with entities in the target feed that reference
   * the duplicate entity, both sets of entity will now appear to reference the same thing. This can
   * be useful for similarity detection.
   *
   * @param context
   * @param oldId the original id in the source input feed
   * @param newId the new id, which replaces the old in the source input feed
   */
  protected abstract void renameKey(GtfsMergeContext context, KEY oldId, KEY newId);

  /**
   * Writes the specified entity collection to the merged output feed.
   *
   * @param context
   * @param key the identifier for the entity collection to save
   */
  protected abstract void saveElementsForKey(GtfsMergeContext context, KEY key);

  /**
   * Renames the specified identifier to make it unique in the merged output feed. Useful for when
   * you find two entity collections with the same identifier that aren't actually duplicates.
   *
   * @param context
   * @param key
   * @return
   */
  @SuppressWarnings("unchecked")
  private KEY getRenamedKey(GtfsMergeContext context, KEY key) {
    if (key instanceof String) {
      if (this.getDuplicateRenamingStrategy() == EDuplicateRenamingStrategy.AGENCY) {
        _log.warn("rename with type String not supported for key=" + key);
      }
      return (KEY) (context.getPrefix() + key);
    } else if (key instanceof AgencyAndId) {
      if (this.getDuplicateRenamingStrategy() == EDuplicateRenamingStrategy.AGENCY) {
        return (KEY)
            MergeSupport.renameAgencyAndId(
                ((AgencyAndId) key).getAgencyId() + "-", (AgencyAndId) key);
      }
      return (KEY) MergeSupport.renameAgencyAndId(context, (AgencyAndId) key);
    }
    throw new UnsupportedOperationException("uknown key type: " + key.getClass());
  }

  @Override
  protected String getDescription() {
    return _keyDescription;
  }
}

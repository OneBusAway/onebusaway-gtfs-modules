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

import org.onebusaway.gtfs.model.Transfer;

/**
 * Entity merge strategy for handling {@link Transfer} entities.
 *
 * @author bdferris
 */
public class TransferMergeStrategy
    extends AbstractNonIdentifiableSingleEntityMergeStrategy<Transfer> {

  public TransferMergeStrategy() {
    super(Transfer.class);
  }

  @Override
  protected boolean entitiesAreIdentical(Transfer transferA, Transfer transferB) {
    if (!transferA.getFromStop().equals(transferB.getFromStop())) {
      return false;
    }
    if (!transferA.getToStop().equals(transferB.getToStop())) {
      return false;
    }
    if (transferA.getTransferType() != transferB.getTransferType()) {
      return false;
    }
    if (transferA.getMinTransferTime() != transferB.getMinTransferTime()) {
      return false;
    }
    return true;
  }
}

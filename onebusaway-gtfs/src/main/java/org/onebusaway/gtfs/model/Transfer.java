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

public final class Transfer extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  private static final int MISSING_VALUE = -999;

  private int id;

  private Stop fromStop;

  private Stop toStop;

  private int transferType;

  private int minTransferTime = MISSING_VALUE;

  public Transfer() {

  }

  public Transfer(Transfer obj) {
    this.id = obj.id;
    this.fromStop = obj.fromStop;
    this.toStop = obj.toStop;
    this.transferType = obj.transferType;
    this.minTransferTime = obj.minTransferTime;
  }

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  public Stop getFromStop() {
    return fromStop;
  }

  public void setFromStop(Stop fromStop) {
    this.fromStop = fromStop;
  }

  public Stop getToStop() {
    return toStop;
  }

  public void setToStop(Stop toStop) {
    this.toStop = toStop;
  }

  public int getTransferType() {
    return transferType;
  }

  public void setTransferType(int transferType) {
    this.transferType = transferType;
  }

  public boolean isMinTransferTimeSet() {
    return minTransferTime != MISSING_VALUE;
  }

  public int getMinTransferTime() {
    return minTransferTime;
  }

  public void setMinTransferTime(int minTransferTime) {
    this.minTransferTime = minTransferTime;
  }

  public void clearMinTransferTime() {
    this.minTransferTime = MISSING_VALUE;
  }

  public String toString() {
    return "<Transfer " + getId() + ">";
  }
}

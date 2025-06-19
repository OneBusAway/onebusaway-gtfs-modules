/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs.model;

public interface BlockProxy {
  public Integer getId();

  public void setId(Integer id);

  public int getBlockSequence();

  public void setBlockSequence(int blockSequence);

  public int getBlockVairable();

  public void setBlockVariable(int blockVariable);

  public int getBlockRoute();

  public void setBlockRoute(int blockRoute);

  public int getBlockRun();

  public void setBlockRun(int blockRun);
}

/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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

@CsvFields(filename = "block.txt", required = false)
public final class Block extends IdentityBean<Integer> {
  private static final long serialVersionUID = 1L;
  
  @CsvField(ignore = true)
  private int id;
  
  @CsvField(name = "block_seq_num")
  private int blockSequence;
  
  @CsvField(name = "block_var_num")
  private int blockVariable;
  
  @CsvField(name = "block_route_num")
  private int blockRoute;
  
  @CsvField(name = "block_run_num")
  private int blockRun;

  @CsvField(ignore = true)
  private transient BlockProxy proxy = null;
  
  
  public Block() {
    
  }
  
  public Block(Block b) {
    this.blockSequence = b.blockSequence;
    this.blockVariable = b.blockVariable;
    this.blockRoute = b.blockRoute;
    this.blockRun = b.blockRun;
  }
  
  @Override
  public Integer getId() {
    if (proxy != null) {
      return proxy.getId();
    }
    return id;
  }

  @Override
  public void setId(Integer id) {
    if (proxy != null) {
      proxy.setId(id);
      return;
    }
    this.id = id;
  }
  
  public int getBlockSequence() {
    if (proxy != null) {
      return proxy.getBlockSequence();
    }
    return blockSequence;
  }

  public void setBlockSequence(int blockSequence) {
    if (proxy != null) {
      proxy.setBlockSequence(blockSequence);
      return;
    }
    this.blockSequence = blockSequence;
  }
  
  public int getBlockVariable() {
    if (proxy != null) {
      return proxy.getBlockVairable();
    }
    return blockVariable;
  }

  public void setBlockVariable(int blockVariable) {
    if (proxy != null) {
      proxy.setBlockVariable(blockVariable);
      return;
    }
    this.blockVariable = blockVariable;
  }

  public int getBlockRoute() {
    if (proxy != null) {
      return proxy.getBlockRoute();
    }
    return blockRoute;
  }

  public void setBlockRoute(int blockRoute) {
    if (proxy != null) {
      proxy.setBlockRoute(blockRoute);
      return;
    }
    this.blockRoute = blockRoute;
  }

  public int getBlockRun() {
    if (proxy != null) {
      return proxy.getBlockRun();
    }
    return blockRun;
  }

  public void setBlockRun(int blockRun) {
    if (proxy != null) {
      proxy.setBlockRun(blockRun);
      return;
    }
    this.blockRun = blockRun;
  }
  
  
}

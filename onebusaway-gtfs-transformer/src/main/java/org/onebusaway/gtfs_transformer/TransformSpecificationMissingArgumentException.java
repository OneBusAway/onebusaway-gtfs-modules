/**
 * Copyright (C) 2012 Google Inc.
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
package org.onebusaway.gtfs_transformer;

public class TransformSpecificationMissingArgumentException extends
    TransformSpecificationException {

  private static final long serialVersionUID = 1L;

  public TransformSpecificationMissingArgumentException(String line, String name) {
    super("missing required argument: \"" + name + "\"", line);
  }

  public TransformSpecificationMissingArgumentException(String line,
      String[] names) {
    super("missing required argument: " + join(names), line);
  }

  public TransformSpecificationMissingArgumentException(String line,
      String name, String parent) {
    super("missing required argument: \"" + name + "\" in parent \"" + parent
        + "\" section", line);
  }

  public TransformSpecificationMissingArgumentException(String line,
      String[] names, String parent) {
    super("missing required argument: " + join(names) + " in parent \""
        + parent + "\" section", line);
  }

  private static String join(String[] args) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < args.length; ++i) {
      if (i > 0) {
        if (args.length > 2) {
          b.append(",");
        }
        b.append(" ");
        if (i + 1 == args.length) {
          b.append("or ");
        }
      }
    }
    return b.toString();
  }
}

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
package org.onebusaway.collections.beans;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultPropertyMethodResolver implements PropertyMethodResolver {

  private static final String OBA_IFACE_PATH =
      "org.onebusaway.transit_data_federation.services.transit_graph.";
  private static final String OBA_IMPL_PATH =
      "org.onebusaway.transit_data_federation.impl.transit_graph.";
  private static Map<String, List<Method>> interfaceMethodsByKey = new HashMap();
  private static Map<String, String> interfaceToImplMap;

  static {
    // TODO inject these somehow
    interfaceToImplMap = new HashMap<>();
    // support GTFS interface corner case
    interfaceToImplMap.put(
        "org.onebusaway.gtfs.model.StopLocation", "org.onebusaway.gtfs.model.Stop");
    // support OBA interfaces
    String[] entryInterfaces = {
      "AgencyEntry",
      "BlockConfigurationEntry",
      "BlockEntry",
      "BlockStopTimeEntry",
      "BlockTripEntry",
      "FrequencyBlockStopTimeEntry",
      "FrequencyEntry",
      "RouteCollectionEntry",
      "RouteEntry",
      "StopEntry",
      "StopTimeEntry",
      "TripEntry"
    };
    for (String interfaceName : entryInterfaces) {
      interfaceToImplMap.put(
          OBA_IFACE_PATH + interfaceName, OBA_IMPL_PATH + interfaceName + "Impl");
    }
  }

  @Override
  public PropertyMethod getPropertyMethod(Class<?> targetType, String propertyName) {
    String methodName = "get";
    for (String part : propertyName.split(" |_")) {
      methodName += part.substring(0, 1).toUpperCase() + part.substring(1);
    }
    Method method = null;
    try {
      if (targetType.isInterface()) {
        List<Method> methods = getCachedInterfaceMethods(targetType, propertyName, methodName);
        if (methods.size() == 1) method = methods.getFirst();
        else {
          throw new IllegalStateException(
              "Ambiguous implementation set for interface: "
                  + targetType
                  + " /"
                  + methodName
                  + " with potentials: "
                  + methods
                  + " and "
                  + interfaceToImplMap.keySet()
                  + " known interface mappings");
        }
      } else method = targetType.getMethod(methodName);
    } catch (Exception ex) {
      throw new IllegalStateException("error introspecting class: " + targetType, ex);
    }
    if (method == null) {
      throw new IllegalStateException(
          "could not find property \"" + propertyName + "\" for type " + targetType.getName());
    }
    method.setAccessible(true);
    return new PropertyMethodImpl(method);
  }

  private List<Method> getCachedInterfaceMethods(
      Class<?> targetType, String propertyName, String methodName) {
    String key = hash(targetType, propertyName);
    if (interfaceMethodsByKey.containsKey(key)) {
      return interfaceMethodsByKey.get(key);
    }

    ScanResult scanResult =
        new ClassGraph().acceptPackages("org.onebusaway").enableClassInfo().scan();

    List<Method> methods = new ArrayList<>();
    for (ClassInfo ci : scanResult.getClassesImplementing(targetType.getCanonicalName())) {
      try {
        if (matches(ci.getName(), targetType)) {
          methods.add(Class.forName(ci.getName()).getMethod(methodName));
        }
      } catch (Exception e) {
        continue;
      }
    }
    interfaceMethodsByKey.put(key, methods);

    return methods;
  }

  private boolean matches(String reflectedTypeName, Class<?> targetType) {
    String targetTypeName = targetType.getName();
    if (interfaceToImplMap.containsKey(targetTypeName)) {
      String implName = interfaceToImplMap.get(targetTypeName);
      return implName.equals(reflectedTypeName);
    }
    return targetTypeName.equals(reflectedTypeName);
  }

  private String hash(Class<?> targetType, String propertyName) {
    return targetType.getName() + "." + propertyName;
  }
}

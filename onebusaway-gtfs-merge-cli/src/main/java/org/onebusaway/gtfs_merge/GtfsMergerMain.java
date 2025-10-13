/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_merge;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs_merge.strategies.AbstractEntityMergeStrategy;
import org.onebusaway.gtfs_merge.strategies.EntityMergeStrategy;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "gtfs-merge",
    description = "Merge GTFS feeds",
    usageHelpAutoWidth = true,
    mixinStandardHelpOptions = true)
public class GtfsMergerMain implements Callable<Integer> {

  @Parameters(
      arity = "2..*",
      description = "Input GTFS directories/files followed by output directory")
  private List<File> files;

  @Option(
      names = {"--file"},
      description = "GTFS file name to configure")
  private List<String> fileOptions;

  @Option(
      names = {"--duplicateDetection"},
      description = "Duplicate detection strategy")
  private List<String> duplicateDetectionOptions;

  @Option(
      names = {"--logDroppedDuplicates"},
      description = "Log dropped duplicates")
  private boolean logDroppedDuplicates;

  @Option(
      names = {"--errorOnDroppedDuplicates"},
      description = "Error on dropped duplicates")
  private boolean errorOnDroppedDuplicates;

  /** Mapping from GTFS file name to the entity type handled by that class. */
  private final Map<String, Class<?>> _entityClassesByFilename = new HashMap<>();

  /**
   * If we ever need to register a custom option handler for a specific entity type, we would do it
   * here.
   */
  private final Map<Class<?>, OptionHandler> _optionHandlersByEntityClass = new HashMap<>();

  public static void main(String[] args) {
    int exitCode = new CommandLine(new GtfsMergerMain()).execute(args);
    System.exit(exitCode);
  }

  public GtfsMergerMain() {
    mapEntityClassesToFilenames();
  }

  @Override
  public Integer call() throws Exception {
    if (files == null || files.size() < 2) {
      throw new CommandLine.ParameterException(
          new CommandLine(this), "At least one input file and one output file required");
    }

    GtfsMerger merger = new GtfsMerger();
    processOptions(merger);

    List<File> inputPaths = files.subList(0, files.size() - 1);
    File outputPath = files.getLast();

    merger.run(inputPaths, outputPath);
    return 0;
  }

  private void mapEntityClassesToFilenames() {
    for (Class<?> entityClass : GtfsEntitySchemaFactory.getEntityClasses()) {
      CsvFields csvFields = entityClass.getAnnotation(CsvFields.class);
      if (csvFields == null) {
        continue;
      }
      String filename = csvFields.filename();
      _entityClassesByFilename.put(filename, entityClass);
    }
  }

  private void processOptions(GtfsMerger merger) {
    if (fileOptions == null || fileOptions.isEmpty()) {
      return;
    }

    for (int i = 0; i < fileOptions.size(); i++) {
      String filename = fileOptions.get(i);
      Class<?> entityClass = _entityClassesByFilename.get(filename);
      if (entityClass == null) {
        throw new IllegalStateException("unknown GTFS filename: " + filename);
      }

      AbstractEntityMergeStrategy mergeStrategy =
          getMergeStrategyForEntityClass(entityClass, merger);
      OptionHandler handler = new OptionHandler();

      // Apply duplicate detection if specified for this file index
      if (duplicateDetectionOptions != null && i < duplicateDetectionOptions.size()) {
        handler.handleDuplicateDetection(duplicateDetectionOptions.get(i), mergeStrategy);
      }

      // Apply log dropped duplicates if specified
      if (logDroppedDuplicates) {
        handler.handleLogDroppedDuplicates(mergeStrategy);
      }

      // Apply error on dropped duplicates if specified
      if (errorOnDroppedDuplicates) {
        handler.handleErrorOnDroppedDuplicates(mergeStrategy);
      }
    }
  }

  private AbstractEntityMergeStrategy getMergeStrategyForEntityClass(
      Class<?> entityClass, GtfsMerger merger) {
    EntityMergeStrategy strategy = merger.getEntityMergeStrategyForEntityType(entityClass);
    if (strategy == null) {
      throw new IllegalStateException("no merge strategy found for entityType=" + entityClass);
    }
    return (AbstractEntityMergeStrategy) strategy;
  }
}

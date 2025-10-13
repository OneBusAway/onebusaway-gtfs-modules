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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs_merge.strategies.AbstractEntityMergeStrategy;
import org.onebusaway.gtfs_merge.strategies.EDuplicateDetectionStrategy;
import org.onebusaway.gtfs_merge.strategies.ELogDuplicatesStrategy;
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
  List<File> files;

  @Option(
      names = {"--file"},
      description = "GTFS file name to configure")
  List<String> fileOptions;

  @Option(
      names = {"--duplicateDetection"},
      description = "Duplicate detection strategy")
  List<String> duplicateDetectionOptions;

  @Option(
      names = {"--logDroppedDuplicates"},
      description = "Log dropped duplicates")
  boolean logDroppedDuplicates;

  @Option(
      names = {"--errorOnDroppedDuplicates"},
      description = "Error on dropped duplicates")
  boolean errorOnDroppedDuplicates;

  /** Mapping from GTFS file name to the entity type handled by that class. */
  private final Map<String, Class<?>> entityClassesByFilename = new HashMap<>();

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

    var merger = buildMerger();

    ToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);
    System.out.println(merger.toString());

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
      entityClassesByFilename.put(filename, entityClass);
    }
  }

  private GtfsMerger buildMerger() {
    var merger = new GtfsMerger();

    for (int i = 0; i < fileOptions.size(); i++) {
      String filename = fileOptions.get(i);
      Class<?> entityClass = entityClassesByFilename.get(filename);
      if (entityClass == null) {
        throw new IllegalStateException("unknown GTFS filename: " + filename);
      }

      AbstractEntityMergeStrategy mergeStrategy =
          getMergeStrategyForEntityClass(entityClass, merger);

      // Apply duplicate detection if specified for this file index
      if (duplicateDetectionOptions != null && i < duplicateDetectionOptions.size()) {
        mergeStrategy.setDuplicateDetectionStrategy(
            EDuplicateDetectionStrategy.valueOf(duplicateDetectionOptions.get(i).toUpperCase()));
      }

      // Apply log dropped duplicates if specified
      if (logDroppedDuplicates) {
        mergeStrategy.setLogDuplicatesStrategy(ELogDuplicatesStrategy.WARNING);
      }

      // Apply error on dropped duplicates if specified
      if (errorOnDroppedDuplicates) {
        mergeStrategy.setLogDuplicatesStrategy(ELogDuplicatesStrategy.ERROR);
      }
    }
    return merger;
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

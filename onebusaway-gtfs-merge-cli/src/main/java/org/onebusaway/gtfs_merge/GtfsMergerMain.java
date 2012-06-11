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
package org.onebusaway.gtfs_merge;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs_merge.strategies.AbstractEntityMergeStrategy;
import org.onebusaway.gtfs_merge.strategies.EntityMergeStrategy;

public class GtfsMergerMain {

  public static final String ARG_FILE = "file";

  public static final String ARG_IDENTITY_DUPLICATES = "identityDuplicates";

  public static final String ARG_FUZZY_DUPLICATES = "fuzzyDuplicates";

  public static final String ARG_RENAME_DUPLICATES = "renameDuplicates";

  public static final String ARG_LOG_DROPPED_DUPLICATES = "logDroppedDuplicates";

  public static final String ARG_ERROR_ON_DROPPED_DUPLICATES = "errorOnDroppedDuplicates";

  /****
   * Generic Arguments
   ****/

  private static CommandLineParser _parser = new PosixParser();

  private Options _options = new Options();

  private Map<String, Class<?>> _entityClassesByFilename = new HashMap<String, Class<?>>();

  private Map<Class<?>, OptionHandler> _optionHandlersByEntityClass = new HashMap<Class<?>, OptionHandler>();

  public static void main(String[] args) throws IOException {
    GtfsMergerMain m = new GtfsMergerMain();
    m.run(args);
  }

  public GtfsMergerMain() {
    buildOptions(_options);
    mapEntityClassesToFilenames();
  }

  /*****************************************************************************
   * {@link Runnable} Interface
   ****************************************************************************/

  public void run(String[] args) throws IOException {

    if (needsHelp(args)) {
      printHelp();
      System.exit(0);
    }

    try {
      CommandLine cli = _parser.parse(_options, args, true);
      runApplication(cli, args);
    } catch (MissingOptionException ex) {
      System.err.println("Missing argument: " + ex.getMessage());
      printHelp();
    } catch (MissingArgumentException ex) {
      System.err.println("Missing argument: " + ex.getMessage());
      printHelp();
    } catch (UnrecognizedOptionException ex) {
      System.err.println("Unknown argument: " + ex.getMessage());
      printHelp();
    } catch (AlreadySelectedException ex) {
      System.err.println("Argument already selected: " + ex.getMessage());
      printHelp();
    } catch (ParseException ex) {
      System.err.println(ex.getMessage());
      printHelp();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /*****************************************************************************
   * Abstract Methods
   ****************************************************************************/

  protected void buildOptions(Options options) {
    options.addOption(ARG_FILE, true, "GTFS file name");
    options.addOption(ARG_IDENTITY_DUPLICATES, false,
        "use ids for detecting duplicates");
    options.addOption(ARG_FUZZY_DUPLICATES, false,
        "attempt fuzzy duplicate detection");
    options.addOption(ARG_RENAME_DUPLICATES, false, "rename duplicate entities");
    options.addOption(ARG_LOG_DROPPED_DUPLICATES, false,
        "log dropped duplicates");
    options.addOption(ARG_ERROR_ON_DROPPED_DUPLICATES, false,
        "error on dropped duplicates");
  }

  protected void printHelp(PrintWriter out, Options options) throws IOException {

    InputStream is = getClass().getResourceAsStream("usage.txt");
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String line = null;

    while ((line = reader.readLine()) != null) {
      System.err.println(line);
    }

    reader.close();
  }

  protected void runApplication(CommandLine cli, String[] originalArgs)
      throws Exception {

    String[] args = cli.getArgs();

    if (args.length < 2) {
      printHelp();
      System.exit(-1);
    }

    GtfsMerger merger = new GtfsMerger();

    processOptions(cli, merger);

    List<File> inputPaths = new ArrayList<File>();
    for (int i = 0; i < args.length - 1; ++i) {
      inputPaths.add(new File(args[i]));
    }
    File outputPath = new File(args[args.length - 1]);

    merger.run(inputPaths, outputPath);
  }

  /*****************************************************************************
   * Protected Methods
   ****************************************************************************/

  protected void printHelp() throws IOException {
    printHelp(new PrintWriter(System.err, true), _options);
  }

  private boolean needsHelp(String[] args) {
    for (String arg : args) {
      if (arg.equals("-h") || arg.equals("--help") || arg.equals("-help"))
        return true;
    }
    return false;
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

  private void processOptions(CommandLine cli, GtfsMerger merger) {

    OptionHandler currentOptionHandler = null;
    AbstractEntityMergeStrategy mergeStrategy = null;

    for (Option option : cli.getOptions()) {
      if (option.getOpt().equals(ARG_FILE)) {
        String filename = option.getValue();
        Class<?> entityClass = _entityClassesByFilename.get(filename);
        if (entityClass == null) {
          throw new IllegalStateException("unknown GTFS filename: " + filename);
        }
        mergeStrategy = registerMergeStrategyForEntityClass(entityClass, merger);
        currentOptionHandler = getOptionHandlerForEntityClass(entityClass);
      } else {
        if (currentOptionHandler == null) {
          throw new IllegalArgumentException(
              "you must specify a --file argument first before specifying file-specific arguments");
        }
        currentOptionHandler.handleOption(option, mergeStrategy);
      }
    }
  }

  private AbstractEntityMergeStrategy registerMergeStrategyForEntityClass(
      Class<?> entityClass, GtfsMerger merger) {
    try {
      String name = entityClass.getName();
      int index = name.lastIndexOf('.');
      if (index != -1) {
        name = name.substring(index + 1);
      }
      String mergeStrategyClassName = "org.onebusaway.gtfs_merge.strategies."
          + name + "MergeStrategy";
      Class<?> mergeStrategyClass = Class.forName(mergeStrategyClassName);
      AbstractEntityMergeStrategy strategy = (AbstractEntityMergeStrategy) mergeStrategyClass.newInstance();

      Method setMethod = merger.getClass().getMethod("set" + name + "Strategy",
          EntityMergeStrategy.class);
      setMethod.invoke(merger, strategy);
      return strategy;
    } catch (Exception ex) {
      throw new IllegalStateException(
          "error creating merge strategy for entity class " + entityClass, ex);
    }
  }

  private OptionHandler getOptionHandlerForEntityClass(Class<?> entityClass) {
    OptionHandler handler = _optionHandlersByEntityClass.get(entityClass);
    if (handler == null) {
      handler = new OptionHandler();
    }
    return handler;
  }

}

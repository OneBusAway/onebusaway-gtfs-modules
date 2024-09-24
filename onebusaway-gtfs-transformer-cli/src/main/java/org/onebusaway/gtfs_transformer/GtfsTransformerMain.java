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
package org.onebusaway.gtfs_transformer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import org.onebusaway.gtfs_transformer.updates.EnsureStopTimesIncreaseUpdateStrategy;
import org.onebusaway.gtfs_transformer.updates.LocalVsExpressUpdateStrategy;
import org.onebusaway.gtfs_transformer.updates.RemoveDuplicateTripsStrategy;
import org.onebusaway.gtfs_transformer.updates.RemoveRepeatedStopTimesStrategy;
import org.onebusaway.gtfs_transformer.updates.TrivialStopTimeInterpolationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GtfsTransformerMain {
  
  private static final Logger LOG = LoggerFactory.getLogger(GtfsTransformerMain.class);

  /****
   * Generic Arguments
   ****/

  private static final String ARG_AGENCY_ID = "agencyId";

  private static final String ARG_MODIFICATIONS = "modifications";

  private static final String ARG_TRANSFORM = "transform";

  private static final String ARG_REFERENCE = "reference"; // reference GTFS

  private static final String ARG_STOP_MAPPING = "stopMapping";

  private static final String ARG_IGNORE_STOPS = "ignoreStops";

  private static final String ARG_REGEX_FILE = "regexFile";

  private static final String ARG_CONTROL_FILE = "controlFile";

  private static final String ARG_CONCURRENCY_FILE = "concurrencyFile";

  private static final String ARG_OMNY_ROUTES_FILE = "omnyRoutesFile";

  private static final String ARG_OMNY_STOPS_FILE = "omnyStopsFile";

  private static final String ARG_VERIFY_ROUTES_FILE = "verifyRoutesFile";

  private static final String ARG_LOCAL_VS_EXPRESS = "localVsExpress";

  private static final String ARG_CHECK_STOP_TIMES = "checkStopTimes";

  private static final String ARG_REMOVE_REPEATED_STOP_TIMES = "removeRepeatedStopTimes";

  private static final String INTERPOLATE_REPEATED_STOP_TIMES = "interpolateRepeatedStopTimes";

  private static final String ARG_REMOVE_DUPLICATE_TRIPS = "removeDuplicateTrips";

  private static final String ARG_OVERWRITE_DUPLICATES = "overwriteDuplicates";

  private static final CommandLineParser parser = new PosixParser();

  private final Options options = new Options();

  public static void main(String[] args) throws IOException {
    GtfsTransformerMain m = new GtfsTransformerMain();
    m.run(args);
  }

  public GtfsTransformerMain() {

    buildOptions(options);
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
      CommandLine cli = parser.parse(options, args, true);
      runApplication(cli, args);
    } catch (MissingOptionException | MissingArgumentException ex) {
      System.err.println("Missing argument: " + ex.getMessage());
      printHelp();
      System.exit(-2);
    } catch (UnrecognizedOptionException ex) {
      System.err.println("Unknown argument: " + ex.getMessage());
      printHelp();
      System.exit(-2);
    } catch (AlreadySelectedException ex) {
      System.err.println("Argument already selected: " + ex.getMessage());
      printHelp();
      System.exit(-2);
    } catch (ParseException ex) {
      System.err.println(ex.getMessage());
      printHelp();
      System.exit(-2);
    } catch (TransformSpecificationException ex) {
      System.err.println("error with transform line: " + ex.getLine());
      System.err.println(ex.getMessage());
      System.exit(-1);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(-1);
    }
  }

  /*****************************************************************************
   * Abstract Methods
   ****************************************************************************/

  protected void buildOptions(Options options) {

    options.addOption(ARG_AGENCY_ID, true, "agency id");

    options.addOption(ARG_MODIFICATIONS, true, "data modifications");
    options.addOption(ARG_TRANSFORM, true, "data transformation");
    options.addOption(ARG_REFERENCE, true, "reference GTFS to merge from");
    options.addOption(ARG_STOP_MAPPING, true, "Stop Name Mapping File");
    options.addOption(ARG_IGNORE_STOPS, true, "List of stops names to ignore");
    options.addOption(ARG_REGEX_FILE, true, "Regex pattern mapping file");
    options.addOption(ARG_CONTROL_FILE, true, "file to remap stop ids and other properties");
    options.addOption(ARG_CONCURRENCY_FILE, true, "file to remap wrong way concurrencies");
    options.addOption(ARG_OMNY_ROUTES_FILE, true, "file to add OMNY enabled routes to GTFS");
    options.addOption(ARG_OMNY_STOPS_FILE, true, "file to add OMNY enabled stops to GTFS");
    options.addOption(ARG_VERIFY_ROUTES_FILE, true, "file to check route names vs route ids in GTFS");

    options.addOption(ARG_LOCAL_VS_EXPRESS, false,
        "add additional local vs express fields");
    options.addOption(ARG_CHECK_STOP_TIMES, false,
        "check stop times are in order");
    options.addOption(ARG_REMOVE_REPEATED_STOP_TIMES, false,
        "remove repeated stop times");
    options.addOption(ARG_REMOVE_DUPLICATE_TRIPS, false,
        "remove duplicate trips");
    options.addOption(ARG_OVERWRITE_DUPLICATES, false,
        "overwrite duplicate elements");
  }

  private void printHelp() throws IOException {

    InputStream is = getClass().getResourceAsStream("usage.txt");
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String line;

    while ((line = reader.readLine()) != null) {
      System.err.println(line);
    }

    reader.close();
  }

  protected void runApplication(CommandLine cli, String[] originalArgs)
      throws Exception {

    var args = Arrays.stream(cli.getArgs()).toList();

    if (args.size() < 2) {
      printHelp();
      System.exit(-1);
    }
	
    List<File> inputPaths = args.stream().limit(args.size() - 1).map(File::new).toList();
    LOG.info("input paths: {}", inputPaths);

    GtfsTransformer transformer = new GtfsTransformer();
    transformer.setGtfsInputDirectories(inputPaths);

    var outputPath = new File(args.get(args.size() - 1));
    transformer.setOutputDirectory(outputPath);
    LOG.info("output path: {}", outputPath);
    
    Option[] options = getOptionsInCommandLineOrder(cli, originalArgs);

    for (Option option : options) {

      String name = option.getOpt();

      if (name.equals(ARG_REMOVE_REPEATED_STOP_TIMES))
        configureRemoveRepeatedStopTimes(transformer);

      if (name.equals(INTERPOLATE_REPEATED_STOP_TIMES))
        configureInterpolateStopTimes(transformer);

      if (name.equals(ARG_REMOVE_DUPLICATE_TRIPS))
        configureRemoveDuplicateTrips(transformer);

      if (name.equals(ARG_CHECK_STOP_TIMES))
        configureEnsureStopTimesInOrder(transformer);

      if (name.equals(ARG_AGENCY_ID))
        configureAgencyId(transformer, cli.getOptionValue(ARG_AGENCY_ID));

      if (name.equals(ARG_MODIFICATIONS) || name.equals(ARG_TRANSFORM))
        GtfsTransformerLibrary.configureTransformation(transformer,
            option.getValue());

      if (name.equals(ARG_REFERENCE))
        configureAdditionalGTFS(transformer, option.getValue());

      if (name.equals(ARG_STOP_MAPPING)) {
        configureStopMapping(transformer, option.getValue());
      }

      if (name.equals(ARG_IGNORE_STOPS)) {
        configureIgnoreStops(transformer, option.getValue());
      }

      if (name.equals(ARG_REGEX_FILE)) {
        configureRegexFile(transformer, option.getValue());
      }

      if (name.equals(ARG_CONTROL_FILE)) {
        configureControlFile(transformer, option.getValue());
      }

      if (name.equals(ARG_CONCURRENCY_FILE)) {
        configureConcurrencyFile(transformer, option.getValue());
      }

      if (name.equals(ARG_OMNY_ROUTES_FILE)) {
        configureOmnyRoutesFile(transformer, option.getValue());
      }

      if (name.equals(ARG_OMNY_STOPS_FILE)) {
        configureOmnyStopsFile(transformer, option.getValue());
      }

      if (name.equals(ARG_VERIFY_ROUTES_FILE)) {
        configureVerifyRoutesFile(transformer, option.getValue());
      }

      if (name.equals(ARG_LOCAL_VS_EXPRESS))
        configureLocalVsExpressUpdates(transformer);

      if (name.equals(ARG_OVERWRITE_DUPLICATES)) {
        transformer.getReader().setOverwriteDuplicates(true);
      }
    }

    transformer.run();
  }

  private Option[] getOptionsInCommandLineOrder(CommandLine cli,
      String[] originalArgs) {

    Option[] options = cli.getOptions();
    List<Ordered<Option>> orderedOptions = new ArrayList<Ordered<Option>>();

    for (Option option : options) {

      String argName = option.getOpt();
      int optionPosition = originalArgs.length;

      for (int i = 0; i < originalArgs.length; i++) {
        if (originalArgs[i].endsWith(argName)) {
          optionPosition = i;
          break;
        }
      }

      orderedOptions.add(new Ordered<Option>(option, optionPosition));
    }

    Collections.sort(orderedOptions);
    options = new Option[options.length];

    for (int i = 0; i < options.length; i++)
      options[i] = orderedOptions.get(i).getObject();

    return options;
  }

  private void configureRemoveRepeatedStopTimes(GtfsTransformer updater) {
    updater.addTransform(new RemoveRepeatedStopTimesStrategy());
  }

  public void configureInterpolateStopTimes(GtfsTransformer updater) {
    updater.addTransform(new TrivialStopTimeInterpolationStrategy());
  }

  private void configureRemoveDuplicateTrips(GtfsTransformer updater) {
    updater.addTransform(new RemoveDuplicateTripsStrategy());
  }

  private void configureAgencyId(GtfsTransformer updater, String agencyId) {
    if (agencyId != null)
      updater.setAgencyId(agencyId);
  }

  private void configureEnsureStopTimesInOrder(GtfsTransformer updater) {
    updater.addTransform(new EnsureStopTimesIncreaseUpdateStrategy());
  }

  private void configureLocalVsExpressUpdates(GtfsTransformer updater) {
    updater.addTransform(new LocalVsExpressUpdateStrategy());
  }

  private void configureAdditionalGTFS(GtfsTransformer updater,  String path) {
    updater.setGtfsReferenceDirectory(new File(path));
  }

  private void configureStopMapping(GtfsTransformer updater, String file) {
    updater.addParameter("stopMappingFile", file);
  }

  private void configureIgnoreStops(GtfsTransformer updater, String file) {
    updater.addParameter("ignoreStops", file);
  }

  private void configureRegexFile(GtfsTransformer updater, String file) {
    updater.addParameter("regexFile", file);
  }

  private void configureControlFile(GtfsTransformer updater, String file) {
    updater.addParameter("controlFile", file); }

  private void configureConcurrencyFile(GtfsTransformer updater, String file) {
      updater.addParameter("concurrencyFile", file);
  }

  private void configureOmnyRoutesFile(GtfsTransformer updater, String file) {
    updater.addParameter("omnyRoutesFile", file);
  }

  private void configureOmnyStopsFile(GtfsTransformer updater, String file) {
    updater.addParameter("omnyStopsFile", file);
  }

  private void configureVerifyRoutesFile(GtfsTransformer updater, String file) {
    updater.addParameter("verifyRoutesFile", file);
  }

  private boolean needsHelp(String[] args) {
    for (String arg : args) {
      if (arg.equals("-h") || arg.equals("--help") || arg.equals("-help"))
        return true;
    }
    return false;
  }

  private static class Ordered<T> implements Comparable<Ordered<T>> {

    private final T _object;

    private final int _order;

    public Ordered(T object, int order) {
      _object = object;
      _order = order;
    }

    public T getObject() {
      return _object;
    }

    @Override
    public int compareTo(Ordered<T> o) {
      return _order - o._order;
    }
  }
}

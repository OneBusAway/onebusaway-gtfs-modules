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
import java.io.PrintWriter;
import java.util.ArrayList;
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

public class GtfsTransformerMain {

  /****
   * Generic Arguments
   ****/

  private static final String ARG_AGENCY_ID = "agencyId";

  private static final String ARG_MODIFICATIONS = "modifications";

  private static final String ARG_TRANSFORM = "transform";

  private static final String ARG_LOCAL_VS_EXPRESS = "localVsExpress";

  private static final String ARG_CHECK_STOP_TIMES = "checkStopTimes";

  private static final String ARG_REMOVE_REPEATED_STOP_TIMES = "removeRepeatedStopTimes";

  private static final String ARG_REMOVE_DUPLICATE_TRIPS = "removeDuplicateTrips";

  private static CommandLineParser _parser = new PosixParser();

  private Options _options = new Options();

  public static void main(String[] args) throws IOException {
    GtfsTransformerMain m = new GtfsTransformerMain();
    m.run(args);
  }

  public GtfsTransformerMain() {

    buildOptions(_options);
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

    options.addOption(ARG_AGENCY_ID, true, "agency id");

    options.addOption(ARG_MODIFICATIONS, true, "data modifications");
    options.addOption(ARG_TRANSFORM, true, "data transformation");

    options.addOption(ARG_LOCAL_VS_EXPRESS, false,
        "add additional local vs express fields");
    options.addOption(ARG_CHECK_STOP_TIMES, false,
        "check stop times are in order");
    options.addOption(ARG_REMOVE_REPEATED_STOP_TIMES, false,
        "remove repeated stop times");
    options.addOption(ARG_REMOVE_DUPLICATE_TRIPS, false,
        "remove duplicate trips");
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

    if (args.length != 2) {
      printHelp();
      System.exit(-1);
    }

    GtfsTransformer updater = new GtfsTransformer();
    updater.setGtfsInputDirectory(new File(args[0]));
    updater.setOutputDirectory(new File(args[1]));

    Option[] options = getOptionsInCommandLineOrder(cli, originalArgs);

    for (Option option : options) {

      String name = option.getOpt();

      if (name.equals(ARG_REMOVE_REPEATED_STOP_TIMES))
        configureRemoveRepeatedStopTimes(updater);

      if (name.equals(ARG_REMOVE_DUPLICATE_TRIPS))
        configureRemoveDuplicateTrips(updater);

      if (name.equals(ARG_CHECK_STOP_TIMES))
        configureEnsureStopTimesInOrder(updater);

      if (name.equals(ARG_AGENCY_ID))
        configureAgencyId(updater, cli.getOptionValue(ARG_AGENCY_ID));

      if (name.equals(ARG_MODIFICATIONS) || name.equals(ARG_TRANSFORM))
        GtfsTransformerLibrary.configureTransformation(updater,
            option.getValue());

      if (name.equals(ARG_LOCAL_VS_EXPRESS))
        configureLocalVsExpressUpdates(updater);
    }

    updater.run();
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

  private static class Ordered<T> implements Comparable<Ordered<T>> {

    private T _object;

    private int _order;

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

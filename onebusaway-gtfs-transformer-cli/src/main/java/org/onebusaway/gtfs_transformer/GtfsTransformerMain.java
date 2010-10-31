package org.onebusaway.gtfs_transformer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
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
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs_transformer.factory.TransformFactory;
import org.onebusaway.gtfs_transformer.king_county_metro.model.PatternPair;
import org.onebusaway.gtfs_transformer.king_county_metro.transforms.CalendarUpdateStrategy;
import org.onebusaway.gtfs_transformer.king_county_metro.transforms.DeduplicateRoutesStrategy;
import org.onebusaway.gtfs_transformer.king_county_metro.transforms.DeduplicateStopsStrategy;
import org.onebusaway.gtfs_transformer.king_county_metro.transforms.DeprecatedFieldsUpdaterStrategy;
import org.onebusaway.gtfs_transformer.king_county_metro.transforms.PatternPairUpdateStrategy;
import org.onebusaway.gtfs_transformer.king_county_metro.transforms.RemoveMergedTripsStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.updates.DeduplicateTripsStrategy;
import org.onebusaway.gtfs_transformer.updates.EnsureStopTimesIncreaseUpdateStrategy;
import org.onebusaway.gtfs_transformer.updates.LocalVsExpressUpdateStrategy;
import org.onebusaway.gtfs_transformer.updates.RemoveDuplicateTripsStrategy;
import org.onebusaway.gtfs_transformer.updates.RemoveEmptyBlockTripsStrategy;
import org.onebusaway.gtfs_transformer.updates.RemoveRepeatedStopTimesStrategy;
import org.onebusaway.gtfs_transformer.updates.StopNameUpdateStrategyFactory;
import org.onebusaway.gtfs_transformer.updates.TripScheduleModificationFactoryBean;
import org.onebusaway.gtfs_transformer.updates.TripScheduleModificationStrategy;

public class GtfsTransformerMain {

  /****
   * Generic Arguments
   ****/

  private static final String ARG_AGENCY_ID = "agencyId";

  private static final String ARG_MODIFICATIONS = "modifications";

  private static final String ARG_TRANSFORM = "transform";

  private static final String ARG_STOP_NAMES = "stopNames";

  private static final String ARG_LOCAL_VS_EXPRESS = "localVsExpress";

  private static final String ARG_CHECK_STOP_TIMES = "checkStopTimes";

  private static final String ARG_REMOVE_REPEATED_STOP_TIMES = "removeRepeatedStopTimes";

  private static final String ARG_REMOVE_DUPLICATE_TRIPS = "removeDuplicateTrips";

  /****
   * Metro KC Arguments
   ****/

  private static final String ARG_KCMETRO_DATA = "kcmetroData";

  private static final String ARG_CALENDAR_MOD = "calendarModifications";

  private static final String ARG_INTERLINED_ROUTES = "interlinedRoutes";

  private static final String ARG_TIMEPOINT_TO_STOP_MAPPING = "timepointToStopMapping";

  private static final String ARG_TIMEPOINT_MAPPING = "timepointMapping";

  private static final String ARG_DEPRECATED_FIELDS = "deprecatedFields";

  private static final String ARG_DEDUPLICATE_STOPS = "deduplicateStops";

  private static final String ARG_DEDUPLICATE_ROUTES = "deduplicateRoutes";

  private static final String ARG_DEDUPLICATE_TRIPS = "deduplicateTrips";

  private static final String ARG_KCMETRO_DEFAULTS = "kcmetroDefaults";

  private static CommandLineParser _parser = new PosixParser();

  private Options _options = new Options();

  private TransformFactory _modificationFactory = new TransformFactory();

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
    options.addOption(ARG_CALENDAR_MOD, true, "calendar modifications");
    options.addOption(ARG_STOP_NAMES, true, "stop-name overrides");

    options.addOption(ARG_MODIFICATIONS, true, "data modifications");
    options.addOption(ARG_TRANSFORM, true, "data transformation");

    options.addOption(ARG_INTERLINED_ROUTES, false, "fix interlined routes");
    options.addOption(ARG_LOCAL_VS_EXPRESS, false,
        "add additional local vs express fields");
    options.addOption(ARG_TIMEPOINT_TO_STOP_MAPPING, false,
        "generate the timepoint to stop mapping");
    options.addOption(ARG_TIMEPOINT_MAPPING, false,
        "generate the timepoint mapping");
    options.addOption(ARG_DEPRECATED_FIELDS, false, "deprecated fields");
    options.addOption(ARG_CHECK_STOP_TIMES, false,
        "check stop times are in order");
    options.addOption(ARG_DEDUPLICATE_STOPS, false, "deduplicate stops");
    options.addOption(ARG_DEDUPLICATE_ROUTES, false, "deduplicate routes");
    options.addOption(ARG_REMOVE_REPEATED_STOP_TIMES, false,
        "remove repeated stop times");
    options.addOption(ARG_DEDUPLICATE_TRIPS, false, "deduplicate trips");
    options.addOption(ARG_KCMETRO_DEFAULTS, false, "use default settings");
    options.addOption(ARG_KCMETRO_DATA, true, "kcmetro data directory");
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

      if (name.equals(ARG_KCMETRO_DATA))
        updater.setDataInputDirectory(new File(
            cli.getOptionValue(ARG_KCMETRO_DATA)));

      if (name.equals(ARG_KCMETRO_DEFAULTS))
        configureKCMetroDefaults(updater);

      if (name.equals(ARG_DEDUPLICATE_STOPS))
        configureDeduplicateStops(updater);

      if (name.equals(ARG_DEDUPLICATE_ROUTES))
        configureDeduplicateRoutes(updater);

      if (name.equals(ARG_REMOVE_REPEATED_STOP_TIMES))
        configureRemoveRepeatedStopTimes(updater);

      if (name.equals(ARG_REMOVE_DUPLICATE_TRIPS))
        configureRemoveDuplicateTrips(updater);

      if (name.equals(ARG_DEDUPLICATE_TRIPS))
        configureDeduplicateTrips(updater);

      if (name.equals(ARG_CHECK_STOP_TIMES))
        configureEnsureStopTimesInOrder(updater);

      if (name.equals(ARG_AGENCY_ID))
        configureAgencyId(updater, cli.getOptionValue(ARG_AGENCY_ID));

      if (name.equals(ARG_CALENDAR_MOD))
        configureCalendarUpdates(updater, cli.getOptionValue(ARG_CALENDAR_MOD));

      if (name.equals(ARG_STOP_NAMES))
        configureStopNameUpdates(updater, cli.getOptionValue(ARG_STOP_NAMES));

      if (name.equals(ARG_MODIFICATIONS) || name.equals(ARG_TRANSFORM))
        configureTransformation(updater, option.getValue());

      if (name.equals(ARG_INTERLINED_ROUTES))
        configureInterlinedRoutesUpdates(updater);

      if (name.equals(ARG_LOCAL_VS_EXPRESS))
        configureLocalVsExpressUpdates(updater);

      if (name.equals(ARG_DEPRECATED_FIELDS))
        configureDeprecatedFields(updater);
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

  private void configureKCMetroDefaults(GtfsTransformer updater)
      throws IOException {

    updater.setAgencyId("KCM");

    updater.addTransform(new RemoveMergedTripsStrategy());

    configureDeduplicateStops(updater);
    configureDeduplicateRoutes(updater);
    configureRemoveRepeatedStopTimes(updater);
    configureRemoveEmptyBlockTrips(updater);

    // configureTripBlockIds(updater);
    configureEnsureStopTimesInOrder(updater);

    configureCalendarUpdates(
        updater,
        "http://onebusaway.googlecode.com/svn/wiki/KingCountyMetroCalendarModifications.wiki");
    configureStopNameUpdates(
        updater,
        "http://onebusaway.googlecode.com/svn/wiki/KingCountyMetroStopNameModifications.wiki");
    configureTransformation(updater,
        "http://onebusaway.googlecode.com/svn/wiki/KingCountyMetroModifications.wiki");

    configureInterlinedRoutesUpdates(updater);
    configureLocalVsExpressUpdates(updater);
  }

  private void configureDeduplicateStops(GtfsTransformer updater) {
    updater.addTransform(new DeduplicateStopsStrategy());
  }

  private void configureDeduplicateRoutes(GtfsTransformer updater) {
    updater.addTransform(new DeduplicateRoutesStrategy());
  }

  private void configureRemoveRepeatedStopTimes(GtfsTransformer updater) {
    updater.addTransform(new RemoveRepeatedStopTimesStrategy());
  }

  private void configureRemoveDuplicateTrips(GtfsTransformer updater) {
    updater.addTransform(new RemoveDuplicateTripsStrategy());
  }

  private void configureRemoveEmptyBlockTrips(GtfsTransformer updater) {
    updater.addTransform(new RemoveEmptyBlockTripsStrategy());
  }

  private void configureDeduplicateTrips(GtfsTransformer updater) {
    updater.addTransform(new DeduplicateTripsStrategy());
  }

  private void configureAgencyId(GtfsTransformer updater, String agencyId) {
    if (agencyId != null)
      updater.setAgencyId(agencyId);
  }

  private void configureEnsureStopTimesInOrder(GtfsTransformer updater) {
    updater.addTransform(new EnsureStopTimesIncreaseUpdateStrategy());
  }

  private void configureCalendarUpdates(GtfsTransformer updater, String path)
      throws IOException {

    if (path == null)
      return;

    CalendarUpdateStrategy updateStrategy = new CalendarUpdateStrategy();

    TripScheduleModificationFactoryBean factory = new TripScheduleModificationFactoryBean();
    factory.setPath(path);

    TripScheduleModificationStrategy modification = factory.createModificationStrategy();
    updateStrategy.addModificationStrategy(modification);

    updater.addTransform(updateStrategy);
  }

  private void configureStopNameUpdates(GtfsTransformer updater, String path)
      throws IOException {

    if (path == null)
      return;

    StopNameUpdateStrategyFactory factory = new StopNameUpdateStrategyFactory();

    if (path.startsWith("http")) {
      GtfsTransformStrategy strategy = factory.createFromUrl(new URL(path));
      updater.addTransform(strategy);
    } else {
      GtfsTransformStrategy strategy = factory.createFromFile(new File(path));
      updater.addTransform(strategy);
    }
  }

  private void configureTransformation(GtfsTransformer updater, String path)
      throws IOException {

    if (path == null)
      return;

    if (path.startsWith("http")) {
      _modificationFactory.addModificationsFromUrl(updater, new URL(path));
    } else if (path.startsWith("json:")) {
      _modificationFactory.addModificationsFromString(updater,
          path.substring("json:".length()));
    } else {
      _modificationFactory.addModificationsFromFile(updater, new File(path));
    }
  }

  private void configureInterlinedRoutesUpdates(GtfsTransformer updater) {
    GtfsReader reader = updater.getReader();
    reader.getEntityClasses().add(PatternPair.class);
    updater.addTransform(new PatternPairUpdateStrategy());
  }

  private void configureLocalVsExpressUpdates(GtfsTransformer updater) {
    updater.addTransform(new LocalVsExpressUpdateStrategy());
  }

  private void configureDeprecatedFields(GtfsTransformer updater) {
    DeprecatedFieldsUpdaterStrategy strategy = new DeprecatedFieldsUpdaterStrategy();
    updater.addTransform(strategy);
    updater.addOutputSchemaUpdate(strategy);

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

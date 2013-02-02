/**
 * Copyright (C) 2013 Google, Inc.
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
package org.onebusaway.gtfs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.HibernateGtfsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GtfsDatabaseLoaderMain {

  private static Logger _log = LoggerFactory.getLogger(GtfsDatabaseLoaderMain.class);

  private static final String ARG_DRIVER_CLASS = "driverClass";

  private static final String ARG_URL = "url";

  private static final String ARG_USERNAME = "username";

  private static final String ARG_PASSWORD = "password";

  public static void main(String[] args) throws IOException {
    GtfsDatabaseLoaderMain m = new GtfsDatabaseLoaderMain();
    m.run(args);
  }

  private void run(String[] args) throws IOException {
    CommandLine cli = parseCommandLineOptions(args);

    args = cli.getArgs();
    if (args.length != 1) {
      printUsage();
      System.exit(-1);
    }

    Configuration config = new Configuration();
    config.setProperty("hibernate.connection.driver_class",
        cli.getOptionValue(ARG_DRIVER_CLASS));
    config.setProperty("hibernate.connection.url", cli.getOptionValue(ARG_URL));
    if (cli.hasOption(ARG_USERNAME)) {
      config.setProperty("hibernate.connection.username",
          cli.getOptionValue(ARG_USERNAME));
    }
    if (cli.hasOption(ARG_PASSWORD)) {
      config.setProperty("hibernate.connection.password",
          cli.getOptionValue(ARG_PASSWORD));
    }
    config.setProperty("hibernate.connection.pool_size", "1");
    config.setProperty("hibernate.cache.provider_class",
        "org.hibernate.cache.NoCacheProvider");
    config.setProperty("hibernate.hbm2ddl.auto", "update");
    config.addResource("org/onebusaway/gtfs/model/GtfsMapping.hibernate.xml");
    config.addResource("org/onebusaway/gtfs/impl/HibernateGtfsRelationalDaoImpl.hibernate.xml");

    SessionFactory sessionFactory = config.buildSessionFactory();
    HibernateGtfsFactory factory = new HibernateGtfsFactory(sessionFactory);

    GtfsReader reader = new GtfsReader();
    reader.setInputLocation(new File(args[0]));

    GtfsMutableRelationalDao dao = factory.getDao();
    reader.setEntityStore(dao);
    reader.run();
    reader.close();
  }

  private CommandLine parseCommandLineOptions(String[] args) {
    try {
      Options options = new Options();
      buildOptions(options);
      Parser parser = new PosixParser();
      return parser.parse(options, args);
    } catch (ParseException e) {
      System.err.println(e.getMessage());
      printUsage();
      System.exit(-1);
      return null;
    }
  }

  protected void buildOptions(Options options) {
    Option driverClassOption = new Option(ARG_DRIVER_CLASS, true,
        "JDBC driverClass");
    driverClassOption.setRequired(true);
    options.addOption(driverClassOption);

    Option urlOption = new Option(ARG_URL, true, "JDBC url");
    urlOption.setRequired(true);
    options.addOption(urlOption);

    options.addOption(ARG_USERNAME, true, "JDBC username");
    options.addOption(ARG_PASSWORD, true, "JDBC password");
  }

  protected void printUsage() {
    InputStream in = getClass().getResourceAsStream("usage.txt");
    if (in == null) {
      _log.error("could not find usage.txt resource");
      System.exit(-1);
    }
    BufferedReader reader = null;

    try {
      reader = new BufferedReader(new InputStreamReader(in));
      String line = null;
      while ((line = reader.readLine()) != null) {
        System.err.println(line);
      }
    } catch (IOException ex) {
      _log.error("error reading usage.txt resource", ex);
      System.exit(-1);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ex) {
          _log.error("error closing usage.txt resource", ex);
          System.exit(-1);
        }
      }
    }
  }
}

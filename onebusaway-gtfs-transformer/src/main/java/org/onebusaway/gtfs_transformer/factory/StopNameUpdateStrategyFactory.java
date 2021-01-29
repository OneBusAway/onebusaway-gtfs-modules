package org.onebusaway.gtfs_transformer.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopNameUpdateStrategyFactory {

    private static Logger _log = LoggerFactory.getLogger(StopNameUpdateStrategyFactory.class);

    public GtfsTransformStrategy createFromUrl(URL url) throws IOException {
        InputStream in = url.openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        return create(reader);
    }

    public GtfsTransformStrategy createFromFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        return create(reader);
    }

    public GtfsTransformStrategy create(BufferedReader reader) throws IOException {

        StopNameUpdateStrategy strategy = new StopNameUpdateStrategy();
        String line = null;
        int lineNumber = 1;

        while ((line = reader.readLine()) != null) {

            line = line.trim();

            if( line.length() == 0 || line.startsWith("#") || line.equals("{{{") || line.equals("}}}") )
                continue;

            int index = line.indexOf(',');
            if (index == -1)
                throw new IllegalArgumentException("invalid line [#" + lineNumber
                        + "]: " + line);
            String key = line.substring(0, index);
            String value = line.substring(index + 1);
            try {
                int stopId = Integer.parseInt(key);
                strategy.addName(stopId, value);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("invalid stop number at line #"
                        + lineNumber + ": " + key);
            }
            lineNumber++;
        }

        reader.close();

        return strategy;
    }

    private class StopNameUpdateStrategy implements GtfsTransformStrategy {

        @Override
        public String getName() {
            return this.getClass().getSimpleName();
        }

        private Map<Integer, String> _stopNamesByStopId = new HashMap<Integer, String>();

        public void addName(Integer stopId, String name) {
            _stopNamesByStopId.put(stopId, name);
        }

        @Override
        public void run(TransformContext context, GtfsMutableRelationalDao dao) {
            for (Map.Entry<Integer, String> entry : _stopNamesByStopId.entrySet()) {
                AgencyAndId id = new AgencyAndId("KCM",
                        Integer.toString(entry.getKey()));
                String name = entry.getValue();
                Stop stop = dao.getStopForId(id);
                if (stop == null) {
                    _log.warn("stop not found for renaming: id=" + id + " newName="
                            + name);
                    continue;
                }
                stop.setName(name);
            }
        }
    }

}
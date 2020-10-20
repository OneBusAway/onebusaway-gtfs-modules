/**
 * Copyright (C) 2020 Cambridge Systematics, Inc. 
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
package org.onebusaway.gtfs_transformer.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiskCache {

	private static Logger _log = LoggerFactory.getLogger(DiskCache.class);

	private static File createOrGetCacheStore() {
		File cacheStore = new File("_cache");
		
		if(!cacheStore.exists()) {
			_log.info("Creating disk backed cache storage at {}", cacheStore.getAbsolutePath());
			cacheStore.mkdirs();
		} else {
			_log.info("Found disk backed cache storage at {}", cacheStore.getAbsolutePath());
		}
		
		return cacheStore;
	}
	
	public static boolean get(CacheKey key, File output) throws IOException {
		File cacheStore = createOrGetCacheStore();
		
		File cachedResult = Paths.get(cacheStore.getAbsolutePath(), key.getKey()).toFile();

		_log.info("Looking for {} in cache...", key.getKey());
		
		if(cachedResult.exists()) {
			_log.info("Copying cached result {} to {}", cachedResult, output.toPath());
			Files.copy(cachedResult.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return true;
		} else {
			_log.info("{} not found", cachedResult);
			return false;
		}
	}
	
	public static void put(CacheKey key, File input) throws Exception {
		File cacheStore = createOrGetCacheStore();
		
		if(!input.isFile() || !input.exists()) {
			throw new Exception("Input to cache does not exist or is not a file.");
		}

		File destinationPath = Paths.get(cacheStore.getPath(), key.getKey()).toFile();

		_log.info("Copying cached result {} to {}", input.toPath(), destinationPath.getAbsolutePath());
		Files.copy(input.toPath(), destinationPath.toPath());
	}
}
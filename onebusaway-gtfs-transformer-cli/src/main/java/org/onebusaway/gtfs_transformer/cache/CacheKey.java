
package org.onebusaway.gtfs_transformer.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
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

public class CacheKey {

	private static Logger _log = LoggerFactory.getLogger(CacheKey.class);

	private MessageDigest md = MessageDigest.getInstance("MD5");

	private String key;
	
	private void walkJSON(JSONObject o, List<File> files) throws JSONException {
		Iterator e = o.keys();
		while(e.hasNext()) {
			String attributeName = (String) e.next();
			Object value = o.get(attributeName);

			if(value instanceof JSONObject) {
				walkJSON((JSONObject)value, files);

			} else if(value instanceof String) {
				File candidateFile = new File((String)value);
				if(candidateFile != null && candidateFile.exists() && candidateFile.isFile()) {
					files.add(candidateFile);
				}
			}
		}
	}

	public List<File> findInputsInJSON(File file) throws FileNotFoundException, JSONException {		
		ArrayList<File> files = new ArrayList<File>();

		Scanner reader = new Scanner(file);
		while (reader.hasNextLine()) {
			String line = reader.nextLine();
			if(line.startsWith("#") || line.length() == 0)
				continue;

			JSONObject root = new JSONObject(line);
			walkJSON(root, files);
		}

		return files;
	}	

	public CacheKey(CommandLine cli, String[] originalArgs) throws JSONException, IOException, NoSuchAlgorithmException {

		List<File> files = new ArrayList<File>();

		// find filenames in arguments
		int a = 0;
		for(String arg : cli.getArgs()) {
			if(a == cli.getArgs().length - 1) {
				_log.info("Removing presumed output from input list " + arg);
				continue;
			}

			File candidateFile = new File(arg);
			if(candidateFile != null && candidateFile.exists() && candidateFile.isFile()) {
				files.add(candidateFile);
			}

			a++;
		}

		// look at options for both files and JSON
		for(Option option : cli.getOptions()) {
			if(option == null || option.getValues() == null)
				continue;

			for(String value : option.getValues()) {
				File candidateFile = new File(value);
				if(candidateFile != null && candidateFile.exists() && candidateFile.isFile()) {
					files.add(candidateFile);
				} else {
					try {
						JSONObject root = new JSONObject(value);
						if(root != null)
							walkJSON(root, files);
					} catch(Exception e) {
						// not JSON
						continue;
					}
				}
			}
		}

		// now go through the files and find JSON files--dive into those looking for files
		if(!files.isEmpty()) {
			for(File file : files) {
				if(file.getName().endsWith(".json")) {
					files.addAll(findInputsInJSON(file));
				}
			}
		}

		_log.info("Found input files " + files);

		String cacheKey = String.join("",  originalArgs);

		for(File file : files) {
		    md.update(Files.readAllBytes(file.toPath()));
		    byte[] digest = md.digest();
		    String hash = DatatypeConverter.printHexBinary(digest).toUpperCase();
			_log.info("Hash of input file {} is {}", file.getName(), hash);
						
			cacheKey += hash;
	    }	
		
		

		md.update(cacheKey.getBytes());
		byte[] commandDigest = md.digest();
		String commandHash = DatatypeConverter.printHexBinary(commandDigest).toUpperCase();
		
		_log.info("Hash of inputs is {}", commandHash);
		
		this.key = commandHash;
	}
	
	public String getKey() {
		return key;
	}
}
package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

public class Option {
	
	private static Logger logger = Logger.getLogger(Option.class);
		
	public static String DATABASE_DIR = ".\\database";
	public static String PROJECT_DIR = null;
	
	/**
	 * read from config file and set all its fields
	 * @param filename config file name
	 * @throws IOException if config file cannot be opened
	 */
	public static void readOptions(String filename) throws IOException {
		
		// read config info from file
		BufferedReader in = new BufferedReader(new FileReader(filename));
		while (true) {
			String line = in.readLine();
			if (line == null) {
				break;
			}
			String[] fields = line.split("=", 2);
			if (fields.length < 2) {
				logger.warn("Illegal line in config file: " + line);
				continue;
			}
			String name = fields[0].trim();
			String value = fields[1].trim();
			if (name.equals("database.directory")) {
				DATABASE_DIR = value;
			} else if (name.equals("project.directory")) {
				PROJECT_DIR = value;
			} else {
				logger.warn("Illegal line in config file: " + line);
			}
		}
		in.close();
	}

}

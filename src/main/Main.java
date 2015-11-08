package main;

import java.io.IOException;

import neo4j.Neo4j;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Main {
	
	private static Logger logger = Logger.getLogger(Main.class);

	public static void main(String[] args) {
		
		PropertyConfigurator.configure("log4j.properties");
		
		try {
			Option.readOptions("./config.ini");
		} catch (IOException e) {
			logger.error("Fail to get global configuration.");
			return;
		}
		
		Neo4j neo4j = Neo4j.open(Option.DATABASE_DIR, Neo4j.WRITE);
		neo4j.run(new StoreWorker());
		neo4j.close();
		
		logger.info("Done.");
	}

}

package neo4j;

import java.io.File;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class Neo4j {
	
	private static Logger logger = Logger.getLogger(Neo4j.class);
	
	private final GraphDatabaseService db;

	public static final int WRITE = 0;
	public static final int APPEND = 1;
	
	/**
	 * open embedded database server under the path <code>dir</code>, in the
	 * mode of <code>mode</code>
	 * 
	 * If database opened in mode <code>APPEND</code>, the original data under
	 * the path will be reserved. In mode <code>WRITE</code>, all the original
	 * data will be cleared.
	 * 
	 * @param dir
	 *            the database directory path
	 * @param mode
	 *            database opening mode (<code>WRITE</code> / <code>APPEND</code>)
	 * @return <code>Neo4j</code> class instance
	 */
	public static Neo4j open(String dir, int mode) {
		if (mode == WRITE) {
			deleteDirectory(new File(dir));
		}
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dir);
		logger.info("Database opened in " + dir);
		return new Neo4j(db);
	}

	private Neo4j(GraphDatabaseService db) {
		this.db = db;
	}
	
	private static boolean deleteDirectory(File dir) {
		if (dir.isDirectory()) {
			for (String child : dir.list()) {
				boolean success = deleteDirectory(new File(dir, child));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	/**
	 * let <code>worker</code> do the work for database
	 * <p>
	 * This method calls the <code>workFor</code> method of <code>worker</code>.
	 * Under the normal usage, all the work for database are written in
	 * <code>workFor</code> method.
	 * 
	 */
	public void run(Worker worker) {
		try (Transaction tx = db.beginTx()) {
			worker.work(db);
			tx.success();
		}
	}
	
	/**
	 * close the opened database
	 */
	public void close() {
		db.shutdown();
		logger.info("Database closed");
	}

}

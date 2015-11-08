package neo4j;

import org.neo4j.graphdb.GraphDatabaseService;

public interface Worker {
	void work(GraphDatabaseService db);
}

package main;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;

import ast.ASTCreator;
import ast.Tree;
import graph.Graph;
import neo4j.Worker;

public class StoreWorker implements Worker {

	private static Logger logger = Logger.getLogger(StoreWorker.class);

	@Override
	public void work(GraphDatabaseService db) {
		
		Graph graph = new Graph(db);
		
		ASTCreator creator = new ASTCreator(Option.PROJECT_DIR);
		while (creator.hasNext()) {
			Tree tree = creator.next();
			graph.storeTree(tree);
		}
		
		graph.connectTrees();

		logger.info("Work finished");
	}
}

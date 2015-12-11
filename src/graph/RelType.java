package graph;

import org.neo4j.graphdb.RelationshipType;

public enum RelType implements RelationshipType {
	AST,
	CONN,
	CLASSES,
	BINDING,
	UML,
}

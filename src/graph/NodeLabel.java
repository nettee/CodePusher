package graph;

import org.neo4j.graphdb.Label;

public enum NodeLabel implements Label {
	
	// key labels
	Key,
	TypeKey,
	MethodKey,
	VariableKey,
	
	// general labels
	BodyDeclaration,
	AbstractTypeDeclaration,
	Comment,
	Expression,
	Annatation,
	Name,
	Statement,
	Type,
	VariableDeclaration,
	
	// project
	Project,
}

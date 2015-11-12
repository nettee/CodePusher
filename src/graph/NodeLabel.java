package graph;

import org.neo4j.graphdb.Label;

public enum NodeLabel implements Label {
	
	// binding labels
	Binding,
	TypeBinding,
	MethodBinding,
	VariableBinding,
	
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

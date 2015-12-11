package graph;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import ast.StoreVisitor;
import ast.Tree;
import main.Option;

/**
 * <code>Graph</code> is a model of nodes and relationships in Neo4j database.
 * In this project, all changes to databases should be done by invoking methods
 * in this class, rather than directly by Neo4j's API.
 * <p>
 * <code>Graph</code> stores a (one-one) mapping from ASTNode to Neo4j's Node,
 * and this mapping is essential when setting properties and adding
 * relationships.
 */
public class Graph {

	private static final Logger logger = Logger.getLogger(Graph.class);

	private final GraphDatabaseService db;
//	private final ExecutionEngine engine;
	private final BindingNodeCreator bindingNodeCreator;

	private Map<ASTNode, Node> map = new HashMap<>();
	private List<Node> treeRoots = new ArrayList<>();

	public Graph(GraphDatabaseService db) {
		this.db = db;
//		this.engine = new ExecutionEngine(db);
		this.bindingNodeCreator = new BindingNodeCreator(db);
	}

	public void storeTree(Tree tree) {
		StoreVisitor visitor = new StoreVisitor(this);
		tree.root.accept(visitor);

		Node rootNode = map.get(tree.root);
		rootNode.setProperty("FILENAME", tree.filename);
		treeRoots.add(rootNode);
		logger.info(String.format("Store tree '%s'", tree.filename));
	}

	public void connectTrees() {
		Node project = db.createNode(NodeLabel.Project);
		String projectName = Option.PROJECT_DIR.substring(Option.PROJECT_DIR.lastIndexOf(File.separator) + 1);
		project.setProperty("NAME", projectName);
		for (Node treeRoot : treeRoots) {
			project.createRelationshipTo(treeRoot, RelType.CONN);
		}
		logger.info(String.format("Connect trees to node Project(%s)", projectName));
	}

//	public void connectTypeRelationships() {
//		String query = "match (td1:TypeDeclaration)-[:AST {NAME:\"SUPERCLASS_TYPE\"}]->"
//				+ "(:SimpleType)-->(:Binding)<--(td2:TypeDeclaration)"
//				+ "create (td1)-[:TYPE {NAME:\"EXTENDS\"}]->(td2)";
//		engine.execute(query);
//	}

	/**
	 * create a node and add labels according to the giving ASTNode
	 */
	public Node createNode(ASTNode astNode) {

		Node node = db.createNode();

		// add raw label
		String name = astNode.getClass().getSimpleName();
		if (name.equals("TypeDeclaration")) {
			name = "Class";
		}
		if (name.equals("MethodDeclaration")) {
			name = "Method";
		}
		if (name.equals("FieldDeclaration")) {
			name = "Field";
		}
		node.addLabel(DynamicLabel.label(name));

		// add general label
		if (astNode instanceof BodyDeclaration) {
			node.addLabel(NodeLabel.BodyDeclaration);
		}
		if (astNode instanceof AbstractTypeDeclaration) {
			node.addLabel(NodeLabel.AbstractTypeDeclaration);
		}
		if (astNode instanceof Comment) {
			node.addLabel(NodeLabel.Comment);
		}
		if (astNode instanceof Expression) {
			node.addLabel(NodeLabel.Expression);
		}
		if (astNode instanceof Annotation) {
			node.addLabel(NodeLabel.Annatation);
		}
		if (astNode instanceof Name) {
			node.addLabel(NodeLabel.Name);
		}
		if (astNode instanceof Statement) {
			node.addLabel(NodeLabel.Statement);
		}
		if (astNode instanceof Type) {
			node.addLabel(NodeLabel.Type);
		}
		if (astNode instanceof VariableDeclaration) {
			node.addLabel(NodeLabel.VariableDeclaration);
		}

		// add type binding
		if (astNode instanceof TypeDeclaration) {
			bindingNodeCreator.getBindingNode(node, ((TypeDeclaration) astNode).resolveBinding());
		}
		if (astNode instanceof SimpleType) {
			ITypeBinding binding = ((SimpleType) astNode).resolveBinding();
			if (!binding.isPrimitive() && !binding.getName().equals("String")) {
				bindingNodeCreator.getBindingNode(node, binding);
			}
		}

		// add method binding
		if (astNode instanceof MethodDeclaration) {
			bindingNodeCreator.getBindingNode(node, ((MethodDeclaration) astNode).resolveBinding());
		}

		map.put(astNode, node);

		return node;
	}

	/**
	 * Deletes the node corresponding to this AST node.
	 * <p>
	 * NOTE: <code>deleteNode()</code> must be invoked on a node with no
	 * relationships, otherwise, an unchecked exception will be raised when the
	 * transaction is committing.
	 * 
	 * @param astNode
	 *            the AST node
	 */
	public void deleteNode(ASTNode astNode) {
		Node node = map.get(astNode);
		node.delete();
	}

	/**
	 * Deletes all the nodes corresponding to the AST nodes in list
	 * <p>
	 * NOTE: <code>deleteNodes()</code> must be invoked on nodes with no
	 * relationships, otherwise, an unchecked exception will be raised when the
	 * transaction is committing.
	 * 
	 * @param astNodes
	 *            the list of AST nodes
	 */
	@SuppressWarnings("rawtypes")
	public void deleteNodes(List astNodes) {
		for (Object obj : astNodes) {
			ASTNode astNode = (ASTNode) obj;
			deleteNode(astNode);
		}
	}

	public void setProperty(ASTNode node, String name, Object value) {
		if (value == null) {
			return;
		}
		map.get(node).setProperty(name, value);
	}

	public void addRelationship(ASTNode startNode, ASTNode endNode, String relName) {
		if (endNode == null) {
			return;
		}
		Node from = map.get(startNode);
		Node to = map.get(endNode);

		Relationship rel = from.createRelationshipTo(to, RelType.AST);
		rel.setProperty("NAME", relName);
	}

	@SuppressWarnings("rawtypes")
	public void addRelationships(ASTNode startNode, List endNodes, String type) {
		for (Object obj : endNodes) {
			ASTNode endNode = (ASTNode) obj;
			addRelationship(startNode, endNode, type);
		}
	}

}

package graph;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.IBinding;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

public class BindingNodeCreator {

	private final GraphDatabaseService db;

	private Map<String, Node> map = new HashMap<>();

	public BindingNodeCreator(GraphDatabaseService db) {
		this.db = db;
	}

	public Node getBindingNode(Node node0, IBinding binding) {
		if (node0 == null) {
			throw new IllegalArgumentException();
		}
		if (binding == null) {
			throw new NullPointerException("binding is null");
		}

		String key = binding.getKey();

		if (!map.containsKey(key)) {

			Node node = db.createNode();
			node.addLabel(NodeLabel.Binding);

			int kind = binding.getKind();
			if (kind == IBinding.TYPE) {
				node.addLabel(NodeLabel.TypeBinding);
			} else if (kind == IBinding.METHOD) {
				node.addLabel(NodeLabel.MethodBinding);
			} else if (kind == IBinding.VARIABLE) {
				node.addLabel(NodeLabel.VariableBinding);
			} else {
				throw new AssertionError();
			}
			
			node.setProperty("KEY", key);
			node.setProperty("NAME", binding.getName());

			map.put(key, node);
		}

		Node node = map.get(key);
		node0.createRelationshipTo(node, RelType.BINDING);
		return node;
	}

}

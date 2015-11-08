package ast;

import org.eclipse.jdt.core.dom.ASTNode;

public class Tree {

	public String filename;
	public ASTNode root;

	public Tree(String filename, ASTNode root) {
		this.filename = filename;
		this.root = root;
	}
}
package ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import graph.Graph;
import graph.RelName;

/**
 * <code>StoreVisitor</code> is bound to a <code>Graph</code> instance. When it
 * visits each node on AST, it stores nodes and relationships in graph by
 * invoking <code>Graph</code>'s methods.
 * <p>
 * Working pattern and procedure:
 * <ol>
 * <li>create nodes in <code>preVisit()</code>
 * <li>set property for graph in <code>visit(graph)</code>
 * <li>add relationship between nodes in <code>endVisit(graph)</code>
 * </ol>
 * <p>
 * Note that <code>Graph.addRelationship()</code> and
 * <code>Graph.addRelationships()</code> should be invoked in
 * <code>endVisit</code> (but not in <code>visit</code>), because a relationship
 * can only be created when both nodes are created.
 * <p>
 * Modifications on Syntax Tree:
 * <ol>
 * <li>discard all comments, delete <code>LineComment</code>,
 * <code>BlockComment</code>, <code>Javadoc</code>, <code>TagElement</code>,
 * <code>TextElement</code> nodes
 * <li>delete <code>Modifier</code> nodes, add <em>MODIFIERS</em> property to
 * <code>TypeDeclaration</code>, <code>FieldDeclaration</code>,
 * <code>MethodDeclaration</code>, <code>SingleVariableDeclaration</code> node,
 * which is of <code>int</code> type
 * </ol>
 * 
 * @see Graph
 *
 */
public class StoreVisitor extends ASTVisitor {

	private final Graph graph;

	public StoreVisitor(Graph graph) {
		this.graph = graph;
	}

	@Override
	public void preVisit(ASTNode node) {
		graph.createNode(node);
	}

	@Override
	public void postVisit(ASTNode node) {

	}

	@Override
	public void endVisit(AnnotationTypeDeclaration node) {

	}

	@Override
	public void endVisit(AnnotationTypeMemberDeclaration node) {

	}

	@Override
	public void endVisit(AnonymousClassDeclaration node) {

	}

	@Override
	public void endVisit(ArrayAccess node) {
		graph.addRelationship(node, node.getArray(), RelName.ARRAY);
		graph.addRelationship(node, node.getIndex(), RelName.INDEX);
	}

	@Override
	public void endVisit(ArrayCreation node) {
		graph.addRelationship(node, node.getType(), RelName.TYPE);
		graph.addRelationships(node, node.dimensions(), RelName.DIMENSIONS);
		graph.addRelationship(node, node.getInitializer(), RelName.INITIALIZER);
	}

	@Override
	public void endVisit(ArrayInitializer node) {
		graph.addRelationships(node, node.expressions(), RelName.EXPRESSIONS);
	}

	@Override
	public void endVisit(ArrayType node) {
		graph.addRelationship(node, node.getElementType(), RelName.ELEMENT_TYPE);
	}

	@Override
	public void endVisit(AssertStatement node) {

	}

	@Override
	public void endVisit(Assignment node) {
		graph.setProperty(node, "OPERATOR", node.getOperator().toString());

		graph.addRelationship(node, node.getLeftHandSide(), RelName.LEFT_HAND_SIDE);
		graph.addRelationship(node, node.getRightHandSide(), RelName.RIGHT_HAND_SIDE);
	}

	@Override
	public void endVisit(Block node) {
		graph.addRelationships(node, node.statements(), RelName.STATEMENTS);
	}

	@Override
	public void endVisit(BlockComment node) {
		graph.deleteNode(node);
	}

	@Override
	public void endVisit(BooleanLiteral node) {

	}

	@Override
	public void endVisit(BreakStatement node) {

	}

	@Override
	public void endVisit(CastExpression node) {

	}

	@Override
	public void endVisit(CatchClause node) {
		graph.addRelationship(node, node.getException(), RelName.EXCEPTION);
		graph.addRelationship(node, node.getBody(), RelName.BODY);

	}

	@Override
	public void endVisit(CharacterLiteral node) {

	}

	@Override
	public void endVisit(ClassInstanceCreation node) {

	}

	@Override
	public void endVisit(CompilationUnit node) {
		graph.addRelationship(node, node.getPackage(), RelName.PACKAGE);
		graph.addRelationships(node, node.imports(), RelName.IMPORTS);
		graph.addRelationships(node, node.types(), RelName.TYPES);
	}

	@Override
	public void endVisit(ConditionalExpression node) {

	}

	@Override
	public void endVisit(ConstructorInvocation node) {

	}

	@Override
	public void endVisit(ContinueStatement node) {

	}

	@Override
	public void endVisit(CreationReference node) {

	}

	@Override
	public void endVisit(Dimension node) {

	}

	@Override
	public void endVisit(DoStatement node) {

	}

	@Override
	public void endVisit(EmptyStatement node) {

	}

	@Override
	public void endVisit(EnhancedForStatement node) {

	}

	@Override
	public void endVisit(EnumConstantDeclaration node) {

	}

	@Override
	public void endVisit(EnumDeclaration node) {

	}

	@Override
	public void endVisit(ExpressionMethodReference node) {

	}

	@Override
	public void endVisit(ExpressionStatement node) {
		graph.addRelationship(node, node.getExpression(), RelName.EXPRESSION);
	}

	@Override
	public void endVisit(FieldAccess node) {
		graph.addRelationship(node, node.getExpression(), RelName.EXPRESSION);
		graph.addRelationship(node, node.getName(), RelName.NAME);
	}

	@Override
	public void endVisit(FieldDeclaration node) {
		graph.setProperty(node, "MODIFIERS", node.getModifiers());
		graph.deleteNodes(node.modifiers());

		graph.addRelationship(node, node.getType(), RelName.TYPE);
		graph.addRelationships(node, node.fragments(), RelName.FRAGMENTS);
	}

	@Override
	public void endVisit(ForStatement node) {
		graph.addRelationships(node, node.initializers(), RelName.INITIALIZERS);
		graph.addRelationship(node, node.getExpression(), RelName.EXPRESSION);
		graph.addRelationships(node, node.updaters(), RelName.UPDATERS);
		graph.addRelationship(node, node.getBody(), RelName.BODY);
	}

	@Override
	public void endVisit(IfStatement node) {

	}

	@Override
	public void endVisit(ImportDeclaration node) {
		graph.setProperty(node, "STATIC", node.isStatic());
		graph.setProperty(node, "ON_DEMAND", node.isOnDemand());

		graph.addRelationship(node, node.getName(), RelName.NAME);
	}

	@Override
	public void endVisit(InfixExpression node) {
		graph.setProperty(node, "OPERATOR", node.getOperator().toString());

		graph.addRelationship(node, node.getLeftOperand(), RelName.LEFT_OPERAND);
		graph.addRelationship(node, node.getRightOperand(), RelName.RIGHT_OPERAND);
		graph.addRelationships(node, node.extendedOperands(), RelName.EXTENDED_OPERANDS);
	}

	@Override
	public void endVisit(Initializer node) {

	}

	@Override
	public void endVisit(InstanceofExpression node) {

	}

	@Override
	public void endVisit(IntersectionType node) {

	}

	@Override
	public void endVisit(Javadoc node) {
		graph.deleteNode(node);
	}

	@Override
	public void endVisit(LabeledStatement node) {

	}

	@Override
	public void endVisit(LambdaExpression node) {

	}

	@Override
	public void endVisit(LineComment node) {
		graph.deleteNode(node);
	}

	@Override
	public void endVisit(MarkerAnnotation node) {

	}

	@Override
	public void endVisit(MemberRef node) {

	}

	@Override
	public void endVisit(MemberValuePair node) {

	}

	@Override
	public void endVisit(MethodDeclaration node) {
		graph.setProperty(node, "CONSTRUCTOR", node.isConstructor());

		graph.setProperty(node, "MODIFIERS", node.getModifiers());
		graph.deleteNodes(node.modifiers());

		graph.addRelationships(node, node.typeParameters(), RelName.TYPE_PARAMETERS);
		graph.addRelationship(node, node.getReturnType2(), RelName.RETURN_TYPE);
		graph.addRelationship(node, node.getName(), RelName.NAME);
		graph.addRelationships(node, node.parameters(), RelName.PARAMETERS);
		graph.addRelationship(node, node.getBody(), RelName.BODY);
	}

	@Override
	public void endVisit(MethodInvocation node) {
		graph.addRelationship(node, node.getExpression(), RelName.EXPRESSION);
		graph.addRelationships(node, node.typeArguments(), RelName.TYPE_ARGUMENTS);
		graph.addRelationship(node, node.getName(), RelName.NAME);
		graph.addRelationships(node, node.arguments(), RelName.ARGUMENTS);
	}

	@Override
	public void endVisit(MethodRef node) {

	}

	@Override
	public void endVisit(MethodRefParameter node) {

	}

	@Override
	public void endVisit(Modifier node) {

	}

	@Override
	public void endVisit(NameQualifiedType node) {

	}

	@Override
	public void endVisit(NormalAnnotation node) {

	}

	@Override
	public void endVisit(NullLiteral node) {

	}

	@Override
	public void endVisit(NumberLiteral node) {
		graph.setProperty(node, "TOKEN", node.getToken());
	}

	@Override
	public void endVisit(PackageDeclaration node) {
		graph.addRelationships(node, node.annotations(), RelName.ANNOTATIONS);
		graph.addRelationship(node, node.getName(), RelName.NAME);
	}

	@Override
	public void endVisit(ParameterizedType node) {

	}

	@Override
	public void endVisit(ParenthesizedExpression node) {

	}

	@Override
	public void endVisit(PostfixExpression node) {
		graph.setProperty(node, "OPERATOR", node.getOperator().toString());

		graph.addRelationship(node, node.getOperand(), RelName.OPERAND);
	}

	@Override
	public void endVisit(PrefixExpression node) {

	}

	@Override
	public void endVisit(PrimitiveType node) {
		graph.setProperty(node, "PRIMITIVE_TYPE_CODE", node.getPrimitiveTypeCode().toString());
	}

	@Override
	public void endVisit(QualifiedName node) {
		graph.addRelationship(node, node.getQualifier(), RelName.QUALIFIER);
		graph.addRelationship(node, node.getName(), RelName.NAME);
	}

	@Override
	public void endVisit(QualifiedType node) {
		graph.addRelationship(node, node.getQualifier(), RelName.QUALIFIER);
		graph.addRelationship(node, node.getName(), RelName.NAME);

	}

	@Override
	public void endVisit(ReturnStatement node) {
		graph.addRelationship(node, node.getExpression(), RelName.EXPRESSION);
	}

	@Override
	public void endVisit(SimpleName node) {
		graph.setProperty(node, "IDENTIFIER", node.getIdentifier());
	}

	@Override
	public void endVisit(SimpleType node) {
		graph.addRelationship(node, node.getName(), RelName.NAME);
	}

	@Override
	public void endVisit(SingleMemberAnnotation node) {

	}

	@Override
	public void endVisit(SingleVariableDeclaration node) {
		graph.setProperty(node, "VARARGS", node.isVarargs());

		graph.setProperty(node, "MODIFIERS", node.getModifiers());
		graph.deleteNodes(node.modifiers());

		graph.addRelationship(node, node.getType(), RelName.TYPE);
		graph.addRelationship(node, node.getName(), RelName.NAME);
		graph.addRelationship(node, node.getInitializer(), RelName.INITIALIZER);
	}

	@Override
	public void endVisit(StringLiteral node) {

	}

	@Override
	public void endVisit(SuperConstructorInvocation node) {
		graph.addRelationship(node, node.getExpression(), RelName.EXPRESSION);
		graph.addRelationships(node, node.typeArguments(), RelName.TYPE_ARGUMENTS);
		graph.addRelationships(node, node.arguments(), RelName.ARGUMENTS);
	}

	@Override
	public void endVisit(SuperFieldAccess node) {

	}

	@Override
	public void endVisit(SuperMethodInvocation node) {

	}

	@Override
	public void endVisit(SwitchCase node) {

	}

	@Override
	public void endVisit(SwitchStatement node) {

	}

	@Override
	public void endVisit(SynchronizedStatement node) {

	}

	@Override
	public void endVisit(TagElement node) {
		graph.deleteNode(node);
	}

	@Override
	public void endVisit(TextElement node) {
		graph.deleteNode(node);
	}

	@Override
	public void endVisit(ThisExpression node) {
		graph.addRelationship(node, node.getQualifier(), RelName.QUALIFIER);
	}

	@Override
	public void endVisit(ThrowStatement node) {
		graph.addRelationship(node, node.getExpression(), RelName.EXPRESSION);

	}

	@Override
	public void endVisit(TryStatement node) {
		graph.addRelationship(node, node.getBody(), RelName.BODY);
		graph.addRelationships(node, node.catchClauses(), RelName.CATCH_CLAUSES);
		graph.addRelationship(node, node.getFinally(), RelName.FINALLY);
	}

	@Override
	public void endVisit(TypeDeclaration node) {
		graph.setProperty(node, "INTERFACE", node.isInterface());

		graph.setProperty(node, "MODIFIERS", node.getModifiers());
		graph.deleteNodes(node.modifiers());

		graph.addRelationship(node, node.getName(), RelName.NAME);
		graph.addRelationships(node, node.typeParameters(), RelName.TYPE_PARAMETERS);
		graph.addRelationship(node, node.getSuperclassType(), RelName.SUPERCLASS_TYPE);
		graph.addRelationships(node, node.superInterfaceTypes(), RelName.SUPER_INTERFACE_TYPES);
		graph.addRelationships(node, node.bodyDeclarations(), RelName.BODY_DECLARATIONS);
	}

	@Override
	public void endVisit(TypeDeclarationStatement node) {

	}

	@Override
	public void endVisit(TypeLiteral node) {

	}

	@Override
	public void endVisit(TypeMethodReference node) {

	}

	@Override
	public void endVisit(TypeParameter node) {

	}

	@Override
	public void endVisit(UnionType node) {

	}

	@Override
	public void endVisit(VariableDeclarationExpression node) {
		graph.addRelationships(node, node.modifiers(), RelName.MODIFIERS);
		graph.addRelationship(node, node.getType(), RelName.TYPE);
		graph.addRelationships(node, node.fragments(), RelName.FRAGMENTS);
	}

	@Override
	public void endVisit(VariableDeclarationFragment node) {
		graph.addRelationship(node, node.getName(), RelName.NAME);
		graph.addRelationship(node, node.getInitializer(), RelName.INITIALIZER);
	}

	@Override
	public void endVisit(VariableDeclarationStatement node) {
		graph.addRelationships(node, node.modifiers(), RelName.MODIFIERS);
		graph.addRelationship(node, node.getType(), RelName.TYPE);
		graph.addRelationships(node, node.fragments(), RelName.FRAGMENTS);
	}

	@Override
	public void endVisit(WhileStatement node) {

	}

	@Override
	public void endVisit(WildcardType node) {

	}

}

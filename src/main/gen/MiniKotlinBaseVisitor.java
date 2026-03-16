// Generated from /home/drodzek/Documents/Studia/compiler-internship-task-2026/src/main/antlr/MiniKotlin.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;


@SuppressWarnings("CheckReturnValue")
public class MiniKotlinBaseVisitor<T> extends AbstractParseTreeVisitor<T> implements MiniKotlinVisitor<T> {
	
	@Override public T visitProgram(MiniKotlinParser.ProgramContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitFunctionDeclaration(MiniKotlinParser.FunctionDeclarationContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitParameterList(MiniKotlinParser.ParameterListContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitParameter(MiniKotlinParser.ParameterContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitType(MiniKotlinParser.TypeContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitBlock(MiniKotlinParser.BlockContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitStatement(MiniKotlinParser.StatementContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitVariableDeclaration(MiniKotlinParser.VariableDeclarationContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitVariableAssignment(MiniKotlinParser.VariableAssignmentContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitIfStatement(MiniKotlinParser.IfStatementContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitWhileStatement(MiniKotlinParser.WhileStatementContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitReturnStatement(MiniKotlinParser.ReturnStatementContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitAndExpr(MiniKotlinParser.AndExprContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitFunctionCallExpr(MiniKotlinParser.FunctionCallExprContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitMulDivExpr(MiniKotlinParser.MulDivExprContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitEqualityExpr(MiniKotlinParser.EqualityExprContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitComparisonExpr(MiniKotlinParser.ComparisonExprContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitPrimaryExpr(MiniKotlinParser.PrimaryExprContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitNotExpr(MiniKotlinParser.NotExprContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitAddSubExpr(MiniKotlinParser.AddSubExprContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitOrExpr(MiniKotlinParser.OrExprContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitParenExpr(MiniKotlinParser.ParenExprContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitIntLiteral(MiniKotlinParser.IntLiteralContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitStringLiteral(MiniKotlinParser.StringLiteralContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitBoolLiteral(MiniKotlinParser.BoolLiteralContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitIdentifierExpr(MiniKotlinParser.IdentifierExprContext ctx) { return visitChildren(ctx); }
	
	@Override public T visitArgumentList(MiniKotlinParser.ArgumentListContext ctx) { return visitChildren(ctx); }
}
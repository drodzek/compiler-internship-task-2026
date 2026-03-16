package org.example.compiler

import MiniKotlinBaseVisitor
import MiniKotlinParser
import MiniKotlinParser.*

class MiniKotlinCompiler : MiniKotlinBaseVisitor<String>() {

    private var counterID = -1 // starting value
    fun getCounter(): Int {
        counterID++
        return counterID
    }

    fun resetCounter() {
        counterID = -1
    }

    fun compile(program: MiniKotlinParser.ProgramContext, className: String = "MiniProgram"): String {
        val compiledFunctions = visit(program)

        return """
            public class $className {
                $compiledFunctions
            }
        """.trimIndent()
    }

    override fun visitProgram(ctx: MiniKotlinParser.ProgramContext): String {
        val resultString = StringBuilder()
        val functions = ctx.functionDeclaration()
        for (function in functions) {
            val compiledFunction = visit(function)
            resultString.append(compiledFunction + "\n\n")
            resetCounter()
        }
        return resultString.toString().trim()
    }

    override fun visitFunctionDeclaration(ctx: MiniKotlinParser.FunctionDeclarationContext): String {
        
    }

    override fun visitParameterList(ctx: MiniKotlinParser.ParameterListContext): String {
        
    }

    override fun visitParameter(ctx: MiniKotlinParser.ParameterContext): String {
        
    }

    override fun visitType(ctx: MiniKotlinParser.TypeContext): String {
        
    }

    override fun visitBlock(ctx: MiniKotlinParser.BlockContext): String {
        
    }

    override fun visitStatement(ctx: MiniKotlinParser.StatementContext): String {
        
    }

    override fun visitVariableDeclaration(ctx: MiniKotlinParser.VariableDeclarationContext): String {
        
    }

    override fun visitVariableAssignment(ctx: MiniKotlinParser.VariableAssignmentContext): String {
        
    }

    override fun visitIfStatement(ctx: MiniKotlinParser.IfStatementContext): String {
        
    }

    override fun visitWhileStatement(ctx: MiniKotlinParser.WhileStatementContext): String {
        
    }

    override fun visitReturnStatement(ctx: MiniKotlinParser.ReturnStatementContext): String {
        
    }

    override fun visitAndExpr(ctx: MiniKotlinParser.AndExprContext): String {
        val firstArg = visit(ctx.expression(0))
        val secondArg = visit(ctx.expression(1))
        val operator = ctx.getChild(1).text
        return "$firstArg $operator $secondArg"
    }

    override fun visitFunctionCallExpr(ctx: MiniKotlinParser.FunctionCallExprContext): String {
        
    }

    override fun visitMulDivExpr(ctx: MiniKotlinParser.MulDivExprContext): String {
        val firstArg = visit(ctx.expression(0))
        val secondArg = visit(ctx.expression(1))
        val operator = ctx.getChild(1).text
        return "$firstArg $operator $secondArg"
    }

    override fun visitEqualityExpr(ctx: MiniKotlinParser.EqualityExprContext): String {
        val firstArg = visit(ctx.expression(0))
        val secondArg = visit(ctx.expression(1))
        val operator = ctx.getChild(1).text
        return "$firstArg $operator $secondArg"
    }

    override fun visitComparisonExpr(ctx: MiniKotlinParser.ComparisonExprContext): String {
        val firstArg = visit(ctx.expression(0))
        val secondArg = visit(ctx.expression(1))
        val operator = ctx.getChild(1).text
        return "$firstArg $operator $secondArg"
    }

    override fun visitPrimaryExpr(ctx: MiniKotlinParser.PrimaryExprContext): String {
        val primary = visit(ctx.primary())
        return "$primary"
    }

    override fun visitNotExpr(ctx: MiniKotlinParser.NotExprContext): String {
        val firstArg = visit(ctx.expression())
        return "!$firstArg"
    }

    override fun visitAddSubExpr(ctx: MiniKotlinParser.AddSubExprContext): String {
        val firstArg = visit(ctx.expression(0))
        val secondArg = visit(ctx.expression(1))
        val operator = ctx.getChild(1).text
        return "$firstArg $operator $secondArg"
    }

    override fun visitOrExpr(ctx: MiniKotlinParser.OrExprContext): String {
        val firstArg = visit(ctx.expression(0))
        val secondArg = visit(ctx.expression(1))
        val operator = ctx.getChild(1).text
        return "$firstArg $operator $secondArg"
    }

    override fun visitParenExpr(ctx: MiniKotlinParser.ParenExprContext): String {
        val expression = visit(ctx.expression())
        return "($expression)"
    }

    override fun visitIntLiteral(ctx: MiniKotlinParser.IntLiteralContext): String {
        return ctx.text
    }

    override fun visitStringLiteral(ctx: MiniKotlinParser.StringLiteralContext): String {
        return ctx.text
    }

    override fun visitBoolLiteral(ctx: MiniKotlinParser.BoolLiteralContext): String {
        return ctx.text
    }

    override fun visitIdentifierExpr(ctx: MiniKotlinParser.IdentifierExprContext): String {
        return ctx.IDENTIFIER().text
    }

    override fun visitArgumentList(ctx: MiniKotlinParser.ArgumentListContext): String {
        
    }
}

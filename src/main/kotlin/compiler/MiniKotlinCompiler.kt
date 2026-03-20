package org.example.compiler

import MiniKotlinBaseVisitor
import MiniKotlinParser
import MiniKotlinParser.*

class MiniKotlinCompiler : MiniKotlinBaseVisitor<String>() {
    private var targetCallToReplace: MiniKotlinParser.FunctionCallExprContext? = null
    private var replacementArgName: String = ""

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
        val name = ctx.IDENTIFIER().text
        val returnType = visit(ctx.type())
        val body = visit(ctx.block())

        return if (name == "main") {
            "public static void main(String[] args) { \n$body\n}"
        } else {
            val params = if (ctx.parameterList() != null) {
                visit(ctx.parameterList()) + ", Continuation<$returnType> __continuation"
            } else {
                "Continuation<$returnType> __continuation"
            }
            "public static void $name($params) { \n$body\n}"
        }
    }

    override fun visitParameterList(ctx: MiniKotlinParser.ParameterListContext): String {
        return ctx.parameter().joinToString(", ") { visit(it) }
    }

    override fun visitParameter(ctx: MiniKotlinParser.ParameterContext): String {
        val name = ctx.IDENTIFIER().text
        val type = visit(ctx.type())
        return "$type $name"
    }

    override fun visitType(ctx: MiniKotlinParser.TypeContext): String {
        return when (ctx.text) {
            "String" -> "String"
            "Int" -> "Integer"
            "Boolean" -> "Boolean"
            "Unit" -> "Void"
            else -> ctx.text
        }
    }

    override fun visitBlock(ctx: MiniKotlinParser.BlockContext): String {
        return visitStatements(ctx.statement(), 0)
    }

    private fun visitStatements(statements: List<MiniKotlinParser.StatementContext>, from: Int): String {
        if (from >= statements.size) return ""

        val stmt = statements[from]
        val stmtCtx = stmt.getChild(0)

        return when (stmtCtx) {
            is MiniKotlinParser.VariableDeclarationContext -> {
                compileVarDeclCPS(stmtCtx, statements, from)
            }
            is MiniKotlinParser.VariableAssignmentContext -> {
                val rest = visitStatements(statements, from + 1)
                val compiled = visit(stmtCtx)
                if (rest.isNotEmpty()) "$compiled\n$rest" else compiled
            }
            is MiniKotlinParser.IfStatementContext -> {
                compileIfCPS(stmtCtx, statements, from)
            }
            is MiniKotlinParser.WhileStatementContext -> {
                val rest = visitStatements(statements, from + 1)
                val compiled = visit(stmtCtx)
                if (rest.isNotEmpty()) "$compiled\n$rest" else compiled
            }
            is MiniKotlinParser.ReturnStatementContext -> {
                compileReturnCPS(stmtCtx)
            }
            else -> {
                compileExprStmtCPS(stmt, statements, from)
            }
        }
    }

    private fun compileVarDeclCPS(
        ctx: MiniKotlinParser.VariableDeclarationContext,
        statements: List<MiniKotlinParser.StatementContext>,
        from: Int
    ): String {
        val name = ctx.IDENTIFIER().text
        val type = visit(ctx.type())
        val expr = ctx.expression()
        val rest = visitStatements(statements, from + 1)

        return if (isFunctionCall(expr)) {
            val argIdx = getCounter()
            val argName = "arg$argIdx"
            val callStr = compileFunctionCallCPS(expr, argName, "$type $name = $argName;\n$rest")
            callStr
        } else {
            val exprStr = visit(expr)
            val decl = "$type $name = $exprStr;"
            if (rest.isNotEmpty()) "$decl\n$rest" else decl
        }
    }

    private fun compileIfCPS(
        ctx: MiniKotlinParser.IfStatementContext,
        statements: List<MiniKotlinParser.StatementContext>,
        from: Int
    ): String {
        val condition = visit(ctx.expression())
        val thenBlock = visit(ctx.block(0))
        val result = StringBuilder()
        result.append("if ($condition) {\n$thenBlock\n}")
        if (ctx.block().size > 1) {
            val elseBlock = visit(ctx.block(1))
            result.append("\nelse {\n$elseBlock\n}")
        }

        val rest = visitStatements(statements, from + 1)
        if (rest.isNotEmpty()) result.append("\n$rest")
        return result.toString()
    }

    private fun compileExprStmtCPS(
        ctx: MiniKotlinParser.StatementContext,
        statements: List<MiniKotlinParser.StatementContext>,
        from: Int
    ): String {
        val exprChild = ctx.getChild(0)
        if (exprChild is MiniKotlinParser.ExpressionContext || exprChild != null) {
            val stmtText = ctx.text
            val rest = visitStatements(statements, from + 1)
            val exprCtx = findExpression(ctx)
            if (exprCtx != null && isFunctionCall(exprCtx)) {
                val argIdx = getCounter()
                val argName = "arg$argIdx"
                return compileFunctionCallCPS(exprCtx, argName, rest)
            }
            val compiled = visit(ctx)
            return if (rest.isNotEmpty()) "$compiled\n$rest" else compiled
        }
        val rest = visitStatements(statements, from + 1)
        val compiled = visit(ctx)
        return if (rest.isNotEmpty()) "$compiled\n$rest" else compiled
    }

    private fun isFunctionCall(expr: MiniKotlinParser.ExpressionContext): Boolean {
        return expr is MiniKotlinParser.FunctionCallExprContext
    }

    private fun findExpression(ctx: MiniKotlinParser.StatementContext): MiniKotlinParser.ExpressionContext? {
        for (i in 0 until ctx.childCount) {
            val child = ctx.getChild(i)
            if (child is MiniKotlinParser.ExpressionContext) {
                return child
            }
        }
        return null
    }

    private fun findFunctionCall(ctx: org.antlr.v4.runtime.ParserRuleContext): MiniKotlinParser.FunctionCallExprContext? {
        if (ctx is MiniKotlinParser.FunctionCallExprContext) {
            return ctx
        }
        for (i in 0 until ctx.childCount) {
            val child = ctx.getChild(i)
            if (child is org.antlr.v4.runtime.ParserRuleContext) {
                val found = findFunctionCall(child)
                if (found != null) {
                    return found
                }
            }
        }
        return null
    }

    private fun compileFunctionCallCPS(
        expr: MiniKotlinParser.ExpressionContext,
        argName: String,
        bodyAfter: String
    ): String {
        val callExpr = expr as MiniKotlinParser.FunctionCallExprContext
        val funcName = callExpr.IDENTIFIER().text
        val args = if (callExpr.argumentList() != null) visit(callExpr.argumentList()) else ""

        val prefix = if (funcName == "println") "Prelude" else null
        val qualifiedName = if (prefix != null) "$prefix.$funcName" else funcName

        val contBody = if (bodyAfter.isNotEmpty()) {
            "($argName) -> {\n$bodyAfter\n}"
        } else {
            "($argName) -> {\n}"
        }

        val callArgs = if (args.isNotEmpty()) "$args, $contBody" else contBody
        return "$qualifiedName($callArgs);"
    }

    private fun compileReturnCPS(ctx: MiniKotlinParser.ReturnStatementContext): String {
        val expr = ctx.expression()
        val nestedCall = findFunctionCall(expr)

        return if (nestedCall != null) {
            val argIdx = getCounter()
            val argName = "arg$argIdx"

            targetCallToReplace = nestedCall
            replacementArgName = argName
            val modifiedExpr = visit(expr)

            targetCallToReplace = null
            replacementArgName = ""

            compileFunctionCallCPS(nestedCall, argName, "__continuation.accept($modifiedExpr);\nreturn;")
        } else {
            visit(ctx)
        }
    }

    override fun visitStatement(ctx: MiniKotlinParser.StatementContext): String {
        return visit(ctx.getChild(0))
    }

    override fun visitVariableDeclaration(ctx: MiniKotlinParser.VariableDeclarationContext): String {
        val name = ctx.IDENTIFIER().text
        val type = visit(ctx.type())
        val expr = visit(ctx.expression())
        return "$type $name = $expr;"
    }

    override fun visitVariableAssignment(ctx: MiniKotlinParser.VariableAssignmentContext): String {
        val name = ctx.IDENTIFIER().text
        val expr = visit(ctx.expression())
        return "$name = $expr;"
    }

    override fun visitIfStatement(ctx: MiniKotlinParser.IfStatementContext): String {
        val condition = visit(ctx.expression())
        val thenBlock = visit(ctx.block(0))
        val result = StringBuilder()
        result.append("if ($condition) {\n$thenBlock\n}")
        if (ctx.block().size > 1) {
            val elseBlock = visit(ctx.block(1))
            result.append("\nelse {\n$elseBlock\n}")
        }
        return result.toString()
    }

    override fun visitWhileStatement(ctx: MiniKotlinParser.WhileStatementContext): String {
        val condition = visit(ctx.expression())
        val body = visit(ctx.block())
        return "while ($condition) {\n$body\n}"
    }

    override fun visitReturnStatement(ctx: MiniKotlinParser.ReturnStatementContext): String {
        val expr = visit(ctx.expression())
        return "__continuation.accept($expr);\nreturn;"
    }

    override fun visitAndExpr(ctx: MiniKotlinParser.AndExprContext): String {
        val firstArg = visit(ctx.expression(0))
        val secondArg = visit(ctx.expression(1))
        val operator = ctx.getChild(1).text
        return "$firstArg $operator $secondArg"
    }

    override fun visitFunctionCallExpr(ctx: MiniKotlinParser.FunctionCallExprContext): String {
        if (ctx === targetCallToReplace) {
            return replacementArgName
        }

        val funcName = ctx.IDENTIFIER().text
        val args = if (ctx.argumentList() != null) visit(ctx.argumentList()) else ""
        val prefix = if (funcName == "println") "Prelude" else null
        val qualifiedName = if (prefix != null) "$prefix.$funcName" else funcName
        return "$qualifiedName($args)"
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
        return ctx.expression().joinToString(", ") { visit(it) }
    }
}

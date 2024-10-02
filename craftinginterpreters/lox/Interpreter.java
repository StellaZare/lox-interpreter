package craftinginterpreters.lox;

import java.util.List;

/**
 * This class is a visitor that interprets the expression
 * The return type of the visitor is Object
 * 
 * program -> declaration* EOF ;
 * 
 * declaration -> varDecl | statement ;
 * 
 * varDecl -> "var" IDENTIFIER ( "=" expression )? ";" ;
 * statement -> exprStmt | printStmt ;
 * 
 * exprStmt -> expression ";" ;
 * printStmt -> "print" expression ";" ;
 * 
 */
class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment();

    /**
     * The interpreter's public API
     */
    void interpret(List<Stmt> statements) {
        try {
            for(Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    /**
     * Invokes the visitor for the provided Expr subclass
     * i.e. Binary, Grouping, Literal, ...
     */
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    /**
     * Invokes the visitor for the provided Stmt subclass
     * i.e. Expression, Print, Var
     */
    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    /**
     * Evaluate the block
     */
    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        // Save the outer block environment
        Environment previous = this.environment;
        try {
            // Replace with current block environment
            this.environment = environment;
            for (Stmt s : statements) {
                execute(s);
            }
        } finally {
            // Reset outer block environment
            this.environment = previous;
        }
    }

    /**
     * Evaluate the statement
     */
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    /**
     * Evaluate the print statement
     */
    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    /**
     * Evaluate statements declaring a variable
     */
    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            // An expression was provided at initilization
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }
    

    /**
     * Evaluate assignment expresions
     */
    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, expr.value);
        return value;
    }

    /**
     * Convert the literal to a runtime value
     */
    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    /**
     * Convert grouping node to a runtime value
     * 
     * The grouping node is a wrapper around another expression,
     * To evaluate the grouping expression, we evaluate inner expression recursively 
     */
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    /**
     * Convert the unary expression to a runtime value
     * Evaluate the operand and apply the unary operator to the result
     * Cannot evaluate the unary expression without the operand first
     */
    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
            case BANG:
                return !isTruthy(right);
        }

        // Unreachable
        return null;
    }

    /**
     * Evaluate the variable expression
     * @throws RuntimeError if the variable is not found
     */
    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    /**
     * Convert the binary expression to a runtime value
     * To evaluate the binary expression, we evaluate left and right expression recursively
     */
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");

            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }
        return null;
    }
    /**
     * Check if the operand is valid for the unary operator (ie. a number)
     */
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    /**
     * Assign a truthy value to the value
     * False and nil are falsey, everything else is truthy
     */
    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    /**
     * Check if the two operands are equal
     */
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    /**
     * Check if the operands are valid for the binary operator (ie. numbers)
     */
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    /**
     * Convert Lox runtime value to a string
     * Value can be a number, string, or nil
     */
    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }
}
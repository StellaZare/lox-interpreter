package craftinginterpreters.lox;

// import java.util.List;

abstract class Expr {
    /**
     * The visitor functions for each type of expression
     * is defined in Interpreter.java
     * 
     */
    interface Visitor<R> {
        R visitLogicalExpr(Logical expr);
        R visitBinaryExpr(Binary expr );
        R visitGroupingExpr(Grouping expr );
        R visitLiteralExpr(Literal expr );
        R visitUnaryExpr(Unary expr );
        R visitVariableExpr(Variable expr );
        R visitAssignExpr(Assign expr );
    }

    static class Logical extends Expr {
        Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.right = right;
            this.operator = operator;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }

        final Expr left;
        final Expr right;
        final Token operator;
    }

    static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }

        // The value or expression to the left of the operator
        final Expr left;
        // The operator token ( +, -, *, / )
        final Token operator;
        // The value or expression to the right of the operator
        final Expr right;
    }

    static class Grouping extends Expr {
        Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }

        // The expression inside the parentheses
        final Expr expression;
    }

    static class Literal extends Expr {
        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        // The value of the literal
        final Object value;
    }

    static class Unary extends Expr {
        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }

        // The unary operator token ( !, - )
        final Token operator;
        // The number or expression to which the operator applies
        final Expr right;
    }

    static class Variable extends Expr {
        Variable(Token name) {
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
        
        final Token name;
    }

    static class Assign extends Expr {
        Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }

        final Token name;
        final Expr value;
    }

  abstract <R> R accept(Visitor<R> visitor);
}


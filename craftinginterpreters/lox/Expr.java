package craftinginterpreters.lox;

// import java.util.List;

abstract class Expr {
  /**
   * The visitor functions for each type of expression
   * is defined in Interpreter.java
   * 
   */
  interface Visitor<R> {
    R visitBinaryExpr(Binary expr );
    R visitGroupingExpr(Grouping expr );
    R visitLiteralExpr(Literal expr );
    R visitUnaryExpr(Unary expr );
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

  abstract <R> R accept(Visitor<R> visitor);
}


package com.bloxi.lox;

class AstPrinter implements Expr.Visitor<String> {
  String print(Expr expr) {
    return expr.accept(this);
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return parenthesize("group", expr.expression);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    if (expr.value == null)
      return "nil";
    return expr.value.toString();
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);
  }

  private String parenthesize(String name, Expr... exprs) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    for (Expr expr : exprs) {
      builder.append(" ");
      builder.append(expr.accept(this));
    }
    builder.append(")");

    return builder.toString();
  }

  // NOTE: Driver code for testing
  // public static void main(String[] args) {
  // Token operator_star = new Token(TokenType.STAR, "*", null, 1);
  // Token operator_plus = new Token(TokenType.PLUS, "+", null, 1);
  // Expr lhs = new Expr.Literal("hello");
  // Expr group = new Expr.Binary(new Expr.Literal(42), operator_star, new
  // Expr.Literal(69));
  // Expr rhs = new Expr.Grouping(group);

  // Expr expression = new Expr.Binary(lhs, operator_plus, rhs);
  // AstPrinter printer = new AstPrinter();
  // System.out.println(printer.print(expression));
  // }
}

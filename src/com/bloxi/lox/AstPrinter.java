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

  @Override
  public String visitTernaryExpr(Expr.Ternary expr) {
    return parenthesize("?", expr.condition, expr.trueExpr, expr.falseExpr);
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

  @Override
  public String visitVariableExpr(Expr.Variable expr) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visitAssignExpr(Expr.Assign expr) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visitLogicalExpr(Expr.Logical expr) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visitCallExpr(Expr.Call expr) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visitGetExpr(Expr.Get expr) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visitSetExpr(Expr.Set expr) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visitThisExpr(Expr.This expr) {
    // TODO Auto-generated method stub
    return null;
  }

  // NOTE: Driver code for testing
  // public static void main(String[] args) {
  //// 1 + 2 ? 3 * 4 : 5 / 6;
  // Token operator_plus = new Token(TokenType.PLUS, "+", null, 1);
  // Token operator_star = new Token(TokenType.STAR, "*", null, 1);
  // Token operator_slash = new Token(TokenType.SLASH, "/", null, 1);

  // Expr condition = new Expr.Binary(new Expr.Literal(1), operator_plus, new
  // Expr.Literal(2));
  // Expr trueExpr = new Expr.Binary(new Expr.Literal(3), operator_star, new
  // Expr.Literal(4));
  // Expr falseExpr = new Expr.Binary(new Expr.Literal(5), operator_slash, new
  // Expr.Literal(6));

  // Expr ternary = new Expr.Ternary(condition, trueExpr, falseExpr);

  // AstPrinter printer = new AstPrinter();
  // System.out.println(printer.print(ternary));
  // }
}

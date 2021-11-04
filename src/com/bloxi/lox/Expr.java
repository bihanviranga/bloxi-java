package com.bloxi.lox;

import java.util.List;

abstract class Expr {
  interface Visitor<R> {
    R visitLiteralExpr (Literal expr);
    R visitUnaryExpr (Unary expr);
    R visitBinaryExpr (Binary expr);
    R visitTernaryExpr (Ternary expr);
    R visitGroupingExpr (Grouping expr);
  }

  abstract <R> R accept(Visitor<R> visitor);

  static class Literal extends Expr {
    final Object value;

    Literal(Object value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }
  }

  static class Unary extends Expr {
    final Token operator;
    final Expr right;

    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }
  }

  static class Binary extends Expr {
    final Expr left;
    final Token operator;
    final Expr right;

    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }
  }

  static class Ternary extends Expr {
    final Expr condition;
    final Expr trueExpr;
    final Expr falseExpr;

    Ternary(Expr condition, Expr trueExpr, Expr falseExpr) {
      this.condition = condition;
      this.trueExpr = trueExpr;
      this.falseExpr = falseExpr;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitTernaryExpr(this);
    }
  }

  static class Grouping extends Expr {
    final Expr expression;

    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }
  }
}
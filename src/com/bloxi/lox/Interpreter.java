package com.bloxi.lox;

import java.util.List;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
  private Environment environment = new Environment();

  void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
    }
  }

  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
  }

  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    Object rhs = evaluate(expr.right);

    switch (expr.operator.type) {
      case MINUS:
        checkNumberOperand(expr.operator, rhs);
        return -(double) rhs;
      case BANG:
        return !isTruthy(rhs);
    }

    // should be unreachable
    return null;
  }

  @Override
  public Object visitBinaryExpr(Expr.Binary expr) {
    Object lhs = evaluate(expr.left);
    Object rhs = evaluate(expr.right);

    switch (expr.operator.type) {
      case GREATER:
        checkNumberOperands(expr.operator, lhs, rhs);
        return (double) lhs > (double) rhs;
      case GREATER_EQUAL:
        checkNumberOperands(expr.operator, lhs, rhs);
        return (double) lhs >= (double) rhs;
      case LESS:
        checkNumberOperands(expr.operator, lhs, rhs);
        return (double) lhs < (double) rhs;
      case LESS_EQUAL:
        checkNumberOperands(expr.operator, lhs, rhs);
        return (double) lhs <= (double) rhs;
      case EQUAL_EQUAL:
        return isEqual(lhs, rhs);
      case BANG_EQUAL:
        return !isEqual(lhs, rhs);
      case MINUS:
        checkNumberOperands(expr.operator, lhs, rhs);
        return (double) lhs - (double) rhs;
      case STAR:
        checkNumberOperands(expr.operator, lhs, rhs);
        return (double) lhs * (double) rhs;
      case SLASH:
        checkNumberOperands(expr.operator, lhs, rhs);
        if ((double) rhs == 0) {
          throw new RuntimeError(expr.operator, "Cannot divide by zero.");
        }
        return (double) lhs / (double) rhs;
      case PLUS:
        if (lhs instanceof Double && rhs instanceof Double)
          return (double) lhs + (double) rhs;
        if (lhs instanceof String && rhs instanceof String) {
          return (String) lhs + (String) rhs;
        }
        // if either side is string
        if (lhs instanceof String || rhs instanceof String) {
          return stringify(lhs) + stringify(rhs);
        }

        throw new RuntimeError(expr.operator, "At least one of the operands must be a string.");

    }

    // should be unreachable
    return null;
  }

  @Override
  public Object visitTernaryExpr(Expr.Ternary expr) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression);
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    Object value = evaluate(stmt.expression);

    if (Lox.replMode) {
      System.out.println(stringify(value));
    }

    return null;
  }

  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    Object value = evaluate(stmt.expression);
    System.out.println(stringify(value));
    return null;
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    Object value = null;
    if (stmt.initializer != null) {
      value = evaluate(stmt.initializer);
    }

    environment.define(stmt.name.lexeme, value);
    return null;
  }

  @Override
  public Object visitVariableExpr(Expr.Variable expr) {
    return environment.get(expr.name);
  }

  @Override
  public Object visitAssignExpr(Expr.Assign expr) {
    Object value = evaluate(expr.value);
    environment.assign(expr.name, value);
    // Assign statement returns the assigned value.
    // Ex: `print a = 2` prints 2
    return value;
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    executeBlock(stmt.statements, new Environment(environment));
    return null;
  }

  private void execute(Stmt stmt) {
    stmt.accept(this);
  }

  /**
   * Boolean false and nil are the falsy values. Everything else is truthy.
   *
   * @param object object to determine truthiness
   * @return true if truthy
   */
  private boolean isTruthy(Object object) {
    if (object == null)
      return false;
    if (object instanceof Boolean)
      return (boolean) object;
    return true;
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null)
      return true;
    if (a == null)
      return false;

    return a.equals(b);
  }

  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double)
      return;

    throw new RuntimeError(operator, "Operand must be a number.");
  }

  private void checkNumberOperands(Token operator, Object lhs, Object rhs) {
    if (lhs instanceof Double && rhs instanceof Double)
      return;

    throw new RuntimeError(operator, "Operands must be numbers.");
  }

  private String stringify(Object object) {
    if (object == null)
      return "nil";

    if (object instanceof Double) {
      String text = object.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }

      return text;
    }

    return object.toString();
  }

  /**
   * Execute given list of statements in given environment.
   *
   * @param statements
   * @param environment
   */
  void executeBlock(List<Stmt> statements, Environment environment) {
    Environment previous = this.environment;

    try {
      this.environment = environment;
      for (Stmt statement : statements) {
        execute(statement);
      }
    } finally {
      this.environment = previous;
    }
  }
}

package com.bloxi.lox;

import java.util.ArrayList;
import java.util.List;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
  final Environment globals = new Environment();
  private Environment environment = globals;
  // NOTE: These flags are better suited inside an environment
  private boolean insideLoop = false;
  private boolean breakFlag = false;

  Interpreter() {
    // a native function
    globals.define("clock", new LoxCallable() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double) System.currentTimeMillis() / 1000.0;
      }

      @Override
      public String toString() {
        return "<native fn>";
      }
    });
  }

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
    if (isTruthy(evaluate(expr.condition))) {
      return evaluate(expr.trueExpr);
    } else {
      return evaluate(expr.falseExpr);
    }
  }

  @Override
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression);
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
  public Object visitLogicalExpr(Expr.Logical expr) {
    Object lhs = evaluate(expr.left);

    // short circuiting
    if (expr.operator.type == TokenType.OR) {
      if (isTruthy(lhs))
        return lhs;
    } else {
      if (!isTruthy(lhs))
        return lhs;
    }

    return evaluate(expr.right);
  }

  @Override
  public Object visitCallExpr(Expr.Call expr) {
    Object callee = evaluate(expr.callee);

    List<Object> arguments = new ArrayList<>();
    for (Expr argument : expr.arguments) {
      arguments.add(evaluate(argument));
    }

    if (!(callee instanceof LoxCallable)) {
      throw new RuntimeError(expr.paren, "Can only call functions and classes.");
    }

    LoxCallable function = (LoxCallable) callee;
    if (arguments.size() != function.arity()) {
      String errorMsg = String.format("Expected %d arguments but got %d.", function.arity(), arguments.size());
      throw new RuntimeError(expr.paren, errorMsg);
    }
    return function.call(this, arguments);
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
  public Void visitBlockStmt(Stmt.Block stmt) {
    executeBlock(stmt.statements, new Environment(environment));
    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    if (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.thenBranch);
    } else if (stmt.elseBranch != null) {
      execute(stmt.elseBranch);
    }
    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    insideLoop = true;
    while (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.body);
      if (breakFlag) {
        breakFlag = false;
        break;
      }
    }
    insideLoop = false;
    return null;
  }

  @Override
  public Void visitBreakStmt(Stmt.Break stmt) {
    if (insideLoop) {
      breakFlag = true;
    } else {
      throw new RuntimeError(stmt.token, "'break' is allowed only inside loops.");
    }
    return null;
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
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

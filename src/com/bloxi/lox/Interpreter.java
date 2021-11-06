package com.bloxi.lox;

class Interpreter implements Expr.Visitor<Object> {

  void interpret(Expr expression) {
    try {
      Object value = evaluate(expression);
      System.out.println(stringify(value));
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
        return (double) lhs / (double) rhs;
      case PLUS:
        if (lhs instanceof Double && rhs instanceof Double)
          return (double) lhs + (double) rhs;
        if (lhs instanceof String && rhs instanceof String) {
          return (String) lhs + (String) rhs;
        }
        throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings");

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

}

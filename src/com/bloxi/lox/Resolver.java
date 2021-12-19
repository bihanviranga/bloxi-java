package com.bloxi.lox;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.HashMap;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
  private final Interpreter interpreter;
  private final Stack<Map<String, Boolean>> scopes = new Stack<>();
  private FunctionType currentFunction = FunctionType.NONE;
  private ClassType currentClass = ClassType.NONE;
  private boolean insideLoop = false;

  Resolver(Interpreter interpreter) {
    this.interpreter = interpreter;
  }

  private enum FunctionType {
    NONE, FUNCTION, METHOD, INITIALIZER
  }

  private enum ClassType {
    NONE, CLASS
  }

  @Override
  public Void visitVariableExpr(Expr.Variable expr) {
    if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
      Lox.error(expr.name, "Can't read local variable in its own initializer.");
    }

    resolveLocal(expr, expr.name);
    return null;
  }

  @Override
  public Void visitAssignExpr(Expr.Assign expr) {
    resolve(expr.value);
    resolveLocal(expr, expr.name);
    return null;
  }

  @Override
  public Void visitBinaryExpr(Expr.Binary expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitCallExpr(Expr.Call expr) {
    resolve(expr.callee);

    for (Expr argument : expr.arguments) {
      resolve(argument);
    }

    return null;
  }

  @Override
  public Void visitGroupingExpr(Expr.Grouping expr) {
    resolve(expr.expression);
    return null;
  }

  @Override
  public Void visitLiteralExpr(Expr.Literal expr) {
    // Literals don't mention variables or contain any subexpressions.
    return null;
  }

  @Override
  public Void visitLogicalExpr(Expr.Logical expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitUnaryExpr(Expr.Unary expr) {
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitTernaryExpr(Expr.Ternary expr) {
    resolve(expr.condition);
    resolve(expr.trueExpr);
    resolve(expr.falseExpr);
    return null;
  }

  @Override
  public Void visitGetExpr(Expr.Get expr) {
    // Property dispatch in Lox is dynamic
    // so we don't check here if the property name exists.
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visitSetExpr(Expr.Set expr) {
    resolve(expr.value);
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visitThisExpr(Expr.This expr) {
    if (currentClass == ClassType.NONE)
      Lox.error(expr.keyword, "Can't use 'this' outside of a class.");

    resolveLocal(expr, expr.keyword);
    return null;
  }

  @Override
  public Void visitClassStmt(Stmt.Class stmt) {
    ClassType enclosingClass = currentClass;
    currentClass = ClassType.CLASS;

    declare(stmt.name);
    define(stmt.name);

    // A class can't inherit from itself
    if (stmt.superclass != null && stmt.name.lexeme.equals(stmt.superclass.name.lexeme))
      Lox.error(stmt.superclass.name, "A class can't inherit from itself.");

    if (stmt.superclass != null)
      resolve(stmt.superclass);

    beginScope();
    scopes.peek().put("this", true);

    for (Stmt.Function method : stmt.methods) {
      FunctionType declaration = FunctionType.METHOD;
      if (method.name.lexeme.equals("init"))
        declaration = FunctionType.INITIALIZER;

      resolveFunction(method, declaration);
    }

    endScope();

    currentClass = enclosingClass;
    return null;
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    // Defining the name before resolving the body
    // allows a function to call itself.
    declare(stmt.name);
    define(stmt.name);
    resolveFunction(stmt, FunctionType.FUNCTION);
    return null;
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    beginScope();
    resolve(stmt.statements);
    endScope();
    return null;
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    declare(stmt.name);
    if (stmt.initializer != null) {
      resolve(stmt.initializer);
    }
    define(stmt.name);
    return null;
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    resolve(stmt.expression);
    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    resolve(stmt.condition);
    resolve(stmt.thenBranch);
    if (stmt.elseBranch != null)
      resolve(stmt.elseBranch);
    return null;
  }

  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    resolve(stmt.expression);
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    if (currentFunction == FunctionType.NONE)
      Lox.error(stmt.keyword, "Can't return from top-level code.");

    if (stmt.value != null) {
      if (currentFunction == FunctionType.INITIALIZER)
        Lox.error(stmt.keyword, "Can't return a value from an initializer");
      resolve(stmt.value);
    }

    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    resolve(stmt.condition);

    boolean currentlyInsideLoop = insideLoop;
    insideLoop = true;

    // In loops, the body is resolved only once
    resolve(stmt.body);

    insideLoop = currentlyInsideLoop;
    return null;
  }

  @Override
  public Void visitBreakStmt(Stmt.Break stmt) {
    // Nothing to do here!
    if (!insideLoop)
      Lox.error(stmt.token, "'break' is allowed only inside loops.");

    return null;
  }

  void resolve(List<Stmt> statements) {
    for (Stmt statement : statements) {
      resolve(statement);
    }
  }

  /** Calls the visitor on Stmt */
  private void resolve(Stmt stmt) {
    stmt.accept(this);
  }

  /** Calls the visitor on Expr */
  private void resolve(Expr expr) {
    expr.accept(this);
  }

  /** Resolve the variable to a value */
  private void resolveLocal(Expr expr, Token name) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      if (scopes.get(i).containsKey(name.lexeme)) {
        interpreter.resolve(expr, scopes.size() - 1 - i);
        return;
      }
    }
  }

  /** Marks the start of a new scope by pushing a Hashmap to the scopes stack */
  private void beginScope() {
    scopes.push(new HashMap<String, Boolean>());
  }

  /** Marks the end of a scope by popping one off the stack */
  private void endScope() {
    scopes.pop();
  }

  /** Marks a variable as declared */
  private void declare(Token name) {
    if (scopes.isEmpty())
      return;

    Map<String, Boolean> scope = scopes.peek();

    if (scope.containsKey(name.lexeme)) {
      Lox.error(name, "A variable with this name already exists in this scope.");
    }

    scope.put(name.lexeme, false);
  }

  /** Marks a variable as defined */
  private void define(Token name) {
    if (scopes.isEmpty())
      return;
    scopes.peek().put(name.lexeme, true);
  }

  /** Resolves the variables in a function */
  private void resolveFunction(Stmt.Function function, FunctionType type) {
    FunctionType enclosingFunction = currentFunction;
    currentFunction = type;

    beginScope();

    for (Token param : function.params) {
      declare(param);
      define(param);
    }

    resolve(function.body);
    endScope();
    currentFunction = enclosingFunction;
  }
}

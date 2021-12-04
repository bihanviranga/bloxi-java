package com.bloxi.lox;

import java.util.List;

class LoxFunction implements LoxCallable {
  private final Stmt.Function declaration;
  private final Environment closure;

  LoxFunction(Stmt.Function declaration, Environment closure) {
    this.declaration = declaration;
    this.closure = closure;
  }

  LoxFunction bind(LoxInstance instance) {
    Environment environment = new Environment(closure);
    environment.define("this", instance);
    return new LoxFunction(declaration, environment);
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    Environment environment = new Environment(closure);

    // define function params as variables in the environment
    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).lexeme, arguments.get(i));
    }

    // return value is thrown through the stack trace
    // as an exception
    try {
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return returnValue) {
      return returnValue.value;
    }
    return null;
  }

  @Override
  public int arity() {
    return declaration.params.size();
  }

  @Override
  public String toString() {
    return String.format("<fn %s>", declaration.name.lexeme);
  }
}
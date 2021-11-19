package com.bloxi.lox;

import java.util.Map;
import java.util.HashMap;

class Environment {
  final Environment enclosing;
  private final Map<String, Object> values = new HashMap<>();

  Environment() {
    enclosing = null;
  }

  Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  /** Adds a new variable to the environment */
  void define(String name, Object value) {
    values.put(name, value);
  }

  /** Returns a variable from the environment */
  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme);
    }

    // If this isn't the global scope, checking parent scope for var
    if (enclosing != null)
      return enclosing.get(name);

    throw new RuntimeError(name, String.format("Undefined variable '%s'.", name.lexeme));
  }

  /** Assigns a value to an existing variable */
  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      return;
    }

    // If variable is not defined in the current scope,
    // delegate assignment to the parent scope
    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }

    throw new RuntimeError(name, String.format("Assigning to undefined variable: %s", name.lexeme));
  }

  /** Get a variable from nth ancestor of environment */
  Object getAt(int distance, String name) {
    return ancestor(distance).values.get(name);
  }

  /** Change a variable in the nth ancestor of environment */
  void assignAt(int distance, Token name, Object value) {
    ancestor(distance).values.put(name.lexeme, value);
  }

  /** Get the nth ancestor of enviroment */
  Environment ancestor(int distance) {
    Environment environment = this;
    for (int i = 0; i < distance; i++) {
      environment = environment.enclosing;
    }
    return environment;
  }

}

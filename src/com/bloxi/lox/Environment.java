package com.bloxi.lox;

import java.util.Map;
import java.util.HashMap;

class Environment {
  private final Map<String, Object> values = new HashMap<>();

  /** Adds a new variable to the environment */
  void define(String name, Object value) {
    values.put(name, value);
  }

  /** Returns a variable from the environment */
  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme);
    }

    throw new RuntimeError(name, String.format("Undefined variable '%s'.", name.lexeme));
  }

  /** Assigns a value to an existing variable */
  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      return;
    }

    throw new RuntimeError(name, String.format("Assigning to undefined variable: %s", name.lexeme));
  }
}

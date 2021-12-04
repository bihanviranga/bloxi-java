package com.bloxi.lox;

import java.util.Map;
import java.util.HashMap;

class LoxInstance {
  private LoxClass loxClass;
  private final Map<String, Object> fields = new HashMap<>();

  LoxInstance(LoxClass loxClass) {
    this.loxClass = loxClass;
  }

  Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

    LoxFunction method = loxClass.findMethod(name.lexeme);
    if (method != null)
      return method.bind(this);

    throw new RuntimeError(name, String.format("Undefined property '%s'.", name.lexeme));
  }

  void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }

  @Override
  public String toString() {
    return loxClass.name + " instance";
  }
}

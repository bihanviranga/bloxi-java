package com.bloxi.lox;

import java.util.List;

class Parser {
  private static class ParseError extends RuntimeException {
  }

  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  Expr parse() {
    try {
      return expression();
    } catch (ParseError error) {
      return null;
    }
  }

  private Expr expression() {
    return separator();
  }

  private Expr separator() {
    Expr expr = equality();

    while (match(TokenType.COMMA)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr equality() {
    Expr expr = comparison();

    while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr comparison() {
    Expr expr = term();

    while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr term() {
    Expr expr = factor();

    while (match(TokenType.MINUS, TokenType.PLUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr factor() {
    Expr expr = unary();

    while (match(TokenType.SLASH, TokenType.STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr unary() {
    if (match(TokenType.BANG, TokenType.MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    return primary();
  }

  private Expr primary() {
    if (match(TokenType.FALSE))
      return new Expr.Literal(false);
    if (match(TokenType.TRUE))
      return new Expr.Literal(true);
    if (match(TokenType.NIL))
      return new Expr.Literal(null);

    if (match(TokenType.NUMBER, TokenType.STRING)) {
      return new Expr.Literal(previous().literal);
    }

    if (match(TokenType.LEFT_PAREN)) {
      Expr expr = expression();
      consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }

    // nothing matched
    throw error(peek(), "Expected expression.");
  }

  /**
   * Synchronizing to avoid cascaded errors
   */
  private void synchronize() {
    // consume the faulty token...
    advance();

    // ...and find the start of the next statement.
    while (!isAtEnd()) {
      if (previous().type == TokenType.SEMICOLON)
        return;

      // encountering one of these tokens
      // means the parser is at the start of a new statement
      switch (peek().type) {
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }

      advance();
    }
  }

  /**
   * Logs an error and returns a ParseError object
   *
   * @param token   token with the error
   * @param message error message
   * @return ParseError object
   */
  private ParseError error(Token token, String message) {
    Lox.error(token, message);
    return new ParseError();
  }

  /**
   * Checks if the next token is of the given type. If not, throws and error with
   * the given message. If there's an error, the token is not consumed.
   *
   * @param type    type of token to find
   * @param message error message to throw if not found
   * @return the found token
   */
  private Token consume(TokenType type, String message) {
    if (check(type))
      return advance();

    throw error(peek(), message);
  }

  /**
   * Checks if the current token has any of the given types. If a match is found,
   * consumes the token.
   *
   * @param types types to check
   * @return true if current token is one of the types
   */
  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }

    return false;
  }

  /**
   * Checks if the current token is of the given type. Does not consume the token.
   *
   * @param type type to check
   * @return true if current token is of the given type
   */
  private boolean check(TokenType type) {
    if (isAtEnd())
      return false;
    return peek().type == type;
  }

  /**
   * Consumes and returns the current token.
   *
   * @return current token
   */
  private Token advance() {
    if (!isAtEnd())
      current++;
    return previous();
  }

  /**
   * Checks if parser is at the end of source.
   *
   * @return true if EOF is reached
   */
  private boolean isAtEnd() {
    return peek().type == TokenType.EOF;
  }

  /**
   * Returns the current token without consuming it.
   *
   * @return current token
   */
  private Token peek() {
    return tokens.get(current);
  }

  /**
   * Returns the previous token.
   *
   * @return previous token
   */
  private Token previous() {
    return tokens.get(current - 1);
  }
}

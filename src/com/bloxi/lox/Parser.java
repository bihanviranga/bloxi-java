package com.bloxi.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Parser {
  private static class ParseError extends RuntimeException {
  }

  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }

    return statements;
  }

  private Stmt declaration() {
    try {
      if (match(TokenType.VAR))
        return varDeclaration();

      return statement();
    } catch (ParseError error) {
      // Synchronizing here because it has the lowest precedence
      synchronize();
      return null;
    }
  }

  private Stmt varDeclaration() {
    Token name = consume(TokenType.IDENTIFIER, "Expected variable name.");

    Expr initializer = null;
    if (match(TokenType.EQUAL)) {
      initializer = expression();
    }

    consume(TokenType.SEMICOLON, "Expected ';' after variable declaration.");
    return new Stmt.Var(name, initializer);
  }

  private Stmt statement() {
    if (match(TokenType.PRINT))
      return printStatement();

    if (match(TokenType.LEFT_BRACE))
      return new Stmt.Block(block());

    if (match(TokenType.IF))
      return ifStatement();

    if (match(TokenType.WHILE))
      return whileStatement();

    if (match(TokenType.FOR))
      return forStatement();

    return expressionStatement();
  }

  private Stmt forStatement() {
    consume(TokenType.LEFT_PAREN, "Expected '(' after 'for'.");

    Stmt initializer;
    if (match(TokenType.SEMICOLON)) {
      initializer = null;
    } else if (match(TokenType.VAR)) {
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    }

    Expr condition = null;
    if (!check(TokenType.SEMICOLON))
      condition = expression();
    consume(TokenType.SEMICOLON, "Expected ';' after loop condition.");

    Expr increment = null;
    if (!check(TokenType.RIGHT_PAREN))
      increment = expression();

    consume(TokenType.RIGHT_PAREN, "Expected ')' after for clauses.");

    Stmt body = statement();

    if (increment != null)
      body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));

    if (condition == null)
      condition = new Expr.Literal(true);

    body = new Stmt.While(condition, body);

    if (initializer != null)
      body = new Stmt.Block(Arrays.asList(initializer, body));

    return body;
  }

  private Stmt whileStatement() {
    consume(TokenType.LEFT_PAREN, "Expected '(' after 'while'.");
    Expr condition = expression();
    consume(TokenType.RIGHT_PAREN, "Expected ')' after condition.");
    Stmt body = statement();

    return new Stmt.While(condition, body);
  }

  private Stmt ifStatement() {
    consume(TokenType.LEFT_PAREN, "Expected '(' after 'if'.");
    Expr condition = expression();
    consume(TokenType.RIGHT_PAREN, "Expected ')' after condition.");

    Stmt thenBranch = statement();
    Stmt elseBranch = null;
    if (match(TokenType.ELSE)) {
      elseBranch = statement();
    }

    return new Stmt.If(condition, thenBranch, elseBranch);
  }

  private Stmt printStatement() {
    Expr value = expression();
    consume(TokenType.SEMICOLON, "Expected ';' after print.");
    return new Stmt.Print(value);
  }

  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(TokenType.SEMICOLON, "Expected ';' after expression.");
    return new Stmt.Expression(expr);
  }

  private Expr expression() {
    return separator();
  }

  private Expr separator() {
    Expr expr = assignment();

    while (match(TokenType.COMMA)) {
      Token operator = previous();
      Expr right = assignment();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr assignment() {
    // We are evaluating the lhs first as if it were an expression.
    // Later only we check if it's a Expr.Variable
    Expr expr = conditional();

    if (match(TokenType.EQUAL)) {
      Token equals = previous();
      Expr value = assignment();

      // The lhs can't be any Expr. It must be a Expr.Variable
      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable) expr).name;
        return new Expr.Assign(name, value);
      }

      error(equals, "Cannot assign. Invalid target.");
    }

    return expr;
  }

  private Expr conditional() {
    Expr expr = logical_or();

    while (match(TokenType.QUESTION_MARK)) {
      Expr trueExpr = conditional();
      Expr falseExpr = null;
      while (match(TokenType.COLON)) {
        falseExpr = conditional();
      }

      expr = new Expr.Ternary(expr, trueExpr, falseExpr);
    }

    return expr;
  }

  private Expr logical_or() {
    Expr expr = logical_and();

    while (match(TokenType.OR)) {
      Token operator = previous();
      Expr right = logical_and();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr logical_and() {
    Expr expr = equality();

    while (match(TokenType.AND)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Logical(expr, operator, right);
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

    if (match(TokenType.IDENTIFIER)) {
      return new Expr.Variable(previous());
    }

    if (match(TokenType.LEFT_PAREN)) {
      Expr expr = expression();
      consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.");
      return new Expr.Grouping(expr);
    }

    // nothing matched
    return errorProductions();
  }

  // TODO: right paren without left
  // TODO: ternary should be handled differently
  private Expr errorProductions() {
    // Handling unexpected EOF
    if (isAtEnd()) {
      Token errToken;
      if (current == 0) {
        // immediate EOF means empty source
        errToken = tokens.get(current);
      } else {
        errToken = previous();
      }
      throw error(errToken, "Unexpected EOF");
    }

    Token nextToken = advance();
    switch (nextToken.type) {
      // binary or ternary operators appearing first is an error
      case COMMA:
      case BANG_EQUAL:
      case EQUAL_EQUAL:
      case GREATER:
      case GREATER_EQUAL:
      case LESS:
      case LESS_EQUAL:
      case PLUS:
      case SLASH:
      case STAR:
        // consume next expression for binary
        expression();
        throw error(nextToken, "Binary operator expected a left-hand operand.");
      default:
        throw error(nextToken, "Expected expression.");
    }
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

  /**
   * Returns a list of statements until a closing brace is encountered.
   *
   * @return list of statements
   */
  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<>();

    while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(TokenType.RIGHT_BRACE, "Expected '}' after block.");
    return statements;
  }
}

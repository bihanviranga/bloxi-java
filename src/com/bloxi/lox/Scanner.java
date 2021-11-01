package com.bloxi.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;

  /** This hashmap maps strings to token types */
  private static final Map<String, TokenType> keywords;
  static {
    keywords = new HashMap<>();
    keywords.put("and", TokenType.AND);
    keywords.put("class", TokenType.CLASS);
    keywords.put("else", TokenType.ELSE);
    keywords.put("false", TokenType.FALSE);
    keywords.put("for", TokenType.FOR);
    keywords.put("fun", TokenType.FUN);
    keywords.put("if", TokenType.IF);
    keywords.put("nil", TokenType.NIL);
    keywords.put("or", TokenType.OR);
    keywords.put("print", TokenType.PRINT);
    keywords.put("return", TokenType.RETURN);
    keywords.put("super", TokenType.SUPER);
    keywords.put("this", TokenType.THIS);
    keywords.put("true", TokenType.TRUE);
    keywords.put("var", TokenType.VAR);
    keywords.put("while", TokenType.WHILE);
  }

  Scanner(String source) {
    this.source = source;
  }

  /**
   * Scans all tokens in source until EOF
   */
  List<Token> scanTokens() {
    while (!isAtEnd()) {
      // At the beginning of the next lexeme
      start = current;
      scanToken();
    }

    tokens.add(new Token(TokenType.EOF, "", null, line));
    return tokens;
  }

  /**
   * Moves current pointer forward and scans the token that begins there
   */
  private void scanToken() {
    char c = advance();
    switch (c) {
      // single char tokens
      case '(':
        addToken(TokenType.LEFT_PAREN);
        break;
      case ')':
        addToken(TokenType.RIGHT_PAREN);
        break;
      case '{':
        addToken(TokenType.LEFT_BRACE);
        break;
      case '}':
        addToken(TokenType.RIGHT_BRACE);
        break;
      case ',':
        addToken(TokenType.COMMA);
        break;
      case '.':
        addToken(TokenType.DOT);
        break;
      case '-':
        addToken(TokenType.MINUS);
        break;
      case '+':
        addToken(TokenType.PLUS);
        break;
      case ';':
        addToken(TokenType.SEMICOLON);
        break;
      case '*':
        addToken(TokenType.STAR);
        break;

      // double char tokens
      case '!':
        addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
        break;
      case '=':
        addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
        break;
      case '<':
        addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
        break;
      case '>':
        addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
        break;
      case '/':
        if (match('/')) {
          // this is a comment
          while (peek() != '\n' && !isAtEnd())
            advance();
        } else if (match('*')) {
          // block comment
          blockComment();
        } else {
          addToken(TokenType.SLASH);
        }
        break;

      // whitespace and newlines
      case ' ':
      case '\r':
      case '\t':
        break;
      case '\n':
        line++;
        break;

      // strings
      case '"':
        string();
        break;

      default:
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          // Any lexeme starting with a letter or underscore is an identifier,
          // including reserved words
          identifier();
        } else {
          Lox.error(line, String.format("Unexpected character:  %c", c));
        }
        break;
    }
  }

  /**
   * Adds a token without a literal to tokens list.
   *
   * @param type type of token to add
   */
  private void addToken(TokenType type) {
    addToken(type, null);
  }

  /**
   * Adds a token to the tokens list
   *
   * @param type    type of the token
   * @param literal value of the token
   */
  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }

  /**
   * Checks if the next char in source matches a given value. Consumes character.
   *
   * @param expected expected char
   * @return true if next char == expected, false if otherwise. Also false if EOF
   */
  private boolean match(char expected) {
    if (isAtEnd())
      return false;
    if (source.charAt(current) != expected)
      return false;

    current++;
    return true;
  }

  /**
   * Returns the next char of source and increments current char pointer.
   *
   * @return next char of source
   */
  private char advance() {
    return source.charAt(current++);
  }

  /**
   * Read the next char of source without incrementing the current pointer
   */
  private char peek() {
    if (isAtEnd())
      return '\0';
    return source.charAt(current);
  }

  /**
   * Peek two chars without incrementing the current pointer.
   *
   * @return char after the next, or '\0' if EOF
   */
  private char peekNext() {
    if (current + 1 >= source.length())
      return '\0';
    return source.charAt(current + 1);
  }

  /**
   * Determines if `current` is at the end of the source.
   *
   * @return true if at the end of source
   */
  private boolean isAtEnd() {
    return current >= source.length();
  }

  /**
   * Checks if the given char is a digit
   *
   * @param c char to check
   * @return true if char represents a digit
   */
  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  /**
   * Determines whether a char represents an alphabet character or underscore
   *
   * @param c char to check
   * @return true if c is in the alphabet or is "_"
   */
  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  /**
   * Determines whether a char is an alphanumeric character or underscore
   *
   * @param c char to check
   * @return true if c is alphanumeric of "_"
   */
  private boolean isAlphaNumberic(char c) {
    return isAlpha(c) || isDigit(c);
  }

  /**
   * Finds the end of a string literal and adds it as a token. The end is
   * signalled when this method finds the matching '"'. If the source ends before
   * finding the matching '"', its an unterminated string error.
   */
  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n')
        line++;
      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, String.format("Unterminated string: %s", source.substring(start, current)));
      return;
    }

    // Consume the closing ".
    advance();

    // Trim the surrounding quotes
    String value = source.substring(start + 1, current - 1);
    addToken(TokenType.STRING, value);
  }

  /**
   * Parses a number and adds it as a token.
   */
  private void number() {
    // Consume all the digits
    while (isDigit(peek()))
      advance();

    // Look for a fractional part, and if there's
    // numbers after it.
    if (peek() == '.' && isDigit(peekNext())) {
      // consume the "."
      advance();

      // consume the rest of the numbers
      while (isDigit(peek()))
        advance();
    }

    addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  /**
   * Parses an alphanumeric identifier and adds it as a token
   */
  private void identifier() {
    while (isAlphaNumberic(peek()))
      advance();

    // If the scanned identifier is a keyword, set its type.
    // Otherwise, it's IDENTIFIER, meaning its a user-defined variable.
    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if (type == null)
      type = TokenType.IDENTIFIER;
    addToken(type);
  }

  /**
   * Advances until the end of block comments, incrementing line as needed
   */
  private void blockComment() {
    // Peek until '*' or EOF is found
    while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) {
      if (peek() == '\n')
        line++;
      advance();
    }

    // If EOF is found first, its an error
    if (isAtEnd()) {
      Lox.error(line, String.format("Unterminated block comment: %s", source.substring(start, current)));
      return;
    }

    // If '*' is found, and the next is a '/', block comment ends
    if (peek() == '*' && peekNext() == '/') {
      // consume these two
      advance();
      advance();
    }
  }
}

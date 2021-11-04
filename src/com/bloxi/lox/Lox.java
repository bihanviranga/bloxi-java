package com.bloxi.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
  static boolean hadError;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: bloxi [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));

    if (hadError)
      System.exit(65);
  }

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) {
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null)
        break;
      run(line);
      // Reset the error marker on each loop
      hadError = false;
    }
  }

  private static void run(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    // For now just print the tokens
    // for (Token token : tokens) {
    // System.out.println(token);
    // }

    Parser parser = new Parser(tokens);
    Expr expression = parser.parse();

    // Stop if there was a syntax error
    if (hadError)
      return;

    // For now just print the AST nodes
    AstPrinter printer = new AstPrinter();
    System.out.println(printer.print(expression));

  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  static void error(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line, "at EOF", message);
    } else {
      report(token.line, String.format("at %s", token.lexeme), message);
    }
  }

  private static void report(int line, String where, String message) {
    System.err.println(String.format("[line %d] Error %s: %s", line, where, message));
    hadError = true;
  }
}

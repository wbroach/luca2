package com.luca;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.luca.TokenType.EOF;

public class Luca {
  static boolean hadError = false;
  static boolean hadRuntimeError = false;
  private static final Interpreter interpreter = new Interpreter();

  public static void main(String[] args) throws IOException {
  if (args.length > 1) {
      System.out.println("Usage: luca [script]");
      System.exit(64);
    }
    else if (args.length == 1) {
      runFile(args[0]);
    }
    else {
      runPrompt();
    }
  }

  private static void run(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();
    Parser parser = new Parser(tokens);
    Expr expression = parser.parse();

    if (hadError) { return; }

    interpreter.interpret(expression);
  }

  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));

    if (hadError) { System.exit(65); }
    if (hadRuntimeError) { System.exit(70); }
  }

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    while (true) {
      System.out.print("> ");
      String line = reader.readLine();
      if (Objects.isNull(line)) { break; }
      run(line);
      hadError = false;
    }
  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  static void error(Token token, String message)  {
    if (token.type == EOF) {
      report(token.line, " at end", message);
    }
    else {
      report(token.line, " at '" + token.lexeme + "'", message);
    }
  }

  static void runtimeError(RuntimeError error) {
    System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
    hadRuntimeError = true;
  }

  private static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }
}

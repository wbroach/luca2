package com.luca;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.luca.TokenType.*;

@RequiredArgsConstructor
class Scanner {
  private static final Map<String, TokenType> keywords = Map.ofEntries(
    Map.entry("true", TRUE),
    Map.entry("false", FALSE),
    Map.entry("if", IF),
    Map.entry("else", ELSE),
    Map.entry("and", AND),
    Map.entry("or", OR),
    Map.entry("class", CLASS),
    Map.entry("super", SUPER),
    Map.entry("nil", NIL),
    Map.entry("print", PRINT),
    Map.entry("func", FUNC),
    Map.entry("return", RETURN),
    Map.entry("this", THIS),
    Map.entry("var", VAR),
    Map.entry("for", FOR),
    Map.entry("while", WHILE)
  );

  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;

  List<Token> scanTokens() {
    while (isNotAtEnd()) {
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, StringUtils.EMPTY, null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(': addToken(LEFT_PAREN); break;
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      case '-': addToken(MINUS); break;
      case '+': addToken(PLUS); break;
      case ';': addToken(SEMICOLON); break;
      case '*': addToken(STAR); break;
      case '!':
        addToken(match('=') ? BANG_EQUAL : BANG);
        break;
      case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
        break;
      case '<':
        addToken(match('=') ? LESS_EQUAL : LESS);
        break;
      case '>':
        addToken(match('=') ? GREATER_EQUAL : GREATER);
        break;
      case '/':
        if (match('/')) {
          while(peek() != '\n' && isNotAtEnd()) { advance(); }
        }
        else {
          addToken(SLASH);
        }
        break;

      case ' ':
      case '\r':
      case '\t':
        break;
      case '\n':
        ++line;
        break;

      case '"': string(); break;

      default:
        if (isDigit(c)) {
          number();
        }
        else if (isAlpha(c)) {
          identifier();
        }
        else {
          Luca.error(line, "Unexpected character.");
        }
    }
  }

  private char advance() {
    return source.charAt(current++);
  }

  private char peek() {
    if (isAtEnd()) { return '\0'; }
    return source.charAt(current);
  }

  private char peekNext() {
    if (current + 1 >= source.length()) { return '\0'; }
    return source.charAt(current + 1);
  }

  private boolean match(char expected) {
    if (isAtEnd() || source.charAt(current) != expected) {
      return false;
    }
    else {
      ++current;
      return true;
    }
  }

  private void string() {
    while (peek() != '"' && isNotAtEnd()) {
      if (peek() == '\n') { ++line; }
      advance();
    }

    if (isAtEnd()) {
      Luca.error(line, "Unterminated string.");
      return;
    }

    advance(); // consume the closing '"' character

    addToken(STRING, source.substring(start + 1, current - 1));
  }

  private void number() {
    while (isDigit(peek())) { advance(); }

    if (peek() == '.' && isDigit(peekNext())) {
      advance(); // consume '.' character
      while (isDigit(peek())) { advance(); }
    }

    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) { advance(); }

    String text = source.substring(start, current);

    addToken(keywords.getOrDefault(text, IDENTIFIER));
  }

  private void addToken(TokenType tokenType) {
    addToken(tokenType, null);
  }

  private void addToken(TokenType tokenType, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(tokenType, text, literal, line));
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isAlpha(char c) {
    return isUpperCase(c) || isLowerCase(c) || isUnderscore(c);
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  private boolean isUpperCase(char c) {
    return c >= 'A' && c <= 'Z';
  }

  private boolean isLowerCase(char c) {
    return c >= 'a' && c <= 'z';
  }

  private boolean isUnderscore(char c) {
    return c == '_';
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private boolean isNotAtEnd() {
    return !isAtEnd();
  }
}

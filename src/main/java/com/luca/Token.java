package com.luca;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
class Token {
  final TokenType type;
  final String lexeme;
  final Object literal;
  final int line;

  @Override
  public String toString() {
    return type + StringUtils.SPACE + lexeme + StringUtils.SPACE + literal;
  }
}

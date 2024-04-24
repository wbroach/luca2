package com.luca;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Builder
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

package com.luca;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Supplier;

import static com.luca.TokenType.*;

@RequiredArgsConstructor
public class Parser {
	private final List<Token> tokens;
	private int current = 0;

	private Expr expression() {
		return equality();
	}

	private Expr equality() {
		return parseBinOp(this::comparison, List.of(BANG_EQUAL, EQUAL_EQUAL));
	}

	private Expr comparison() {
		return parseBinOp(this::term, List.of(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL));
	}

	private Expr term() {
		return parseBinOp(this::factor, List.of(MINUS, PLUS));
	}

	private Expr factor() {
		return parseBinOp(this::unary, List.of(STAR, SLASH));
	}

	private Expr unary() {
		if (match(List.of(BANG, MINUS))) {
			Token operator = previous();
			Expr right = primary();
			return new Expr.Unary(operator, right);
		}

		return primary();
	}

	private Expr primary() {

	}

	private Expr parseBinOp(Supplier<Expr> exprType, List<TokenType> opTypes) {
		Expr expr = exprType.get();
		while(match(opTypes)) {
			Token operator = previous();
			Expr right = exprType.get();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private boolean match(List<TokenType> types) {
		for (TokenType type : types) {
			if (check(type)) {
				advance();
				return true;
			}
		}
		return false;
	}

	private boolean check(TokenType type) {
		if (isAtEnd()) {
			return false;
		}
		return peek().type == type;
	}

	private Token advance() {
		if (isNotAtEnd()) {
			++current;
		}
		return previous();
	}

	private Token previous() {
		return tokens.get(current - 1);
	}

	private Token peek() {
		return tokens.get(current);
	}

	private boolean isAtEnd() {
		return peek().type == EOF;
	}

	private boolean isNotAtEnd() {
		return !isAtEnd();
	}
}

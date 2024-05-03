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
//		Expr expr = comparison();
//		while (match(BANG_EQUAL, EQUAL_EQUAL)) {
//			Token operator = previous();
//			Expr right = comparison();
//			expr = new Expr.Binary(expr, operator, right);
//		}
//
//		return expr;
		return binOp(this::comparison, List.of(BANG_EQUAL, EQUAL_EQUAL));
	}

	private Expr comparison() {
	}

	private Expr binOp(Supplier<Expr> exprSupplier, List<TokenType> opTypes) {
		Expr expr = exprSupplier.get();
		while(match(opTypes)) {
			Token operator = previous();
			Expr right = exprSupplier.get();
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

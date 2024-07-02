package com.luca;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.luca.TokenType.*;

@RequiredArgsConstructor
public class Parser {
	private static final int MAX_PARAMS_ARGS = 255;
	private static class ParseError extends RuntimeException {}

	private final List<Token> tokens;
	private int current = 0;

	List<Stmt> parse() {
		List<Stmt> statements = new ArrayList<>();
		while (isNotAtEnd()) {
			statements.add(declaration());
		}
		return statements;
	}

	private Stmt declaration() {
		try {
			if (match(VAR)) { return varDeclaration(); }
			if (match(FUNC)) { return function("function"); }

			return statement();
		} catch (ParseError e) {
			synchronize();
			return null;
		}
	}

	private Stmt.Function function(String kind) {
		Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
		consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
		List<Token> parameters = new ArrayList<>();
		if (!check(RIGHT_PAREN)) {
			do {
				if (parameters.size() >= MAX_PARAMS_ARGS) {
					error(peek(), "Can't have more than " + MAX_PARAMS_ARGS + " parameters.");
				}
				parameters.add(consume(IDENTIFIER, "Expect parameter name."));
			} while (match(COMMA));
		}
		consume(RIGHT_PAREN, "Expect ')' after parameters.");
		consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
		List<Stmt> body = block();
		return new Stmt.Function(name, parameters, body);
	}

	private Stmt varDeclaration() {
		Token name = consume(IDENTIFIER, "Expect variable name.");

		Expr initializer = null;
		if (match(EQUAL)) {
			initializer = expression();
		}

		consume(SEMICOLON, "Expect ';' after variable declaration.");
		return new Stmt.Var(name, initializer);
	}

	private Stmt statement() {
		if (match(IF)) {
			return ifStatement();
		}
		else if (match(PRINT)) {
			return printStatement();
		}
		else if (match(WHILE)) {
			return whileStatement();
		}
		else if (match(FOR)) {
			return forStatement();
		}
		else if (match(LEFT_BRACE)) {
			return new Stmt.Block(block());
		}
		else {
			return expressionStatement();
		}
	}

	private Stmt ifStatement() {
		consume(LEFT_PAREN, "Expect '(' after 'if'.");
		Expr condition = expression();
		consume(RIGHT_PAREN, "Expect ')' after if condition.");

		Stmt thenBranch = statement();
		Stmt elseBranch = match(ELSE) ? statement() : null;

		return new Stmt.If(condition, thenBranch, elseBranch);
	}

	private Stmt printStatement() {
		Expr value = expression();
		consume(SEMICOLON, "Expect ';' after value.");
		return new Stmt.Print(value);
	}

	private Stmt whileStatement() {
		consume(LEFT_PAREN, "Expect '(' after 'while'.");
		Expr condition = expression();
		consume(RIGHT_PAREN, "Expect ')' after condition.");
		Stmt body = statement();

		return new Stmt.While(condition, body);
	}

	private Stmt forStatement() {
		consume(LEFT_PAREN, "Expect '(' after 'for'.");

		Stmt initializer;
		if (match(SEMICOLON)) {
			initializer = null;
		}
		else if (match(VAR)) {
			initializer = varDeclaration();
		}
		else {
			initializer = expressionStatement();
		}

		Expr condition = null;
		if(!check(SEMICOLON)) {
			condition = expression();
		}
		consume(SEMICOLON, "Expect';' after loop condition.");

		Expr increment = null;
		if(!check(RIGHT_PAREN)) {
			increment = expression();
		}
		consume(RIGHT_PAREN, "Expect ')' after for clauses.");

		Stmt body = statement();

		if (increment != null) {
			body = new Stmt.Block(List.of(body, new Stmt.Expression(increment)));
		}

		if (condition == null) {
			condition = new Expr.Literal(true);
		}

		body = new Stmt.While(condition, body);

		if (initializer != null) {
			body = new Stmt.Block(List.of(initializer, body));
		}

		return body;
	}

	private List<Stmt> block() {
		List<Stmt> statements = new ArrayList<>();

		while (!check(RIGHT_BRACE) && isNotAtEnd()) {
			statements.add(declaration());
		}

		consume(RIGHT_BRACE, "Expect '}' after block.");
		return statements;
	}

	private Stmt expressionStatement() {
		Expr value = expression();
		consume(SEMICOLON, "Expect ';' after value.");
		return new Stmt.Expression(value);
	}

	private Expr expression() {
		return assignment();
	}

	private Expr assignment() {
		Expr expr = or();

		if (match(EQUAL)) {
			Token equals = previous();
			Expr value = assignment();

			if (expr instanceof Expr.Variable) {
				Token name = ((Expr.Variable)expr).name;
				return new Expr.Assign(name, value);
			}

			error(equals, "Invalid assignment target.");
		}

		return expr;
	}

	private Expr or() {
		return parseLogicalOp(this::and, OR);
	}

	private Expr and() {
		return parseLogicalOp(this::equality, AND);
	}

	private Expr parseLogicalOp(Supplier<Expr> exprType, TokenType opType) {
		Expr expr = exprType.get();
		while(match(opType)) {
			Token operator = previous();
			Expr right = exprType.get();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
	}

	private Expr equality() {
		return parseBinOp(this::comparison, List.of(BANG_EQUAL, EQUAL_EQUAL));
	}

	private Expr comparison() {
		return parseBinOp(this::term, List.of(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL));
	}

	private Expr term() {
		return parseBinOp(this::factor, List.of(PLUS, MINUS));
	}

	private Expr factor() {
		return parseBinOp(this::unary, List.of(STAR, SLASH));
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

	private Expr unary() {
		if (match(List.of(BANG, MINUS))) {
			Token operator = previous();
			Expr right = primary();
			return new Expr.Unary(operator, right);
		}

		return call();
	}

	private Expr call() {
		Expr expr = primary();

		while (true) {
			if (match(LEFT_PAREN)) {
				expr = finishCall(expr);
			}
			else {
				break;
			}
		}

		return expr;
	}

	private Expr finishCall(Expr callee) {
		List<Expr> arguments = new ArrayList<>();
		if (!check(RIGHT_PAREN)) {
			do {
				if (arguments.size() >= MAX_PARAMS_ARGS) {
					error(peek(), "Can't have more than " + MAX_PARAMS_ARGS + " arguments");
				}
				arguments.add(expression());
			} while (match(COMMA));
		}

		Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

		return new Expr.Call(callee, paren, arguments);
	}

	private Expr primary() {
		if (match(FALSE)) {
			return new Expr.Literal(false);
		}
		else if (match(TRUE)) {
			return new Expr.Literal(true);
		}
		else if (match(NIL)) {
			return new Expr.Literal(null);
		}
		else if (match(List.of(NUMBER, STRING))) {
			return new Expr.Literal(previous().literal);
		}
		else if (match(IDENTIFIER)) {
			return new Expr.Variable(previous());
		}
		else if (match(LEFT_PAREN)) {
			Expr expr = expression();
			consume(RIGHT_PAREN, "Expect ')' after expression.");
			return new Expr.Grouping(expr);
		}
		else {
			throw error(peek(), "Expect expression.");
		}
	}

	private boolean match(TokenType type) {
		return match(List.of(type));
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

	private Token consume(TokenType type, String message) {
		if (check(type)) {
			return advance();
		}
		else {
			throw error(peek(), message);
		}
	}

	private ParseError error(Token token, String message) {
		Luca.error(token, message);
		return new ParseError();
	}

	private void synchronize() {
		advance();
		while (isNotAtEnd()) {
			if (previous().type == SEMICOLON) { return; }

			if (checkKeyword(peek().type)) { return; }

			advance();
		}
	}

	private boolean checkKeyword(TokenType type) {
		return type == CLASS || type == FOR || type == FUNC
						|| type == IF || type == PRINT || type == RETURN
						|| type == VAR || type == WHILE;
	}

	private boolean isAtEnd() {
		return peek().type == EOF;
	}

	private boolean isNotAtEnd() {
		return !isAtEnd();
	}
}

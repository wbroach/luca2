package com.luca;

import lombok.RequiredArgsConstructor;

abstract class Stmt {

	interface Visitor<R> {
		R visitExpressionStmt(Expression stmt);
		R visitPrintStmt(Print stmt);
		R visitVarStmt(Var stmt);
	}

	abstract <R> R accept(Visitor<R> visitor);

	@RequiredArgsConstructor
	static class Expression extends Stmt {
		final Expr expression;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitExpressionStmt(this);
		}
	}

	@RequiredArgsConstructor
	static class Print extends Stmt {
		final Expr expression;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitPrintStmt(this);
		}
	}

	@RequiredArgsConstructor
	static class Var extends Stmt {
		final Token name;
		final Expr initializer;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVarStmt(this);
		}
	}

}
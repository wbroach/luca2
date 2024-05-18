package com.luca;

import lombok.RequiredArgsConstructor;

abstract class Stmt {

	interface Visitor<R> {
		R visitExpressionStmt(Expression stmt);
		R visitPrintStmt(Print stmt);
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

}

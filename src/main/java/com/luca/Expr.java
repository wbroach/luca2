package com.luca;

import lombok.RequiredArgsConstructor;

abstract class Expr {

	interface Visitor<R> {
		R visitAssignExpr(Assign expr);
		R visitBinaryExpr(Binary expr);
		R visitGroupingExpr(Grouping expr);
		R visitLiteralExpr(Literal expr);
		R visitUnaryExpr(Unary expr);
		R visitVariableExpr(Variable expr);
	}

	abstract <R> R accept(Visitor<R> visitor);

	@RequiredArgsConstructor
	static class Assign extends Expr {
		final Token name;
		final Expr value;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitAssignExpr(this);
		}
	}

	@RequiredArgsConstructor
	static class Binary extends Expr {
		final Expr left;
		final Token operator;
		final Expr right;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBinaryExpr(this);
		}
	}

	@RequiredArgsConstructor
	static class Grouping extends Expr {
		final Expr expression;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGroupingExpr(this);
		}
	}

	@RequiredArgsConstructor
	static class Literal extends Expr {
		final Object value;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteralExpr(this);
		}
	}

	@RequiredArgsConstructor
	static class Variable extends Expr {
		final Token name;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVariableExpr(this);
		}
	}

	@RequiredArgsConstructor
	static class Unary extends Expr {
		final Token operator;
		final Expr right;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitUnaryExpr(this);
		}
	}
}

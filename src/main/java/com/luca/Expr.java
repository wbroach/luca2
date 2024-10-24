package com.luca;

import lombok.RequiredArgsConstructor;

import java.util.List;

abstract class Expr {

	interface Visitor<R> {
		R visitAssignExpr(Assign expr);
		R visitBinaryExpr(Binary expr);
		R visitCallExpr(Call expr);
		R visitGetExpr(Get expr);
		R visitGroupingExpr(Grouping expr);
		R visitLiteralExpr(Literal expr);
		R visitLogicalExpr(Logical expr);
		R visitSetExpr(Set expr);
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
	static class Call extends Expr {
		final Expr callee;
		final Token paren;
		final List<Expr> arguments;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitCallExpr(this);
		}
	}

	@RequiredArgsConstructor
	static class Get extends Expr {
		final Expr object;
		final Token name;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGetExpr(this);
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
	static class Logical extends Expr {
		final Expr left;
		final Token operator;
		final Expr right;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLogicalExpr(this);
		}
	}

	@RequiredArgsConstructor
	static class Set extends Expr {
		final Expr object;
		final Token name;
		final Expr value;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitSetExpr(this);
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

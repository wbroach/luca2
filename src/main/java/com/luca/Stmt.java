package com.luca;

import lombok.RequiredArgsConstructor;

import java.util.List;

abstract class Stmt {

	interface Visitor<R> {
		R visitBlockStmt(Block stmt);
		R visitExpressionStmt(Expression stmt);
		R visitIfStmt(If stmt);
		R visitPrintStmt(Print stmt);
		R visitVarStmt(Var stmt);
		R visitWhileStmt(While stmt);
	}

	abstract <R> R accept(Visitor<R> visitor);

	@RequiredArgsConstructor
	static class Block extends Stmt {
		final List<Stmt> statements;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBlockStmt(this);
		}
	}

	@RequiredArgsConstructor
	static class Expression extends Stmt {
		final Expr expression;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitExpressionStmt(this);
		}
	}

	@RequiredArgsConstructor
	static class If extends Stmt {
		final Expr condition;
		final Stmt thenBranch;
		final Stmt elseBranch;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitIfStmt(this);
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

	@RequiredArgsConstructor
	static class While extends Stmt {
		final Expr condition;
		final Stmt body;

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitWhileStmt(this);
		}
	}

}

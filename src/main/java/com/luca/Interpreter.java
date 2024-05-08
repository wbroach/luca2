package com.luca;

import java.util.Objects;

public class Interpreter implements Expr.Visitor<Object> {

	@Override
	public Object visitBinaryExpr(Expr.Binary expr) {
		Object left = evaluate(expr.left);
		Object right = evaluate(expr.right);

		switch (expr.operator.type) {
			case GREATER:
				return (double)left > (double)right;
			case GREATER_EQUAL:
				return (double)left >= (double)right;
			case LESS:
				return (double)left < (double)right;
			case LESS_EQUAL:
				return (double)left <= (double)right;
			case MINUS:
				return (double)left - (double)right;
			case PLUS:
				if (additionEligible(left, right)) {
					return (double)left + (double)right;
				}
				else if(concatEligible(left, right)) {
					return (String)left + (String)right;
				}

				break;
			case SLASH:
				return (double)left / (double)right;
			case STAR:
				return (double)left * (double)right;
			case BANG_EQUAL:
				return !isEqual(left, right);
			case EQUAL_EQUAL:
				return isEqual(left, right);
		}

		return null; // unreachable
	}

	@Override
	public Object visitGroupingExpr(Expr.Grouping expr) {
		return this.evaluate(expr);
	}

	@Override
	public Object visitLiteralExpr(Expr.Literal expr) {
		return expr.value;
	}

	@Override
	public Object visitUnaryExpr(Expr.Unary expr) {
		Object right = evaluate(expr.right);

		switch(expr.operator.type) {
			case BANG:
				return !isTruthy(right);
			case MINUS:
				return -(double)right;
		}

		return null; // unreachable
	}

	private Object evaluate(Expr expr) {
		return expr.accept(this);
	}

	private boolean isTruthy(Object object) {
		if (Objects.isNull(object)) {
			return false;
		}
		else if (object instanceof Boolean) {
			return (boolean)object;
		}
		else {
			return true;
		}
	}

	private boolean isEqual(Object left, Object right) {
		if (Objects.isNull(left) && Objects.isNull(right)) {
			return true;
		}
		else if (Objects.isNull(left)) {
			return false;
		}
		else {
			return left.equals(right);
		}
	}

	private boolean additionEligible(Object left, Object right) {
		return left instanceof Double && right instanceof Double;
	}

	private boolean concatEligible(Object left, Object right) {
		return left instanceof String && right instanceof String;
	}
}

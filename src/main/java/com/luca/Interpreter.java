package com.luca;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class Interpreter implements Expr.Visitor<Object> {

	void interpret(Expr expr) {
		try {
			Object value = evaluate(expr);
			System.out.println(stringify(value));
		} catch (RuntimeError error) {
			Luca.runtimeError(error);
		}
	}

	@Override
	public Object visitBinaryExpr(Expr.Binary expr) {
		Object left = evaluate(expr.left);
		Object right = evaluate(expr.right);

		switch (expr.operator.type) {
			case GREATER:
				checkNumberOperand(expr.operator, left, right);
				return (double)left > (double)right;
			case GREATER_EQUAL:
				checkNumberOperand(expr.operator, left, right);
				return (double)left >= (double)right;
			case LESS:
				checkNumberOperand(expr.operator, left, right);
				return (double)left < (double)right;
			case LESS_EQUAL:
				checkNumberOperand(expr.operator, left, right);
				return (double)left <= (double)right;
			case MINUS:
				checkNumberOperand(expr.operator, left, right);
				return (double)left - (double)right;
			case PLUS:
				if (additionEligible(left, right)) {
					return (double)left + (double)right;
				}
				else if (concatEligible(left, right)) {
					return (String)left + (String)right;
				}
				else {
					throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings");
				}
			case SLASH:
				checkNumberOperand(expr.operator, left, right);
				return (double)left / (double)right;
			case STAR:
				checkNumberOperand(expr.operator, left, right);
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
		return this.evaluate(expr.expression);
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
				checkNumberOperand(expr.operator, right);
				return -(double)right;
		}

		return null; // unreachable
	}

	private Object evaluate(Expr expr) {
		return expr.accept(this);
	}

	private void checkNumberOperand(Token operator, Object operand) {
		if (!(operand instanceof Double)) {
			throw new RuntimeError(operator, "Operand must be a number.");
		}
	}

	private void checkNumberOperand(Token operator, Object left, Object right) {
		if (!(left instanceof Double && right instanceof Double)) {
			throw new RuntimeError(operator, "Operands must be numbers");
		}
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

	private String stringify(Object value) {
		if (Objects.isNull(value)) {
			return "nil";
		}
		else if (value instanceof Double) {
			return StringUtils.removeEnd(value.toString(), ".0");
		}
		else {
			return value.toString();
		}
	}

}
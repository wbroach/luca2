package com.luca;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
	final Environment globals = new Environment();
	private Environment environment = globals;
	private final Map<Expr, Integer> locals = new HashMap<>();

	Interpreter() {
		globals.define("clock", new LucaCallable() {
			@Override
			public int arity() {
				return 0;
			}

			@Override
			public Object call(Interpreter interpreter, List<Object> arguments) {
				return (double)System.currentTimeMillis() / 1000.0;
			}

			@Override
			public String toString() {
				return "<native fn>";
			}
		});
	}

	void interpret(List<Stmt> statements) {
		try {
			for (Stmt statement : statements) {
				execute(statement);
			}
		} catch (RuntimeError error) {
			Luca.runtimeError(error);
		}
	}

	void resolve(Expr expr, int depth) {
		locals.put(expr, depth);
	}

	@Override
	public Void visitVarStmt(Stmt.Var stmt) {
		Object value = null;
		if (stmt.initializer != null) {
			value = evaluate(stmt.initializer);
		}

		environment.define(stmt.name.lexeme, value);
		return null;
	}

	@Override

	public Void visitFunctionStmt(Stmt.Function stmt) {
		LucaFunction function = new LucaFunction(stmt, environment);
		environment.define(stmt.name.lexeme, function);
		return null;
	}

	@Override
	public Void visitReturnStmt(Stmt.Return stmt) {
		Object value = null;
		if (stmt.value != null) {
			value = evaluate(stmt.value);
		}

		throw new Return(value);
	}

	@Override
	public Void visitIfStmt(Stmt.If stmt) {
		if (isTruthy(stmt.condition)) {
			execute(stmt.thenBranch);
		}
		else if (stmt.elseBranch != null) {
			execute(stmt.elseBranch);
		}
		return null;
	}

	@Override
	public Void visitPrintStmt(Stmt.Print stmt) {
		Object value = evaluate(stmt.expression);
		System.out.println(stringify(value));
		return null;
	}

	@Override
	public Void visitWhileStmt(Stmt.While stmt) {
		while (isTruthy(evaluate(stmt.condition))) {
			execute(stmt.body);
		}
		return null;
	}

	@Override
	public Void visitBlockStmt(Stmt.Block stmt) {
		executeBlock(stmt.statements, new Environment(this.environment));
		return null;
	}

	void executeBlock(List<Stmt> statements, Environment environment) {
		Environment previous = this.environment;
		try {
			this.environment = environment;
			for (Stmt statement : statements) {
				execute(statement);
			}
		} finally {
			this.environment = previous;
		}
	}

	@Override
	public Void visitExpressionStmt(Stmt.Expression stmt) {
		evaluate(stmt.expression);
		return null;
	}

	@Override
	public Object visitAssignExpr(Expr.Assign expr) {
		Object value = evaluate(expr.value);

		Integer distance = locals.get(expr);
		if (distance != null) {
			environment.assignAt(distance, expr.name, value);
		}
		else {
			globals.assign(expr.name, value);
		}

		return value;
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
	public Object visitCallExpr(Expr.Call expr) {
		Object callee = evaluate(expr.callee);

		List<Object> arguments = new ArrayList<>();
		for (Expr argument : expr.arguments) {
			arguments.add(evaluate(argument));
		}

		if (!(callee instanceof LucaCallable)) {
			throw new RuntimeError(expr.paren, "Can only call functions and classes.");
		}
		LucaCallable function = (LucaCallable) callee;

		if (arguments.size() != function.arity()) {
			throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got "
			+ arguments.size() + ".");
		}
		return function.call(this, arguments);
	}

	@Override
	public Object visitGroupingExpr(Expr.Grouping expr) {
		return evaluate(expr.expression);
	}

	@Override
	public Object visitLiteralExpr(Expr.Literal expr) {
		return expr.value;
	}

	@Override
	public Object visitLogicalExpr(Expr.Logical expr) {
		Object left = evaluate(expr.left);

		if (expr.operator.type == TokenType.OR) {
			if (isTruthy(left)) { return left; }
		}
		else {
			if (!isTruthy(left)) { return left; }
		}

		return evaluate(expr.right);
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

	@Override
	public Object visitVariableExpr(Expr.Variable expr) {
		return lookUpVariable(expr.name, expr);
	}

	private Object lookUpVariable(Token name, Expr expr) {
		return Optional.ofNullable(locals.get(expr))
						.map(distance -> environment.getAt(distance, name.lexeme))
						.orElse(globals.get(name));
	}

	private void execute(Stmt stmt) {
		stmt.accept(this);
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
		if (object == null) {
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
		if (left == null && right == null) {
			return true;
		}
		else if (left == null) {
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
		if (value == null) {
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

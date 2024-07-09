package com.luca;

import java.util.HashMap;
import java.util.Map;


public class Environment {
	final Environment enclosing;
	private static final Map<String, Object> values = new HashMap<>();

	Environment() {
		this.enclosing = null;
	}

	Environment(Environment enclosing) {
		this.enclosing = enclosing;
	}

	Object get(Token name) {
		if (values.containsKey(name.lexeme)) {
			return values.get(name.lexeme);
		}

		if (enclosing != null) {
			return enclosing.get(name);
		}

		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
	}

	Object getAt(int distance, String name) {
		return ancestor(distance).values.get(name);
	}

	private Environment ancestor(int distance) {
		Environment environment = this;
		for (int i = 0; i < distance; ++i) {
			environment = environment.enclosing;
		}

		return environment;
	}

	void define(String name, Object value) {
		values.put(name, value);
	}

	void assign(Token name, Object value) {
		if (values.containsKey(name.lexeme)) {
			values.put(name.lexeme, value);
		}

		if (enclosing != null) {
			enclosing.assign(name, value);
		}

		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
	}
}

package com.luca;

import java.util.HashMap;
import java.util.Map;

class LucaInstance {
	private LucaClass klass;
	private final Map<String, Object> fields = new HashMap<>();

	LucaInstance(LucaClass klass) {
		this.klass = klass;
	}

	Object get(Token name) {
		if (fields.containsKey(name.lexeme)) {
			return fields.get(name.lexeme);
		}

		throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
	}

	void set(Token name, Object value) {
		fields.put(name.lexeme, value);
	}

	@Override
	public String toString() {
		return klass.name + " instance";
	}
}

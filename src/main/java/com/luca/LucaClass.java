package com.luca;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
class LucaClass implements LucaCallable {
	final String name;
	private final Map<String, LucaFunction> methods;

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		LucaInstance instance = new LucaInstance(this);
		return instance;
	}

	@Override
	public int arity() {
		return 0;
	}

	LucaFunction findMethod(String name) {
		if (methods.containsKey(name)) {
			return methods.get(name);
		}

		return null;
	}

	@Override
	public String toString() {
		return name;
	}
}

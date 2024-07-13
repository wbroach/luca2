package com.luca;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
class LucaClass implements LucaCallable {
	final String name;

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		LucaInstance instance = new LucaInstance(this);
		return instance;
	}

	@Override
	public int arity() {
		return 0;
	}

	@Override
	public String toString() {
		return name;
	}
}

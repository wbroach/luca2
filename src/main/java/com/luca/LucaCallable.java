package com.luca;

import java.util.List;

interface LucaCallable {
	int arity();
	Object call(Interpreter interpreter, List<Object> arguments);
}

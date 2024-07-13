package com.luca;

class LucaInstance {
	private LucaClass klass;

	LucaInstance(LucaClass klass) {
		this.klass = klass;
	}

	@Override
	public String toString() {
		return klass.name + " instance";
	}
}

package arden.runtime.validation;

import java.util.Arrays;

import arden.runtime.ArdenDuration;
import arden.runtime.ArdenValue;

public class Call {
	public final ArdenValue[] args;
	public final ArdenValue delay;
	
	public Call(ArdenValue[] args, ArdenValue delay) {
		if (args == null) {
			this.args = new ArdenValue[] {};
		} else {
			this.args = args;
		}

		if (delay == null) {
			this.delay = ArdenDuration.seconds(0, ArdenValue.NOPRIMARYTIME);
		} else {
			this.delay = delay;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Call) {
			Call other = (Call) obj;
			return Arrays.equals(this.args, other.args) && delay.equals(other.delay);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(args) + delay.hashCode();
	}
}

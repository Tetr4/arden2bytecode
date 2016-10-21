package arden.runtime.validation;

import arden.runtime.ArdenValue;

public class EventCall extends Call {
	public final String mapping;

	public EventCall(String mapping, ArdenValue delay) {
		super(null, delay);
		this.mapping = mapping;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EventCall) {
			EventCall other = (EventCall) obj;
			return this.mapping.equalsIgnoreCase(other.mapping) && super.equals(other);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + mapping.toLowerCase().hashCode();
	}
}
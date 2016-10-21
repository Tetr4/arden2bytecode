package arden.runtime.validation;

import arden.runtime.ArdenValue;

public class WrittenMessage {
	public final ArdenValue value;
	public final String destination;

	public WrittenMessage(ArdenValue value, String destination) {
		this.value = value;
		this.destination = destination;
	}
}
package arden.engine;

public abstract class Call implements Runnable, Comparable<Call> {
	public final int priority;

	public Call(int priority) {
		this.priority = priority;
	}

	@Override
	public int compareTo(Call other) {
		return priority - other.priority;
	}
}
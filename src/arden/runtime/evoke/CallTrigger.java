package arden.runtime.evoke;

import arden.runtime.ArdenEvent;
import arden.runtime.ArdenTime;

/** A special trigger, that implies that the MLM was called, by another MLM. */
public final class CallTrigger implements Trigger {
	private final ArdenEvent event;
	private final long delay;

	public CallTrigger(ArdenEvent event, long delay) {
		this.event = event;
		this.delay = delay;
	}
	
	public CallTrigger(ArdenEvent event) {
		this(event, 0);
	}

	public CallTrigger(long delay) {
		this(null, delay);
	}

	public CallTrigger() {
		this(null, 0);
	}

	@Override
	public ArdenTime getNextRunTime() {
		return null;
	}

	@Override
	public boolean runOnEvent(ArdenEvent event) {
		return false;
	}

	@Override
	public void scheduleEvent(ArdenEvent event) {

	}

	@Override
	public ArdenEvent getTriggeringEvent() {
		return event;
	}

	@Override
	public long getDelay() {
		return delay;
	}

}

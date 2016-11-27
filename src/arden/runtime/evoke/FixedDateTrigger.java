package arden.runtime.evoke;

import arden.runtime.ArdenEvent;
import arden.runtime.ArdenTime;

public final class FixedDateTrigger implements Trigger {
	private final ArdenTime date;
	protected boolean triggered = false;

	public FixedDateTrigger(ArdenTime date) {
		this.date = date;
	}

	@Override
	public ArdenTime getNextRunTime() {
		if (triggered) {
			// Don't run again
			return null;
		}
		triggered = true;
		return date;
	}

	@Override
	public boolean runOnEvent(ArdenEvent event) {
		return false;
	}

	@Override
	public void scheduleEvent(ArdenEvent event) {

	}

	public ArdenEvent getTriggeringEvent() {
		// fixed date is not an event
		return null;
	}

	@Override
	public long getDelay() {
		return 0;
	}
}

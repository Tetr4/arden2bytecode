package arden.runtime.evoke;

import arden.runtime.ArdenEvent;
import arden.runtime.ArdenTime;

public final class UntilTrigger implements Trigger {
	private final Trigger cycle;
	private final ArdenTime until; // FIXME Should be a boolean expression

	public UntilTrigger(Trigger cycle, ArdenTime until) {
		this.cycle = cycle;
		this.until = until;
	}

	@Override
	public ArdenTime getNextRunTime() {
		ArdenTime next = cycle.getNextRunTime();
		if (until.compareTo(next) > 0) {
			return next;
		}
		return null;
	}

	@Override
	public boolean runOnEvent(ArdenEvent event) {
		return cycle.runOnEvent(event);
	}

	@Override
	public void scheduleEvent(ArdenEvent event) {
		cycle.scheduleEvent(event);
	}

	public ArdenEvent getTriggeringEvent() {
		return cycle.getTriggeringEvent();
	}

	@Override
	public long getDelay() {
		return cycle.getDelay();
	}
}

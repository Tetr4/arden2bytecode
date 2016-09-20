package arden.runtime.evoke;

import arden.runtime.ArdenEvent;
import arden.runtime.ArdenTime;
import arden.runtime.ExecutionContext;

public class UntilTrigger implements Trigger {
	private Trigger cycle;
	private ArdenTime until; // FIXME Should be a boolean expression

	public UntilTrigger(Trigger cycle, ArdenTime until) {
		this.cycle = cycle;
		this.until = until;
	}

	@Override
	public ArdenTime getNextRunTime(ExecutionContext context) {
		ArdenTime next = cycle.getNextRunTime(context);
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

}

package arden.runtime.evoke;

import java.util.Arrays;
import java.util.List;

import arden.runtime.ArdenEvent;
import arden.runtime.ArdenTime;

public final class AnyTrigger implements Trigger {
	private final List<Trigger> triggers;

	public AnyTrigger(Trigger[] triggers) {
		this.triggers = Arrays.asList(triggers);
	}

	public AnyTrigger(List<Trigger> triggers) {
		this.triggers = triggers;
	}

	@Override
	public ArdenTime getNextRunTime() {
		// Find oldest trigger/event
		ArdenTime oldest = null;
		for (Trigger trigger : triggers) {
			ArdenTime nextRunTime = trigger.getNextRunTime();
			if (nextRunTime != null && (oldest == null || oldest.compareTo(nextRunTime) > 0)) {
				oldest = nextRunTime;
			}
		}

		return oldest;
	}

	@Override
	public boolean runOnEvent(ArdenEvent event) {
		for (Trigger trigger : triggers) {
			if (trigger.runOnEvent(event)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void scheduleEvent(ArdenEvent event) {
		for (Trigger trigger : triggers) {
			trigger.scheduleEvent(event);
		}
	}

	public ArdenEvent getTriggeringEvent() {
		for (Trigger trigger : triggers) {
			ArdenEvent triggeringEvent = trigger.getTriggeringEvent();
			if (triggeringEvent != null) {
				return triggeringEvent;
			}
		}
		return null;
	}

	@Override
	public long getDelay() {
		for (Trigger trigger : triggers) {
			ArdenEvent triggeringEvent = trigger.getTriggeringEvent();
			if (triggeringEvent != null) {
				return trigger.getDelay();
			}
		}
		return 0;
	}
}

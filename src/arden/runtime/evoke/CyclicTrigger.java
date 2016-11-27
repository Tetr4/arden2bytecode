package arden.runtime.evoke;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import arden.runtime.ArdenDuration;
import arden.runtime.ArdenEvent;
import arden.runtime.ArdenTime;

public final class CyclicTrigger implements Trigger {
	private final ArdenDuration interval;
	private final ArdenDuration length;
	private final Trigger starting;
	private final List<ScheduledCycle> scheduledCycles = new ArrayList<>();
	private long currentDelay = 0;

	public CyclicTrigger(ArdenDuration interval, ArdenDuration length, Trigger starting) {
		this.interval = interval;
		this.length = length;
		this.starting = starting;
	}

	@Override
	public ArdenTime getNextRunTime() {
		// Check if the starting trigger has happened
		ArdenTime startTime = starting.getNextRunTime();
		if (startTime != null) {
			ArdenTime end = new ArdenTime(startTime.add(length));
			ScheduledCycle cycle = new ScheduledCycle(startTime, end);
			scheduledCycles.add(cycle);
		}

		ScheduledCycle oldestCycle = null;
		Iterator<ScheduledCycle> iterator = scheduledCycles.iterator();
		while (iterator.hasNext()) {
			ScheduledCycle cycle = iterator.next();

			if (cycle.next.compareTo(cycle.end) > 0) {
				// Cycle is already finished.
				iterator.remove();
				continue;
			}

			// Calculate cycle, which should be returned
			if (oldestCycle == null) {
				oldestCycle = cycle;
			} else if (cycle.next.compareTo(oldestCycle.next) < 0) {
				oldestCycle = cycle;
			}
		}

		if (oldestCycle == null) {
			return null;
		}

		currentDelay = oldestCycle.next.value - oldestCycle.start.value;
		ArdenTime nextRunTime = oldestCycle.next;

		oldestCycle.next = new ArdenTime(oldestCycle.next.add(interval));
		return nextRunTime;
	}

	@Override
	public boolean runOnEvent(ArdenEvent event) {
		return false;
	}

	@Override
	public void scheduleEvent(ArdenEvent event) {
		starting.scheduleEvent(event);
		if (starting.runOnEvent(event)) {
			ArdenEvent startEvent = starting.getTriggeringEvent();

			ArdenTime startTime = new ArdenTime(startEvent.eventTime);
			ArdenTime end = new ArdenTime(startTime.add(length));
			ScheduledCycle cycle = new ScheduledCycle(startTime, end);
			scheduledCycles.add(cycle);
		}
	}

	public ArdenEvent getTriggeringEvent() {
		return starting.getTriggeringEvent();
	}

	@Override
	public long getDelay() {
		return currentDelay + starting.getDelay();
	}

	private class ScheduledCycle {
		ArdenTime start;
		ArdenTime next;
		ArdenTime end;

		public ScheduledCycle(ArdenTime start, ArdenTime end) {
			this.start = start;
			this.next = this.start;
			this.end = end;
		}
	}
}

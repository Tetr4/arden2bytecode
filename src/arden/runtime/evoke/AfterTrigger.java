package arden.runtime.evoke;

import java.util.SortedSet;
import java.util.TreeSet;

import arden.runtime.ArdenDuration;
import arden.runtime.ArdenEvent;
import arden.runtime.ArdenTime;

public final class AfterTrigger implements Trigger {
	private final Trigger target;
	private final ArdenDuration duration;
	private final SortedSet<ArdenTime> additionalSchedules;

	public AfterTrigger(ArdenDuration duration, Trigger target) {
		this.duration = duration;
		this.target = target;
		this.additionalSchedules = new TreeSet<ArdenTime>(new ArdenTime.NaturalComparator());
	}

	@Override
	public ArdenTime getNextRunTime() {
		ArdenTime nextRunTime = target.getNextRunTime();
		if (nextRunTime != null) {
			nextRunTime = new ArdenTime(nextRunTime.add(duration));
		}

		// Select oldest schedule if it is older than nextRunTime
		if (!additionalSchedules.isEmpty()) {
			if (additionalSchedules.comparator().compare(additionalSchedules.first(), nextRunTime) < 0) {
				ArdenTime oldestSchedule = additionalSchedules.first();
				nextRunTime = oldestSchedule;
				additionalSchedules.remove(oldestSchedule);
			}
		}

		return nextRunTime;
	}

	@Override
	public boolean runOnEvent(ArdenEvent event) {
		return false;
	}

	@Override
	public void scheduleEvent(ArdenEvent event) {
		target.scheduleEvent(event);
		if (target.runOnEvent(event)) {
			// trigger in 'duration' after eventtime
			long triggerTime = new ArdenTime(event.eventTime).add(duration);
			additionalSchedules.add(new ArdenTime(triggerTime));
		}
	}

	@Override
	public ArdenEvent getTriggeringEvent() {
		return target.getTriggeringEvent();
	}

	@Override
	public long getDelay() {
		return target.getDelay() + (long) (duration.toSeconds() * 1000);
	}
}

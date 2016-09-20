package arden.runtime.evoke;

import java.util.SortedSet;
import java.util.TreeSet;

import arden.runtime.ArdenDuration;
import arden.runtime.ArdenEvent;
import arden.runtime.ArdenTime;
import arden.runtime.ExecutionContext;

public class AfterTrigger extends Trigger {

	Trigger target;
	ArdenDuration duration;
	SortedSet<ArdenTime> additionalSchedules;

	public AfterTrigger(ArdenDuration duration, Trigger target) {
		this.duration = duration;
		this.target = target;
		this.additionalSchedules = new TreeSet<ArdenTime>(new ArdenTime.NaturalComparator());
	}

	@Override
	public ArdenTime getNextRunTime(ExecutionContext context) {
		ArdenTime nextRunTime = target.getNextRunTime(context);
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
		if (target.runOnEvent(event)) {
			// trigger in 'duration' after eventtime
			long triggerTime = new ArdenTime(event.eventTime).add(duration);
			additionalSchedules.add(new ArdenTime(triggerTime));
		}
		return false;
	}

}

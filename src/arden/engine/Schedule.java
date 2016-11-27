package arden.engine;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;

import arden.runtime.ArdenTime;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;
import arden.runtime.evoke.Trigger;

@SuppressWarnings("serial")
public class Schedule extends TreeMap<ArdenTime, Queue<Call>> {
	public Schedule() {
		// sort by time
		super(new ArdenTime.NaturalComparator());
	}

	public static Schedule create(ExecutionContext context, List<MedicalLogicModule> mlms) {
		Schedule schedule = new Schedule();

		for (MedicalLogicModule mlm : mlms) {
			Trigger[] triggers;
			try {
				triggers = mlm.getTriggers(context);
			} catch (InvocationTargetException e) {
				// print error and skip this MLM
				e.printStackTrace();
				continue;
			}
			for (Trigger trigger : triggers) {
				ArdenTime nextRuntime = trigger.getNextRunTime();
				if (nextRuntime != null) {
					// scheduled
					MlmCall call = new MlmCall(context, mlm, null, trigger, (int) mlm.getPriority());
					schedule.add(nextRuntime, call);
				}
			}
		}

		return schedule;
	}

	public void add(ArdenTime nextRunTime, Call call) {
		// put MLMs which should run at the same time into groups
		Queue<Call> scheduleGroup = get(nextRunTime);
		if (scheduleGroup == null) {
			scheduleGroup = new PriorityQueue<Call>(3);
			put(nextRunTime, scheduleGroup);
		}
		scheduleGroup.add(call);
	}

	public void add(Schedule additionalSchedule) {
		for (Map.Entry<ArdenTime, Queue<Call>> entry : additionalSchedule.entrySet()) {
			ArdenTime time = entry.getKey();
			Queue<Call> scheduleGroup = get(time);
			if (scheduleGroup == null) {
				put(time, entry.getValue());
			} else {
				scheduleGroup.addAll(entry.getValue());
			}
		}
	}
}
package arden.engine;

import java.lang.reflect.InvocationTargetException;

import arden.runtime.ArdenDuration;
import arden.runtime.ArdenEvent;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;
import arden.runtime.evoke.Trigger;

public final class EventCall extends Call {
	private final ArdenEvent event;
	private final ExecutionContext context;
	private final Iterable<MedicalLogicModule> mlms;

	public EventCall(ExecutionContext context, Iterable<MedicalLogicModule> mlms, ArdenEvent event, int urgency) {
		// handle events before MlmCalls (highest priority/urgency is 99)
		super(99 + urgency);
		this.context = context;
		this.mlms = mlms;
		this.event = event;
	}

	@Override
	public void run() {
		// schedule event for all triggers and call directly triggered MLMs
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
				trigger.scheduleEvent(event);
				if (trigger.runOnEvent(event)) {
					context.call(mlm, null, ArdenDuration.ZERO, trigger, mlm.getPriority());
				}
			}
		}
	}
}
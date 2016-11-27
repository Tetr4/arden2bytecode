package arden.engine;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import arden.runtime.ArdenEvent;
import arden.runtime.ArdenRunnable;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;
import arden.runtime.evoke.Trigger;

/**
 * <p>
 * The evoke engine waits for periodic or delayed evoke triggers (in the evoke
 * slot) and delayed calls (in the action slot) and invokes MLMs when they are
 * scheduled. MLMs which are waiting for execution are called in the order of
 * their priority (MLMs triggered in the evoke slot) or their urgency (action
 * slot calls).
 * </p>
 * <p>
 * Threads can communicate with the engines scheduling loop via a message queue.
 * Messages are {@link EventCall}s or {@link MlmCall}s. EventCalls are handled
 * first as they only add new MlmCalls to the queue, then MlmCalls are handled
 * in order of their priority/urgency.
 * </p>
 * <p>
 * MlmCalls or EventCalls may trigger other MLMs after a delay, so the engine
 * uses each MLMs {@link Trigger#getNextRunTime()} method to check when it
 * should run next. Delayed calls are added to the the queue after their delay
 * has passed, via a {@link ScheduledExecutorService}.
 * </p>
 */
public class EvokeEngine implements Runnable {
	// thread-safe queue of calls which are waiting for execution
	private final PriorityBlockingQueue<Call> calls = new PriorityBlockingQueue<>(11);
	private final ScheduledExecutorService delayer = Executors.newScheduledThreadPool(1);
	private final ExecutionContext context;
	private final List<MedicalLogicModule> mlms;

	public EvokeEngine(ExecutionContext context, List<MedicalLogicModule> mlms) {
		this.mlms = mlms;
		this.context = context;
	}

	/** @see {@link ExecutionContext#findModules(ArdenEvent)} */
	public MedicalLogicModule[] findModules(ArdenEvent event) throws InvocationTargetException {
		List<MedicalLogicModule> foundModules = new ArrayList<>();
		for (MedicalLogicModule mlm : mlms) {
			for (Trigger trigger : mlm.getTriggers(context)) {
				if (trigger.runOnEvent(event)) {
					foundModules.add(mlm);
				}
			}
		}
		return foundModules.toArray(new MedicalLogicModule[foundModules.size()]);
	}

	/**
	 * Call an event after a delay.
	 * 
	 * @param event
	 *            the event, which will be called "as is" without changing the
	 *            eventtime
	 * @param delay
	 *            the delay in milliseconds
	 * @param urgency
	 *            a number from 1 (low urgency) to 99 (high urgency), which is
	 *            used to decide in which order to evaluate events
	 */
	public void call(ArdenEvent event, long delay, int urgency) {
		/*
		 * Checking the evoke statements may require running the data slot,
		 * which should not run concurrent to other (possibly data changing)
		 * MLMs. Therefore add an EventCall to calls, so it is run on the
		 * engines thread.
		 */
		final EventCall call = new EventCall(context, mlms, event, urgency);
		if (delay <= 0) {
			// run event as soon as possible
			calls.add(call);
		} else {
			// add the event call after the delay has passed
			delayer.schedule(new Runnable() {
				@Override
				public void run() {
					calls.add(call);
				}
			}, delay, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * Call an MLM after a delay.
	 * 
	 * @param mlm
	 *            the MLM to call
	 * @param arguments
	 *            parameters to the MLM
	 * @param delay
	 *            the delay in milliseconds
	 * @param evokingTrigger
	 *            the trigger, that will be given to the MLM "as is", without
	 *            changing the delay
	 * @param urgency
	 *            a number from 1 (low urgency) to 99 (high urgency), which is
	 *            used to decide in which order to evaluate MLMs.
	 */
	public void call(ArdenRunnable mlm, ArdenValue[] arguments, long delay, Trigger evokingTrigger, int urgency) {
		final MlmCall call = new MlmCall(context, mlm, arguments, evokingTrigger, urgency);
		if (delay <= 0) {
			// run MLM as soon as possible
			calls.add(call);
		} else {
			// add the call after the delay has passed
			delayer.schedule(new Runnable() {
				@Override
				public void run() {
					calls.add(call);
				}
			}, delay, TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public void run() {
		// initialize schedule for fixed time triggers
		scheduleTriggers();

		// the scheduling loop
		while (!Thread.currentThread().isInterrupted()) {
			// wait for calls
			Call call;
			try {
				call = calls.take();
			} catch (InterruptedException e) {
				// shutting down
				break;
			}

			// execute MlmCall or EventCall on this thread
			call.run();

			// check for MLMs which may now be triggered
			scheduleTriggers();
		}

		// cancel all delayed tasks
		delayer.shutdownNow();
	}

	private void scheduleTriggers() {
		// schedule MLMs by looking at their triggers getNextRunTime() method
		Schedule schedule = Schedule.create(context, mlms);

		ArdenTime currentTime = context.getCurrentTime();
		for (Entry<ArdenTime, Queue<Call>> entry : schedule.entrySet()) {
			ArdenTime nextRuntime = entry.getKey();
			final Queue<Call> triggeredMlms = entry.getValue();
			final long delay = nextRuntime.value - currentTime.value;

			if (delay <= 0) {
				// run MLMs as soon as possible
				calls.addAll(triggeredMlms);
			} else {
				// add the calls after the delay has passed
				delayer.schedule(new Runnable() {
					@Override
					public void run() {
						/*
						 * PriorityBlockingQueue.addAll() is not atomic, i.e.
						 * the first MLM has a high chance to run before all
						 * other MLMs are added. The MLMs are already sorted by
						 * their priority, so this is not a problem.
						 */
						calls.addAll(triggeredMlms);
					}
				}, delay, TimeUnit.MILLISECONDS);

			}
		}
	}
}

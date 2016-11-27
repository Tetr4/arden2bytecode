package arden.tests.specification.testcompiler.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import arden.engine.Call;
import arden.engine.EventCall;
import arden.engine.MlmCall;
import arden.engine.Schedule;
import arden.runtime.ArdenDuration;
import arden.runtime.ArdenEvent;
import arden.runtime.ArdenRunnable;
import arden.runtime.ArdenString;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContextHelpers;
import arden.runtime.MedicalLogicModule;
import arden.runtime.evoke.Trigger;
import arden.tests.specification.testcompiler.TestCompiler;
import arden.tests.specification.testcompiler.TestCompilerDelayedMessage;

/**
 * An engine for the
 * {@link TestCompiler#compileAndRunForEvent(String, String, int)} method. <br>
 * Skips delays by dynamically changing the {@link #getCurrentTime()} value, so
 * tests don't block.
 */
public class TestEngine extends TestContext {
	private final List<MedicalLogicModule> mlms;
	private final Schedule scheduledCalls;
	private final Queue<TestCompilerDelayedMessage> messages = new LinkedList<>();
	private final ArdenTime startTime;
	private ArdenTime currentTime;

	public TestEngine(List<MedicalLogicModule> mlms, ArdenTime startTime) {
		super(mlms);
		this.mlms = mlms;
		this.startTime = startTime;
		currentTime = startTime;
		scheduledCalls = Schedule.create(this, mlms);
	}

	@Override
	public void call(ArdenRunnable mlm, ArdenValue[] arguments, ArdenValue delayValue, Trigger trigger,
			double urgency) {
		ArdenDuration delayDuration = (ArdenDuration) delayValue;
		ArdenTime nextRuntime = new ArdenTime(currentTime.add(delayDuration));
		Trigger calleeTrigger = ExecutionContextHelpers.combine(trigger,
				ExecutionContextHelpers.delayToMillis(delayValue));
		scheduledCalls.add(nextRuntime, new MlmCall(this, mlm, arguments, calleeTrigger, (int) urgency));
	}

	@Override
	public void call(ArdenEvent event, ArdenValue delayValue, double urgency) {
		ArdenDuration delayDuration = (ArdenDuration) delayValue;
		ArdenTime nextRuntime = new ArdenTime(currentTime.add(delayDuration));
		ArdenEvent eventAfterDelay = ExecutionContextHelpers.combine(event,
				ExecutionContextHelpers.delayToMillis(delayValue));
		scheduledCalls.add(nextRuntime, new EventCall(this, mlms, eventAfterDelay, (int) urgency));
	}

	@Override
	public void write(ArdenValue message, ArdenValue destination, double urgency) {
		// save messages
		String stringMessage;
		if (message instanceof ArdenString) {
			stringMessage = ((ArdenString) message).value;
		} else {
			stringMessage = message.toString();
		}
		long delay = currentTime.value - startTime.value;
		messages.add(new TestCompilerDelayedMessage(delay, stringMessage));
	}

	@Override
	public List<String> getMessages() {
		throw new UnsupportedOperationException();
	}

	public TestCompilerDelayedMessage getNextDelayedMessage() throws InvocationTargetException {
		while (messages.isEmpty()) {
			// get the next MLM
			Entry<ArdenTime, Queue<Call>> nextEntry = scheduledCalls.firstEntry();
			if (nextEntry == null) {
				throw new IllegalStateException("There are no more messages");
			}
			ArdenTime nextRunTime = nextEntry.getKey();
			Queue<Call> queueCalls = nextEntry.getValue();
			Call nextCall = queueCalls.poll(); // highest priority
			if (queueCalls.isEmpty()) {
				// remove empty list
				scheduledCalls.pollFirstEntry();
			}

			// run the next MLM
			if (nextRunTime.value > currentTime.value) {
				// skip delay
				currentTime = new ArdenTime(nextRunTime.value);
			}
			nextCall.run();

			// schedule MLMs which may now be triggered
			Schedule additionalSchedule = Schedule.create(this, mlms);
			scheduledCalls.add(additionalSchedule);
		}

		return messages.remove();
	}

	@Override
	public ArdenEvent getEvent(String mapping) {
		return new ArdenEvent(mapping, getCurrentTime().value);
	}

	@Override
	public ArdenTime getCurrentTime() {
		return currentTime;
	}

}

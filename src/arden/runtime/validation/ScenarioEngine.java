package arden.runtime.validation;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Queue;

import org.junit.Assert;

import arden.engine.Call;
import arden.engine.MlmCall;
import arden.engine.Schedule;
import arden.runtime.ArdenBoolean;
import arden.runtime.ArdenEvent;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.MedicalLogicModule;
import arden.runtime.MedicalLogicModuleImplementation;
import arden.runtime.evoke.CallTrigger;
import arden.runtime.evoke.Trigger;

public class ScenarioEngine {
	private final ScenarioExecutionContext context;
	private final MedicalLogicModule mlmUnderTest;
	private ArdenValue conclude;
	private ArdenValue[] returnValues;
	private boolean isTriggered = false;
	private Schedule schedule;

	public ScenarioEngine(ScenarioExecutionContext context, MedicalLogicModule mlmUnderTest) {
		this.context = context;
		this.mlmUnderTest = mlmUnderTest;

		// initialize the triggers, which may call methods on the context,
		// therefore disable warnings
		context.showWarnings(false);
		try {
			mlmUnderTest.getTriggers(context);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		context.showWarnings(true);

		scheduleAndCall();
	}

	public void callMlm(ArdenValue[] args) {
		resetCapturedOutput();
		doCallMlm(args, new CallTrigger());
	}

	public void callEvent(String mapping, ArdenValue primaryTime, ArdenValue eventTime) {
		resetCapturedOutput();
		if (primaryTime != null && ! (primaryTime instanceof ArdenTime)) {
			throw new RuntimeException("Primarytime for event" + mapping + "is not a time value.");
		}
		if (eventTime != null && ! (eventTime instanceof ArdenTime)) {
			throw new RuntimeException("EVENTTIME for event" + mapping + "is not a time value.");
		}

		ArdenEvent event;
		if (primaryTime == null) {
			event = new ArdenEvent(mapping, context.getCurrentTime().value);
		} else if (eventTime == null) {
			event = new ArdenEvent(mapping, ((ArdenTime) primaryTime).value);
		} else {
			event = new ArdenEvent(mapping, ((ArdenTime) primaryTime).value, ((ArdenTime) eventTime).value);
		}
		try {
			Trigger[] triggers = mlmUnderTest.getTriggers(context);
			for (Trigger trigger : triggers) {
				trigger.scheduleEvent(event);
				if (trigger.runOnEvent(event)) {
					resetCapturedOutput();
					isTriggered = true;
					doCallMlm(null, trigger);
				}
			}
		} catch (InvocationTargetException e) {
			throw new AssertionError("Could not create MLM instance", e);
		}
	}

	public void setTime(ArdenValue currentTime) {
		resetCapturedOutput();
		if (currentTime instanceof ArdenTime) {
			context.setCurrentTime((ArdenTime) currentTime);
		} else {
			throw new RuntimeException("The value <" + currentTime + "> is not a valid time.");
		}
		scheduleAndCall();
	}

	private void scheduleAndCall() {
		Schedule additionalSchedule = Schedule.create(context, Arrays.asList(mlmUnderTest));
		if (schedule == null) {
			schedule = additionalSchedule;
		} else {
			schedule.add(additionalSchedule);
		}
		for (Entry<ArdenTime, Queue<Call>> entry : schedule.entrySet()) {
			ArdenTime nextRuntime = entry.getKey();
			Queue<Call> calls = entry.getValue();
			final long delay = nextRuntime.value - context.getCurrentTime().value;
			if (delay <= 0) {
				for (Call call : calls) {
					MlmCall mlmCall = (MlmCall) call;
					resetCapturedOutput();
					isTriggered = true;
					doCallMlm(null, mlmCall.trigger);
				}
				schedule.pollFirstEntry();
			}
		}
	}

	private void resetCapturedOutput() {
		context.resetCapturedOutput();
		conclude = null;
		returnValues = null;
		isTriggered = false;
	}

	private void doCallMlm(ArdenValue[] args, Trigger trigger) {
		MedicalLogicModuleImplementation instance;
		try {
			instance = mlmUnderTest.createInstance(context, args, trigger);
			if (instance.logic(context)) {
				conclude = ArdenBoolean.create(true, ArdenValue.NOPRIMARYTIME);
				returnValues = instance.action(context);
			} else {
				conclude = ArdenBoolean.create(false, ArdenValue.NOPRIMARYTIME);
				returnValues = null;
			}
		} catch (InvocationTargetException e) {
			throw new AssertionError("Could not create MLM instance", e);
		}
	}

	public void assertIsTriggered(boolean not) {
		if (not) {
			Assert.assertFalse("The MLM was triggered", isTriggered);
		} else {
			Assert.assertTrue("The MLM was not triggered", isTriggered);
		}
	}

	public ArdenValue getConclude() {
		return conclude;
	}

	public void assertNothingReturned(boolean not) {
		if (not) {
			if (returnValues == null) {
				Assert.fail("Nothing was returned");
			}
		} else {
			if (returnValues != null) {
				Assert.fail("Something was returned: " + ScenarioExpressionHelpers.commaSeparatedList(returnValues));
			}
		}
	}

	public ArdenValue[] getReturnedValues() {
		Assert.assertNotNull("Nothing was returned", returnValues);
		return returnValues;
	}

}

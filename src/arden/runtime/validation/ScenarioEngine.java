package arden.runtime.validation;

import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;

import arden.runtime.ArdenBoolean;
import arden.runtime.ArdenEvent;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.MedicalLogicModule;
import arden.runtime.MedicalLogicModuleImplementation;
import arden.runtime.evoke.Trigger;

public class ScenarioEngine {
	private final ScenarioExecutionContext context;
	private final MedicalLogicModule mlmUnderTest;
	private ArdenValue conclude;
	private ArdenValue[] returnValues;
	private boolean isTriggered = false;

	public ScenarioEngine(ScenarioExecutionContext context, MedicalLogicModule mlmUnderTest) {
		this.context = context;
		this.mlmUnderTest = mlmUnderTest;
	}

	public void callMlm(ArdenValue[] args) {
		resetCapturedOutput();
		doCallMlm(args);
	}

	public void callEvent(String mapping, ArdenTime primaryTime, ArdenTime eventTime) {
		resetCapturedOutput();

		ArdenEvent event;
		if (primaryTime == null) {
			event = new ArdenEvent(mapping);
		} else if (eventTime == null) {
			event = new ArdenEvent(mapping, primaryTime.value);
		} else {
			event = new ArdenEvent(mapping, primaryTime.value, eventTime.value);
		}

		try {
			Trigger trigger = mlmUnderTest.getTrigger(context, null);
			trigger.scheduleEvent(event);
			if (trigger.runOnEvent(event)) {
				context.setEvokingEvent(event, 0);
				isTriggered = true;
				doCallMlm(null);
			}
		} catch (InvocationTargetException e) {
			throw new AssertionError("Could not create MLM instance", e);
		}

	}

	public void setTime(ArdenTime currentTime) {
		resetCapturedOutput();
		context.setCurrentTime(currentTime);
		try {
			Trigger trigger = mlmUnderTest.getTrigger(context, null);
			ArdenTime nextRunTime = trigger.getNextRunTime(context);
			if (nextRunTime != null && nextRunTime.value - currentTime.value <= 0) {
				isTriggered = true;
				context.setEvokingEvent(trigger.getTriggeringEvent(), trigger.getDelay());
				doCallMlm(null);
			}
		} catch (InvocationTargetException e) {
			throw new AssertionError("Could not create MLM instance", e);
		}
	}

	private void resetCapturedOutput() {
		context.resetCapturedOutput();
		conclude = null;
		returnValues = null;
		isTriggered = false;
	}

	private void doCallMlm(ArdenValue[] args) {
		MedicalLogicModuleImplementation instance;
		try {
			instance = mlmUnderTest.createInstance(context, args);
			if (instance.logic(context)) {
				conclude = ArdenBoolean.create(true, ArdenValue.NOPRIMARYTIME);
				returnValues = instance.action(context);
			} else {
				conclude = ArdenBoolean.create(false, ArdenValue.NOPRIMARYTIME);
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

	public void assertConclude(boolean not, ArdenValue expected) {
		if (not) {
			if (conclude != null) {
				Assert.assertNotEquals("Conclude matched", expected, conclude);
			}
		} else {
			if (conclude == null) {
				Assert.fail();
			} else {
				Assert.assertEquals("Conclude did not match", expected, conclude);
			}
		}
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

	// TODO method to remove checked return values?

	public ArdenValue[] getReturnedValues() {
		Assert.assertNotNull("Nothing was returned", returnValues);
		return returnValues;
	}

}

package arden.compiler;

import java.lang.reflect.Method;

import org.junit.Assert;

import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.MedicalLogicModule;
import arden.runtime.validation.ScenarioEngine;
import arden.runtime.validation.ScenarioExecutionContext;
import arden.runtime.validation.ScenarioExpressionHelpers;

/** Contains references to the methods from the ExecutionContext class */
final class ScenarioMethods {
	public static final Method setQuery;
	public static final Method setInterface;
	public static final Method callMlm;
	public static final Method callEvent;
	public static final Method setTime;
	public static final Method getReturnedValues;
	public static final Method getWrittenMessages;
	public static final Method assertIsTriggered;
	public static final Method assertConclude;
	public static final Method assertMlmCalled;
	public static final Method assertEventCalled;
	public static final Method assertNothingCalled;
	public static final Method assertNothingReturned;
	public static final Method assertNothingWritten;
	public static final Method assertTrue;
	public static final Method assertEquals;
	public static final Method fail;
	public static final Method assertValidFilterResult;
	public static final Method assertMessageInList;
	public static final Method assertSameNumberOfValues;
	public static final Method isSameNumberOfValues;
	public static final Method equals;
	public static final Method isValidFilterResult;
	public static final Method moreMessagesAvailable;

	static {
		try {
			setQuery = ScenarioExecutionContext.class.getMethod("setQuery", String.class, ArdenValue[].class);
			setInterface = ScenarioExecutionContext.class.getMethod("setInterface", String.class, ArdenValue[].class);
			getWrittenMessages = ScenarioExecutionContext.class.getMethod("getWrittenMessages", String.class);
			getReturnedValues = ScenarioEngine.class.getMethod("getReturnedValues", MedicalLogicModule.class);
			callMlm = ScenarioEngine.class.getMethod("callMlm", MedicalLogicModule.class, ArdenValue[].class);
			callEvent = ScenarioEngine.class.getMethod("callEvent", String.class, ArdenTime.class, ArdenTime.class);
			setTime = ScenarioEngine.class.getMethod("setTime", ArdenTime.class);
			
			assertMlmCalled = ScenarioExecutionContext.class.getMethod("assertCalled", boolean.class, MedicalLogicModule.class, ArdenValue[].class, ArdenValue.class);
			assertEventCalled = ScenarioExecutionContext.class.getMethod("assertCalled", boolean.class, String.class, ArdenValue.class);
			assertNothingCalled = ScenarioExecutionContext.class.getMethod("assertNothingCalled", boolean.class);
			assertNothingWritten = ScenarioExecutionContext.class.getMethod("assertNothingWritten", String.class);
			assertNothingReturned = ScenarioEngine.class.getMethod("assertNothingReturned", boolean.class, MedicalLogicModule.class);
			assertIsTriggered = ScenarioEngine.class.getMethod("assertIsTriggered", boolean.class, MedicalLogicModule.class);
			assertConclude = ScenarioEngine.class.getMethod("assertConclude", boolean.class, MedicalLogicModule.class, ArdenValue.class);
			
			assertTrue = Assert.class.getMethod("assertTrue", String.class, boolean.class);
			assertEquals = Assert.class.getMethod("assertEquals", String.class, Object.class, Object.class);
			fail = Assert.class.getMethod("fail", String.class);
			equals = Object.class.getMethod("equals", Object.class);
			
			assertMessageInList = ScenarioExpressionHelpers.class.getMethod("assertMessageInList", ArdenValue.class, ArdenValue[].class);
			assertValidFilterResult = ScenarioExpressionHelpers.class.getMethod("assertValidFilterResult", ArdenValue.class, ArdenValue.class, int.class);
			assertSameNumberOfValues = ScenarioExpressionHelpers.class.getMethod("assertSameNumberOfValues", int.class, ArdenValue[].class);
			isSameNumberOfValues = ScenarioExpressionHelpers.class.getMethod("isSameNumberOfValues", int.class, ArdenValue[].class);
			isValidFilterResult = ScenarioExpressionHelpers.class.getMethod("isValidFilterResult", ArdenValue.class);
			moreMessagesAvailable = ScenarioExpressionHelpers.class.getMethod("moreMessagesAvailable", int.class, ArdenValue[].class);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}

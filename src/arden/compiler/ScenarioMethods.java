package arden.compiler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Assert;

import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.MedicalLogicModule;
import arden.runtime.validation.Call;
import arden.runtime.validation.ScenarioEngine;
import arden.runtime.validation.ScenarioExecutionContext;
import arden.runtime.validation.ScenarioExpressionHelpers;

/** Contains references to the methods from the ExecutionContext class */
final class ScenarioMethods {
	public static final Method setQuery;
	public static final Method setInterface;
	public static final Method getWrittenMessages;
	public static final Method getReturnedValues;
	public static final Method getConclude;
	public static final Method getMlmCalls;
	public static final Method getEventCalls;
	public static final Method callMlm;
	public static final Method callEvent;
	public static final Method setTime;
	
	public static final Field mlmCallDelay;
	public static final Field mlmCallArgs;
	
	public static final Method assertNothingCalled;
	public static final Method assertNothingReturned;
	public static final Method assertNothingWritten;
	public static final Method assertIsTriggered;
	
	public static final Method assertTrue;
	public static final Method assertFalse;
	public static final Method assertEquals;
	public static final Method assertNotEquals;
	public static final Method fail;
	public static final Method equals;
	
	public static final Method assertMessageInList;
	public static final Method assertValidFilterResult;
	public static final Method assertSameNumberOfReturnValues;
	public static final Method isSameNumberOfValues;
	public static final Method isValidFilterResult;
	public static final Method moreItemsAvailable;
	public static final Method isZeroDelay;


	static {
		try {
			setQuery = ScenarioExecutionContext.class.getMethod("setQuery", String.class, ArdenValue[].class);
			setInterface = ScenarioExecutionContext.class.getMethod("setInterface", String.class, ArdenValue[].class);
			getWrittenMessages = ScenarioExecutionContext.class.getMethod("getWrittenMessages", String.class);
			getMlmCalls = ScenarioExecutionContext.class.getMethod("getMlmCalls", MedicalLogicModule.class);
			getEventCalls = ScenarioExecutionContext.class.getMethod("getEventCalls", String.class);
			getReturnedValues = ScenarioEngine.class.getMethod("getReturnedValues");
			getConclude = ScenarioEngine.class.getMethod("getConclude");
			callMlm = ScenarioEngine.class.getMethod("callMlm", ArdenValue[].class);
			callEvent = ScenarioEngine.class.getMethod("callEvent", String.class, ArdenTime.class, ArdenTime.class);
			setTime = ScenarioEngine.class.getMethod("setTime", ArdenTime.class);
			
			mlmCallDelay = Call.class.getDeclaredField("delay");
			mlmCallArgs = Call.class.getDeclaredField("args");
			
			assertNothingCalled = ScenarioExecutionContext.class.getMethod("assertNothingCalled", boolean.class);
			assertNothingWritten = ScenarioExecutionContext.class.getMethod("assertNothingWritten", boolean.class, String.class);
			assertNothingReturned = ScenarioEngine.class.getMethod("assertNothingReturned", boolean.class);
			assertIsTriggered = ScenarioEngine.class.getMethod("assertIsTriggered", boolean.class);
			
			assertTrue = Assert.class.getMethod("assertTrue", String.class, boolean.class);
			assertFalse = Assert.class.getMethod("assertFalse", String.class, boolean.class);
			assertEquals = Assert.class.getMethod("assertEquals", String.class, Object.class, Object.class);
			assertNotEquals = Assert.class.getMethod("assertNotEquals", String.class, Object.class, Object.class);
			fail = Assert.class.getMethod("fail", String.class);
			equals = Object.class.getMethod("equals", Object.class);
			
			assertMessageInList = ScenarioExpressionHelpers.class.getMethod("assertMessageInList", boolean.class, ArdenValue.class, ArdenValue[].class, String.class);
			assertValidFilterResult = ScenarioExpressionHelpers.class.getMethod("assertValidFilterResult", ArdenValue.class, ArdenValue.class, int.class);
			assertSameNumberOfReturnValues = ScenarioExpressionHelpers.class.getMethod("assertSameNumberOfReturnValues", int.class, ArdenValue[].class);
			isSameNumberOfValues = ScenarioExpressionHelpers.class.getMethod("isSameNumberOfValues", int.class, Object[].class);
			isValidFilterResult = ScenarioExpressionHelpers.class.getMethod("isValidFilterResult", ArdenValue.class);
			moreItemsAvailable = ScenarioExpressionHelpers.class.getMethod("moreItemsAvailable", int.class, Object[].class);
			isZeroDelay = ScenarioExpressionHelpers.class.getMethod("isZeroDelay", ArdenValue.class);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}
}

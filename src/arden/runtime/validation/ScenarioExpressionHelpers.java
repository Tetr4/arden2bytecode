package arden.runtime.validation;

import java.util.Arrays;

import org.junit.Assert;

import arden.runtime.ArdenBoolean;
import arden.runtime.ArdenDuration;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;

public class ScenarioExpressionHelpers {

	public static void assertSameNumberOfReturnValues(int expectedCount, ArdenValue[] actual) {
		if (actual.length > expectedCount) {
			String msg = "Too many values were returned: " + commaSeparatedList(actual);
			Assert.fail(msg);
		} else if (actual.length < expectedCount) {
			String msg = "Not enough values were returned: " + commaSeparatedList(actual);
			Assert.fail(msg);
		}
	}

	public static boolean isSameNumberOfValues(int expectedCount, Object[] actual) {
		return expectedCount == actual.length;
	}

	public static boolean moreItemsAvailable(int index, Object[] items) {
		return index < items.length;
	}
	
	public static void assertValidFilterResult(ArdenValue filterResult, ArdenValue returnValue, int index) {
		String msg = "Filter did not match for " + (index + 1) + ". return value: " + returnValue.toString();
		Assert.assertTrue(msg, filterResult instanceof ArdenBoolean && ArdenBoolean.TRUE.equals(filterResult));
	}

	public static boolean isValidFilterResult(ArdenValue filterResult) {
		return ArdenBoolean.TRUE.equals(filterResult);
	}

	public static void assertMessageInList(boolean not, ArdenValue message, ArdenValue[] list, String destination) {
		boolean isInList = Arrays.asList(list).contains(message);
		if (not) {
			String msg = "Message [" + message + "] is in the list of messages at {" + destination + "}: "
					+ commaSeparatedList(list);
			Assert.assertTrue(msg, !isInList);			
		} else {
			String msg = "Message [" + message + "] is not in the list of expected messages at {" + destination + "}: "
					+ commaSeparatedList(list);
			Assert.assertTrue(msg, isInList);
		}

	}
	
	public static boolean isZeroDelay(ArdenValue delay) {
		return ArdenDuration.seconds(0, ArdenTime.NOPRIMARYTIME).equals(delay);
	}

	public static String commaSeparatedList(Object[] values) {
		String separator = "";
		StringBuilder result = new StringBuilder();
		result.append("[");
		for (Object value : values) {
			result.append(separator);
			result.append(value.toString());
			separator = ", ";
		}
		result.append("]");
		return result.toString();
	}

}

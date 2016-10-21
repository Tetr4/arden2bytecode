package arden.runtime.validation;

import org.junit.Assert;

import arden.runtime.ArdenBoolean;
import arden.runtime.ArdenValue;

public class ScenarioExpressionHelpers {

	public static void assertSameNumberOfValues(int expectedCount, ArdenValue[] actual) {
		if (actual.length > expectedCount ) {
			String msg = "Too many values were returned: " + commaSeparatedList(actual);
			Assert.fail(msg);
		} else if (actual.length < expectedCount) {
			String msg = "Not enough values were returned: " + commaSeparatedList(actual);
			Assert.fail(msg);
		}
	}
	
	public static boolean isSameNumberOfValues(int expectedCount, ArdenValue[] actual) {
		return expectedCount == actual.length;
	}
	
	public static boolean moreMessagesAvailable(int index, ArdenValue[] messages) {
		return index < messages.length;
	}
	
	public static void assertValidFilterResult(ArdenValue filterResult, ArdenValue returnValue, int index) {
		String msg = "Filter did not match for " + (index + 1) + ". return value: " + returnValue.toString();
		Assert.assertTrue(msg, filterResult instanceof ArdenBoolean && ArdenBoolean.TRUE.equals(filterResult));
	}
	
	public static boolean isValidFilterResult(ArdenValue filterResult) {
		return ArdenBoolean.TRUE.equals(filterResult);
	}

	public static void assertMessageInList(ArdenValue message, ArdenValue[] list) {
		boolean success = false;
		for (ArdenValue value : list) {
			if (message.equals(value)) {
				success = true;
			}
		}
		String msg = "Message [" + message + "] is not in the list of expected messages: " + commaSeparatedList(list);
		Assert.assertTrue(msg, success);
	}

	private static String commaSeparatedList(ArdenValue[] values) {
		String separator = "";
		StringBuilder result = new StringBuilder();
		result.append("[");
		for (ArdenValue value : values) {
			result.append(separator);
			result.append(value.toString());
			separator = ", ";
		}
		result.append("]");
		return result.toString();
	}

}

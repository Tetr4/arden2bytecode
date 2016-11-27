package arden.runtime.validation;

import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import arden.runtime.ArdenDuration;
import arden.runtime.ArdenEvent;
import arden.runtime.ArdenNull;
import arden.runtime.ArdenObject;
import arden.runtime.ArdenRunnable;
import arden.runtime.ArdenString;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.BaseExecutionContext;
import arden.runtime.DatabaseQuery;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;
import arden.runtime.MemoryQuery;
import arden.runtime.ObjectType;
import arden.runtime.evoke.Trigger;

public class ScenarioExecutionContext extends BaseExecutionContext {
	// configuration
	Map<String, ArdenValue[]> queries = new HashMap<>();
	Map<String, ArdenValue[]> interfaces = new HashMap<>();
	ArdenTime currentTime;
	{
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(1900, 0, 0, 0, 0, 0);
		currentTime = new ArdenTime(calendar.getTimeInMillis());
	}

	// captured output
	List<WrittenMessage> writtenMessages = new ArrayList<>();
	List<MlmCall> mlmCalls = new ArrayList<>();
	List<EventCall> eventCalls = new ArrayList<>();

	public ScenarioExecutionContext() {
		super(((URLClassLoader) (Thread.currentThread().getContextClassLoader())).getURLs());
	}

	public void setCurrentTime(ArdenTime time) {
		currentTime = time;
	}

	public void setQuery(String mapping, ArdenValue[] values) {
		queries.put(mapping, values);
	}

	public void setInterface(String mapping, ArdenValue[] values) {
		interfaces.put(mapping, values);
	}

	public void resetCapturedOutput() {
		writtenMessages.clear();
		mlmCalls.clear();
		eventCalls.clear();
	}

	public MlmCall[] getMlmCalls(MedicalLogicModule mlm) {
		List<MlmCall> calls = new ArrayList<>();
		for (MlmCall call : mlmCalls) {
			if (call.mlm.getName().equalsIgnoreCase(mlm.getName())) {
				calls.add(call);
			}
		}
		return calls.toArray(new MlmCall[calls.size()]);
	}

	public EventCall[] getEventCalls(String mapping) {
		List<EventCall> calls = new ArrayList<>();
		for (EventCall call : eventCalls) {
			if (call.mapping.equalsIgnoreCase(mapping)) {
				calls.add(call);
			}
		}
		return calls.toArray(new EventCall[calls.size()]);
	}

	public void assertNothingCalled(boolean not) {
		if (not) {
			Assert.assertFalse("No MLM(s) where called", mlmCalls.isEmpty());
			Assert.assertFalse("No Event(s) where called", eventCalls.isEmpty());
		} else {
			// TODO output mlms / events
			Assert.assertTrue("MLM(s) have been called", mlmCalls.isEmpty());
			Assert.assertTrue("Event(s) have been called", eventCalls.isEmpty());
		}

	}

	public void assertNothingWritten(boolean not, String destination) {
		ArdenValue[] messages = getWrittenMessages(destination);
		if (not) {
			Assert.assertTrue("Nothing was written", messages.length != 0);
		} else {
			// TODO output messages
			Assert.assertTrue("Message(s) have been written", messages.length == 0);
		}
	}

	// TODO method to remove checked messages?

	public ArdenValue[] getWrittenMessages(String destination) {
		List<ArdenValue> values = new ArrayList<>();
		for (WrittenMessage message : writtenMessages) {
			if (destination == null && message.destination == null) {
				values.add(message.value);
			} else if (destination != null && destination.equals(message.destination)) {
				values.add(message.value);
			}
		}
		return values.toArray(new ArdenValue[values.size()]);
	}

	@Override
	public DatabaseQuery createQuery(String mapping) {
		ArdenValue[] values = queries.get(mapping);
		if (values == null) {
			throw new AssertionError("The result for the {" + mapping + "} query is not defined.");
		}
		return new MemoryQuery(values);
	}

	@Override
	public ArdenValue getMessage(String mapping) {
		return new ArdenString(mapping);
	}

	@Override
	public ArdenObject getMessageAs(String mapping, ObjectType type) {
		// store mapping in typeinfo
		ObjectType mappingType = new ObjectType(mapping, new String[] {});
		return new ArdenObject(mappingType);
	}

	@Override
	public ArdenValue getDestination(String mapping) {
		return new ArdenString(mapping);
	}

	@Override
	public ArdenObject getDestinationAs(String mapping, ObjectType type) {
		// store mapping in typeinfo
		ObjectType mappingType = new ObjectType(mapping, new String[] {});
		return new ArdenObject(mappingType);
	}

	@Override
	public ArdenEvent getEvent(String mapping) {
		return new ArdenEvent(mapping, currentTime.value);
	}

	@Override
	public void write(ArdenValue message, ArdenValue destination, double urgency) {
		if (message instanceof ArdenObject) {
			// recover mapping from type
			message = new ArdenString(((ArdenObject) message).type.name);
		}
		if (destination instanceof ArdenObject) {
			// recover mapping from type
			destination = new ArdenString(((ArdenObject) destination).type.name);
		}
		String destinationString = ArdenString.getStringFromValue(destination);
		writtenMessages.add(new WrittenMessage(message, destinationString));
	}

	@Override
	public ArdenRunnable findInterface(final String mapping) {
		final ArdenValue[] values = interfaces.get(mapping);
		return new ArdenRunnable() {
			@Override
			public ArdenValue[] run(ExecutionContext context, ArdenValue[] arguments, Trigger trigger)
					throws InvocationTargetException {
				if (values == null) {
					System.err.println("Warning: the result for the {" + mapping + "} interface is not defined.");
					return new ArdenValue[] { ArdenNull.INSTANCE };
				}
				return values;
			}
		};
	}

	@Override
	public void call(ArdenRunnable mlm, ArdenValue[] arguments, ArdenValue delay, Trigger trigger, double urgency) {
		mlmCalls.add(new MlmCall((MedicalLogicModule) mlm, arguments, (ArdenDuration) delay));
	}

	@Override
	public void call(ArdenEvent event, ArdenValue delay, double urgency) {
		eventCalls.add(new EventCall(event.name, delay));
	}

	@Override
	public ArdenTime getCurrentTime() {
		return currentTime;
	}

}

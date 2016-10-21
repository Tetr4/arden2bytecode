package arden.runtime.validation;

import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import arden.runtime.ArdenDuration;
import arden.runtime.ArdenEvent;
import arden.runtime.ArdenRunnable;
import arden.runtime.ArdenString;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.BaseExecutionContext;
import arden.runtime.DatabaseQuery;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;
import arden.runtime.MemoryQuery;

public class ScenarioExecutionContext extends BaseExecutionContext {
	// configuration
	Map<String, ArdenValue[]> queries = new HashMap<>();
	Map<String, ArdenValue[]> interfaces = new HashMap<>();

	// captured output
	List<WrittenMessage> writtenMessages = new ArrayList<>();
	List<MlmCall> mlmCalls = new ArrayList<>();
	List<EventCall> eventCalls = new ArrayList<>();

	// event
	ArdenTime currentTime = null;
	ArdenTime triggerTime = null;
	ArdenEvent evokingEvent;

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

	public void setEvokingEvent(ArdenEvent event, long delay) {
		event.isEvokingEvent = true;
		triggerTime = new ArdenTime(event.eventTime + delay);
		this.evokingEvent = event;
	}

	public void resetCapturedOutput() {
		writtenMessages.clear();
		mlmCalls.clear();
		eventCalls.clear();
		evokingEvent = null;
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
	public ArdenEvent getEvent(String mapping) {
		if (evokingEvent != null && evokingEvent.name.equalsIgnoreCase(mapping)) {
			return evokingEvent;
		}
		return super.getEvent(mapping);
	}

	@Override
	public void write(ArdenValue message, String destination) {
		writtenMessages.add(new WrittenMessage(message, destination));
	}

	@Override
	public ArdenRunnable findInterface(final String mapping) {
		final ArdenValue[] values = interfaces.get(mapping);
		if (values == null) {
			throw new AssertionError("The result for the {" + mapping + "} interface is not defined.");
		}
		
		return new ArdenRunnable() {
			@Override
			public ArdenValue[] run(ExecutionContext context, ArdenValue[] arguments) throws InvocationTargetException {
				return values;
			}
		};
	}

	@Override
	public void callWithDelay(ArdenRunnable mlm, ArdenValue[] arguments, ArdenValue delay) {
		mlmCalls.add(new MlmCall((MedicalLogicModule) mlm, arguments, (ArdenDuration) delay));
	}

	@Override
	public void callEvent(ArdenEvent event) {
		// TODO events can be called with delay
		eventCalls.add(new EventCall(event.name, null));
	}

	@Override
	public ArdenTime getEventTime() {
		return new ArdenTime(evokingEvent.eventTime);
	}

	@Override
	public ArdenTime getTriggerTime() {
		return triggerTime;
	}

	@Override
	public ArdenTime getCurrentTime() {
		return currentTime;
	}

}

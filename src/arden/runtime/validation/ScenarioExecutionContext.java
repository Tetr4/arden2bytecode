package arden.runtime.validation;

import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

	public ScenarioExecutionContext() {
		super(((URLClassLoader) (Thread.currentThread().getContextClassLoader())).getURLs());
	}

	ArdenTime currentTimeOverride = null;
	Map<String, ArdenValue[]> queries = new HashMap<>();
	Map<String, ArdenValue[]> interfaces = new HashMap<>();

	Set<WrittenMessage> messages = new HashSet<>();
	Set<MlmCall> mlmCalls = new HashSet<>();
	Map<String, ArdenDuration> eventCalls = new HashMap<>();
	ArdenEvent evokingEvent;

	public void setQuery(String mapping, ArdenValue[] values) {
		queries.put(mapping, values);
	}

	public void setInterface(String mapping, ArdenValue[] values) {
		interfaces.put(mapping, values);
	}

	public void setEvokingEvent(ArdenEvent event) {
		event.isEvokingEvent = true;
		this.evokingEvent = event;
	}

	public void assertCalled(boolean not, MedicalLogicModule mlm, ArdenValue[] args, ArdenValue delay) {
		System.out.println("assertCalled MLM");
	}

	public void assertCalled(boolean not, String eventMapping, ArdenValue delay) {
		System.out.println("assertCalled event");
	}

	public void assertNothingCalled(boolean not) {
		System.out.println("assertNothingCalled");
	}

	public void assertNothingWritten(String destination) {
		System.out.println("assertNothingWritten");
	}

	public ArdenValue[] getWrittenMessages(String destination) {
		System.out.println("getWrittenMessages");
		return new ArdenValue[] { new ArdenString("hello world")};
	}

	@Override
	public DatabaseQuery createQuery(String mapping) {
		ArdenValue[] values = queries.get(mapping);
		if (values != null) {
			return new MemoryQuery(values);
		}
		return super.createQuery(mapping);
	}

	@Override
	public ArdenValue getMessage(String mapping) {
		return new ArdenString(mapping);
	}

	@Override
	public ArdenEvent getEvent(String mapping) {
		// TODO
		return super.getEvent(mapping);
	}

	@Override
	public void write(ArdenValue message, String destination) {
		messages.add(new WrittenMessage(ArdenString.getStringFromValue(message), destination));
	}

	@Override
	public ArdenRunnable findInterface(final String mapping) {
		return new ArdenRunnable() {
			@Override
			public ArdenValue[] run(ExecutionContext context, ArdenValue[] arguments) throws InvocationTargetException {
				return interfaces.get(mapping);
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
		eventCalls.put(event.name, (ArdenDuration) ArdenDuration.seconds(0, 0));
	}

	@Override
	public ArdenTime getEventTime() {
		return new ArdenTime(evokingEvent.eventTime);
	}

	@Override
	public ArdenTime getTriggerTime() {
		// TODO
		return super.getTriggerTime();
	}

	@Override
	public ArdenTime getCurrentTime() {
		if (currentTimeOverride != null) {
			return currentTimeOverride;
		}
		return super.getCurrentTime();
	}

	private class MlmCall {
		public MlmCall(MedicalLogicModule mlm, ArdenValue[] args, ArdenDuration delay) {
			this.mlm = mlm;
			this.args = args;
			this.delay = delay;
		}

		MedicalLogicModule mlm;
		ArdenValue[] args;
		ArdenDuration delay;
	}

	private class WrittenMessage {
		public WrittenMessage(String message, String destination) {
			this.message = message;
			this.destination = destination;
		}

		String message;
		String destination;
	}

}

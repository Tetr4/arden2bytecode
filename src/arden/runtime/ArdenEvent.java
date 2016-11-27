package arden.runtime;

/** Represents an <code>EVENT</code> object. */
public final class ArdenEvent extends ArdenValue {
	public final String name;
	public final boolean isEvokingEvent;
	/**
	 * The <code>TIME OF an_event</code> (the primary time) is the clinically
	 * relevant time, e.g. the time when a sample was taken. <br>
	 * The <code>EVENTTIME</code> is the time when the relevant data, e.g. the
	 * samples measured values, are stored in the database. <br>
	 * This allows using the <code>EVENTTIME</code>, to get the matching
	 * database entry, while <code>TIME OF an_event</code> can be used for
	 * calculation.
	 */
	public final long eventTime;

	public ArdenEvent(String name) {
		super();
		this.name = name;
		this.eventTime = NOPRIMARYTIME;
		this.isEvokingEvent = false;
	}

	public ArdenEvent(String name, long primaryTime) {
		super(primaryTime);
		this.name = name;
		this.eventTime = primaryTime;
		this.isEvokingEvent = false;
	}

	public ArdenEvent(String name, long primaryTime, long eventTime) {
		super(primaryTime);
		this.name = name;
		this.eventTime = eventTime;
		this.isEvokingEvent = false;
	}

	private ArdenEvent(String name, long primaryTime, long eventTime, boolean isEvokingEvent) {
		super(primaryTime);
		this.name = name;
		this.eventTime = eventTime;
		this.isEvokingEvent = true;
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		/*
		 * This is not explicitly defined in the standard. But as events can be
		 * called in the action slot, it should be possible to change the time
		 * of an event.
		 */
		return new ArdenEvent(name, newPrimaryTime);
	}

	public ArdenEvent setEvokingEvent(boolean isEvokingEvent) {
		return new ArdenEvent(name, primaryTime, eventTime, isEvokingEvent);
	}

	@Override
	public boolean isTrue() {
		return isEvokingEvent;
	}

	@Override
	public boolean isFalse() {
		return !isEvokingEvent;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ArdenEvent && name.equalsIgnoreCase(((ArdenEvent) obj).name);
	}

}

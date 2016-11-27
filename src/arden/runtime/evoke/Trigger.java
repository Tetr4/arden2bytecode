package arden.runtime.evoke;

import arden.runtime.ArdenEvent;
import arden.runtime.ArdenTime;
import arden.runtime.ExecutionContext;

public interface Trigger {
	/**
	 * <p>
	 * Calculates when the trigger should run next. Triggers are stateful, i.e.
	 * they will be marked as done/scheduled, when this method is called. In
	 * other word they will return <code>null</code> or a different time the
	 * next time this method is called.
	 * </p>
	 * 
	 * <p>
	 * For example the cyclic trigger:
	 * 
	 * <pre>
	 * {@code
	 * EVERY 10 MINUTES FOR 1 HOUR STARTING TIME OF an_event;
	 * }
	 * </pre>
	 * 
	 * will return <code>null</code> until <code>an_event</code> has happened.
	 * After the event it will return the time of the event. Then it will return
	 * the time of the event + 10 minutes, etc. On the 8th call it will return
	 * <code>null</code> again, as the cycle has finished.
	 * </p>
	 * 
	 * @return The next run time or null if there is no next run time.
	 */
	public ArdenTime getNextRunTime();

	/**
	 * Whether to immediately (no delay) run for an external event such as
	 * 'penicillin_storage'. The event will be compared with others via it's
	 * {@link #equals(Object)} method, which can be overridden. <br>
	 * No state is changed in contrast to {@link #scheduleEvent(ArdenEvent)}.
	 * 
	 * <p>
	 * E.g. the trigger <code>10 SECONDS AFTER TIME OF an_event</code> will
	 * return false, when this method called with a matching
	 * <code>an_event</code>.
	 * </p>
	 * 
	 * @param event
	 *            The event to check.
	 * @return Whether this trigger is immediately triggered by the event.
	 */
	public boolean runOnEvent(ArdenEvent event);

	/**
	 * This method may mark some triggers as scheduled similar to
	 * {@link #getNextRunTime(ExecutionContext)}.
	 * 
	 * <p>
	 * E.g. after scheduling a matching <code>an_event</code> for the trigger
	 * <code>10 SECONDS AFTER TIME OF an_event</code>,
	 * {@link #getNextRunTime(ExecutionContext)} will return a time, that is 10
	 * seconds after the events {@link ArdenEvent#eventTime}.
	 * </p>
	 * 
	 * @param event
	 *            The event for which the trigger may create a schedule.
	 */
	public void scheduleEvent(ArdenEvent event);

	/**
	 * @return The event that caused the trigger to be triggered, or null if no
	 *         such event exists.
	 */
	public ArdenEvent getTriggeringEvent();

	/**
	 * The delay (in millis) since the triggering event occurred. 0 if no such
	 * event occurred.
	 * 
	 * @return The delay since the <code>EVENTTIME</code>.
	 */
	public long getDelay();
}

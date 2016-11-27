// arden2bytecode
// Copyright (c) 2010, Daniel Grunwald
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice, this list
//   of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright notice, this list
//   of conditions and the following disclaimer in the documentation and/or other materials
//   provided with the distribution.
//
// - Neither the name of the owner nor the names of its contributors may be used to
//   endorse or promote products derived from this software without specific prior written
//   permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS &AS IS& AND ANY EXPRESS
// OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
// IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package arden.runtime;

import java.util.Date;

import arden.runtime.evoke.Trigger;

/**
 * Describes the environment in which a Medical Logic Module is executed.
 * 
 * @author Daniel Grunwald
 * 
 */
public abstract class ExecutionContext {
	/**
	 * Creates a database query using a mapping clause, as part of a
	 * <code>READ</code> or <code>READ AS</code> statement. The
	 * {@link DatabaseQuery} object can be used to limit the number of results
	 * produced.
	 * 
	 * @param mapping
	 *            The contents of the statement's mapping clause (text between
	 *            curly braces). The meaning is implementation-defined. The
	 *            Arden language specification uses mapping clauses like
	 *            "medication_cancellation where class = gentamicin".
	 * 
	 * @return This method may not return Java null. Instead, it can return
	 *         {@link DatabaseQuery#NULL}, a query that will always produce an
	 *         empty result set.
	 */
	public DatabaseQuery createQuery(String mapping) {
		return DatabaseQuery.NULL;
	}

	/**
	 * Gets a value that represents a message, as part of a <code>MESSAGE</code>
	 * statement.
	 * 
	 * @param mapping
	 *            The contents of the statement's mapping clause.
	 * 
	 * @return A (possibly custom) subclass of {@link ArdenValue} that
	 *         represents the message. This value may be given as a parameter in
	 *         the {@link #write(ArdenValue, ArdenValue)} method.
	 */
	public ArdenValue getMessage(String mapping) {
		return new ArdenString(mapping);
	}

	/**
	 * Gets an object that represents a message, as part of a
	 * <code>MESSAGE AS</code> statement.
	 * 
	 * @param mapping
	 *            The contents of the statement's mapping clause.
	 * 
	 * @param type
	 *            The type that the returned object should have.
	 * 
	 * @return An {@link ArdenObject} of the given {@link ObjectType}.
	 */
	public ArdenObject getMessageAs(String mapping, ObjectType type) {
		return new ArdenObject(type);
	}

	/**
	 * Gets a value that represents a destination, as part of the
	 * <code>DESTINATION</code> statement.
	 * 
	 * @param mapping
	 *            The contents of the statement's mapping clause.
	 * 
	 * @return A (possibly custom) subclass of {@link ArdenValue} that
	 *         represents the destination. This value is used as a parameter in
	 *         the {@link #write(ArdenValue, ArdenValue)} method.
	 */
	public ArdenValue getDestination(String mapping) {
		return new ArdenString(mapping);
	}

	/**
	 * Gets an object that represents a destination, as part of a
	 * <code>DESTINATION AS</code> statement.
	 * 
	 * @param mapping
	 *            The contents of the statement's mapping clause.
	 * 
	 * @param type
	 *            The type that the returned object should have.
	 * 
	 * @return An {@link ArdenObject} of the given {@link ObjectType}.
	 */
	public ArdenObject getDestinationAs(String mapping, ObjectType type) {
		return new ArdenObject(type);
	}

	/**
	 * Gets an event as part of the <code>EVENT</code> statement.
	 * 
	 * @param mapping
	 *            The contents of the statement's mapping clause.
	 * 
	 * @return An {@link ArdenEvent}. If it is the event, that triggered the
	 *         MLM, it will automatically flagged as such.
	 */
	public ArdenEvent getEvent(String mapping) {
		return new ArdenEvent(mapping);
	}

	/**
	 * Called by the <code>WRITE</code> statement.
	 * 
	 * @param message
	 *            The message to be written. This may be an instance returned
	 *            from {@link #getMessage(String)} or
	 *            {@link #getMessageAs(String, ObjectType)}, but other values
	 *            are possible.
	 * 
	 * @param destination
	 *            The destination for the message. This will be an instance
	 *            returned from {@link #getDestination(String)} or
	 *            {@link #getDestinationAs(String, ObjectType)}. May be null, if
	 *            the default destination should be used.
	 * 
	 * @param urgency
	 *            The urgency from the MLMs urgency slot.
	 */
	public void write(ArdenValue message, ArdenValue destination, double urgency) {
	}

	/**
	 * Retrieves another MLM as part of the <code>MLM</code> statement.
	 * 
	 * @param name
	 *            The name of the requested MLM.
	 * 
	 * @param institution
	 *            The institution of the requested MLM.
	 * 
	 * @return The requested MLM.
	 */
	public MedicalLogicModule findModule(String name, String institution) {
		throw new RuntimeException("findModule not implemented");
	}

	/**
	 * Retrieves all MLMs that are normally evoked by an event. This is used as
	 * part of the event <code>CALL</code> statement in the logic slot.
	 * 
	 * @param event
	 *            The event.
	 * 
	 * @return The requested MLMs.
	 */
	public MedicalLogicModule[] findModules(ArdenEvent event) {
		throw new RuntimeException("findModules not implemented");
	}

	/**
	 * Retrieves an interface implementation, as part of the
	 * <code>INTERFACE</code> statement.
	 * 
	 * @param mapping
	 *            The contents of the statement's mapping clause.
	 * 
	 * @return The interface implementation as an {@link ArdenRunnable}.
	 */
	public ArdenRunnable findInterface(String mapping) {
		throw new RuntimeException("findInterface not implemented");
	}

	/**
	 * Calls another MLM using a delay. This method will be called for all MLM
	 * calls in the action slot.
	 * 
	 * @param mlm
	 *            The MLM that should be called. This will be an instance
	 *            returned from {@link #findModule(String, String)} or
	 *            {@link #findInterface(String)}.
	 * 
	 * @param arguments
	 *            The arguments being passed. Can be null if no arguments were
	 *            specified.
	 * 
	 * @param delay
	 *            The delay for calling the MLM (as ArdenDuration).
	 * 
	 * @param trigger
	 *            The calling MLMs trigger. Used to calculate the called MLMs
	 *            <code>EVENTTIME</code> and <code>TRIGGERTIME</code>.
	 * 
	 * @param urgency
	 *            The urgency from the MLMs urgency slot.
	 */
	public void call(ArdenRunnable mlm, ArdenValue[] arguments, ArdenValue delay, Trigger trigger, double urgency) {
		throw new RuntimeException("MLM call not implemented");
	}

	/**
	 * Calls an event using a delay. This method will be called for all event
	 * calls in the action slot.
	 * 
	 * @param event
	 *            The event that should be called.
	 * 
	 * @param delay
	 *            The delay for calling the event (as ArdenDuration).
	 */
	public void call(ArdenEvent event, ArdenValue delay, double urgency) {
		throw new RuntimeException("Event call not implemented");
	}

	/**
	 * @return The <code>CURRENTTIME</code>.
	 */
	public ArdenTime getCurrentTime() {
		return new ArdenTime(new Date());
	}
}

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

import java.lang.reflect.InvocationTargetException;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

import arden.runtime.evoke.Trigger;

/**
 * Represents a compiled medical logic module.
 * 
 * @author Daniel Grunwald
 */
public interface MedicalLogicModule extends ArdenRunnable {
	/** Creates a new instance of the implementation class. */
	MedicalLogicModuleImplementation createInstance(ExecutionContext context, ArdenValue[] arguments, Trigger evokingTrigger)
			throws InvocationTargetException;

	/** Gets the mlmname */
	String getName();

	/** Gets the maintenance metadata */
	MaintenanceMetadata getMaintenance();

	/** Gets the library metadata */
	LibraryMetadata getLibrary();

	/** Gets the priority of this module. */
	double getPriority();

	/** Gets the urgency value of this module. */
	double getUrgency();

	/**
	 * Gets the triggers telling when to run this MLM. Requires running the data
	 * slot to get event definitions.
	 * 
	 * @param context
	 *            The execution context, which creates event definitions.
	 * 
	 * @throws InvocationTargetException
	 */
	Trigger[] getTriggers(ExecutionContext context) throws InvocationTargetException;

	/**
	 * Gets the value of a variable declared in a Medical Logic Module.
	 * 
	 * @param name
	 *            Name of the value in the MLM.
	 * @return The variable value or null if the MLM has not been run yet or the
	 *         value does not exist. ArdenNull if the variable is not yet
	 *         initialized.
	 */
	ArdenValue getValue(String name);

	/**
	 * Tests this MLM by running the scenarios in the validation category.
	 * 
	 * @param junit
	 *            The JUnit test facade, to which {@link RunListener}s can be
	 *            attached.
	 * @return a JUnit result
	 */
	Result test(JUnitCore junit);
}

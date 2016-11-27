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

package arden.compiler;

import java.lang.reflect.Method;

import arden.runtime.ArdenEvent;
import arden.runtime.ArdenRunnable;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.ObjectType;
import arden.runtime.evoke.Trigger;

/** Contains references to the methods from the ExecutionContext class */
final class ExecutionContextMethods {
	public static final Method createQuery;
	public static final Method getMessage, getMessageAs, getDestination, getDestinationAs, getEvent;
	public static final Method findModule, findModules, findInterface;
	public static final Method write, call, callEvent;
	public static final Method getCurrentTime;

	static {
		try {
			createQuery = ExecutionContext.class.getMethod("createQuery", String.class);

			getMessage = ExecutionContext.class.getMethod("getMessage", String.class);
			getMessageAs = ExecutionContext.class.getMethod("getMessageAs", String.class, ObjectType.class);
			getDestination = ExecutionContext.class.getMethod("getDestination", String.class);
			getDestinationAs = ExecutionContext.class.getMethod("getDestinationAs", String.class, ObjectType.class);
			getEvent = ExecutionContext.class.getMethod("getEvent", String.class);

			findModule = ExecutionContext.class.getMethod("findModule", String.class, String.class);
			findModules = ExecutionContext.class.getMethod("findModules", ArdenEvent.class);
			findInterface = ExecutionContext.class.getMethod("findInterface", String.class);

			write = ExecutionContext.class.getMethod("write", ArdenValue.class, ArdenValue.class, double.class);
			call = ExecutionContext.class.getMethod("call", ArdenRunnable.class, ArdenValue[].class, ArdenValue.class,
					Trigger.class, double.class);
			callEvent = ExecutionContext.class.getMethod("call", ArdenEvent.class, ArdenValue.class, double.class);

			getCurrentTime = ExecutionContext.class.getMethod("getCurrentTime");
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}

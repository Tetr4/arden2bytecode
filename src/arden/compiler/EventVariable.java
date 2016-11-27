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

import java.lang.reflect.Modifier;

import arden.codegenerator.FieldReference;
import arden.compiler.node.PExpr;
import arden.compiler.node.TIdentifier;
import arden.compiler.node.Token;
import arden.runtime.ArdenDuration;
import arden.runtime.ArdenEvent;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;

final class EventVariable extends DataVariable {

	private EventVariable(TIdentifier name, FieldReference field) {
		super(name, field);
	}

	public static EventVariable getVariable(CodeGenerator codeGen, LeftHandSideResult lhs) {
		if (!(lhs instanceof LeftHandSideIdentifier))
			throw new RuntimeCompilerException(lhs.getPosition(), "EVENT variables must be simple identifiers");
		TIdentifier ident = ((LeftHandSideIdentifier) lhs).identifier;
		Variable variable = codeGen.getVariable(ident.getText());
		if (variable instanceof EventVariable) {
			return (EventVariable) variable;
		} else {
			FieldReference mlmField = codeGen.createField(ident.getText(), ArdenEvent.class, Modifier.PRIVATE);
			EventVariable ev = new EventVariable(ident, mlmField);
			codeGen.addVariable(ev);
			return ev;
		}
	}

	@Override
	public void call(CompilerContext context, Token errorPosition, PExpr arguments) {
		context.writer.sequencePoint(errorPosition.getLine());
		context.writer.loadThis();
		context.writer.loadInstanceField(field);
		context.writer.loadVariable(context.executionContextVariable);
		if (arguments != null) {
			new ExpressionCompiler(context).buildArrayForCommaSeparatedExpression(arguments);
		} else {
			context.writer.loadNull();
		}
		context.writer.invokeStatic(
				Compiler.getRuntimeHelper("callEvent", ArdenEvent.class, ExecutionContext.class, ArdenValue[].class));
	}

	@Override
	public void callWithDelay(CompilerContext context, Token errorPosition, PExpr arguments, PExpr delay) {
		context.writer.sequencePoint(errorPosition.getLine());
		context.writer.loadVariable(context.executionContextVariable);

		// set the event's EVENTTIME to the calling MLMs TRIGGERTIME
		context.writer.loadThis();
		context.writer.loadInstanceField(field);
		context.writer.loadThis();
		context.writer.loadInstanceField(context.codeGenerator.getNowField());
		context.writer.loadThis();
		context.writer.loadInstanceField(context.codeGenerator.getTriggerTimeField());
		context.writer.invokeStatic(
				Compiler.getRuntimeHelper("prepareForCall", ArdenEvent.class, ArdenValue.class, ArdenValue.class));

		if (delay != null) {
			delay.apply(new ExpressionCompiler(context));
		} else {
			try {
				context.writer.loadStaticField(ArdenDuration.class.getField("ZERO"));
			} catch (NoSuchFieldException | SecurityException e) {
				throw new RuntimeCompilerException(errorPosition, "Could not create zero delay");
			}
		}

		ActionCompiler.loadUrgency(context);

		context.writer.invokeInstance(ExecutionContextMethods.callEvent);
	}
}
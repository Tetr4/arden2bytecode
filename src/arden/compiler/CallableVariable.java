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

import arden.codegenerator.FieldReference;
import arden.compiler.node.PExpr;
import arden.compiler.node.TIdentifier;
import arden.compiler.node.Token;
import arden.runtime.ArdenDuration;
import arden.runtime.ArdenRunnable;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.evoke.Trigger;

/**
 * MLM or INTERFACE Variables.
 * 
 * An instance field of type MedicalLogicModule or ArdenRunnable is stored in
 * the MLM implementation class. It is set in the data block where the 'x := MLM
 * y' statement or 'x := INTERFACE y' statement occurs and used for CALL
 * statements.
 */
abstract class CallableVariable extends Variable {
	// instance field of type ArdenRunnable
	final FieldReference runnableField;

	protected CallableVariable(TIdentifier varName, FieldReference runnableField) {
		super(varName);
		this.runnableField = runnableField;
	}

	@Override
	public void call(CompilerContext context, Token errorPosition, PExpr arguments) {
		context.writer.sequencePoint(errorPosition.getLine());
		context.writer.loadThis();
		context.writer.loadInstanceField(runnableField);
		context.writer.loadVariable(context.executionContextVariable);
		if (arguments != null) {
			new ExpressionCompiler(context).buildArrayForCommaSeparatedExpression(arguments);
		} else {
			context.writer.loadNull();
		}
		context.writer.loadThis();
		context.writer.loadInstanceField(context.codeGenerator.getTriggerField());
		context.writer.invokeStatic(Compiler.getRuntimeHelper("call", ArdenRunnable.class, ExecutionContext.class,
				ArdenValue[].class, Trigger.class));
	}

	@Override
	public void callWithDelay(CompilerContext context, Token errorPosition, PExpr arguments, PExpr delay) {
		context.writer.sequencePoint(errorPosition.getLine());
		context.writer.loadVariable(context.executionContextVariable);
		context.writer.loadThis();
		context.writer.loadInstanceField(runnableField);
		if (arguments != null) {
			new ExpressionCompiler(context).buildArrayForCommaSeparatedExpression(arguments);
		} else {
			context.writer.loadNull();
		}
		if (delay != null) {
			delay.apply(new ExpressionCompiler(context));
		} else {
			try {
				context.writer.loadStaticField(ArdenDuration.class.getField("ZERO"));
			} catch (NoSuchFieldException | SecurityException e) {
				throw new RuntimeCompilerException(errorPosition, "Could not create zero delay");
			}
		}
		context.writer.loadThis();
		context.writer.loadInstanceField(context.codeGenerator.getTriggerField());
		ActionCompiler.loadUrgency(context);
		context.writer.invokeInstance(ExecutionContextMethods.call);
	}
}

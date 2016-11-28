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

import arden.codegenerator.Label;
import arden.compiler.node.*;
import arden.runtime.ArdenValue;
import arden.runtime.MedicalLogicModuleImplementation;

/**
 * Compiler for actions.
 * 
 * Every actionBlock.apply(this) call will generate code that executes the
 * action. The evaluation stack is empty between all apply calls.
 * 
 * @author Daniel Grunwald
 */
final class ActionCompiler extends VisitorBase {
	private final CompilerContext context;

	public ActionCompiler(CompilerContext context) {
		this.context = context;
	}

	// action_slot =
	// action action_block semicolons;
	@Override
	public void caseAActionSlot(AActionSlot node) {
		node.getActionBlock().apply(this);
	}

	// action_block =
	// {astmt} action_statement
	// | {ablk} action_block semicolon action_statement;
	@Override
	public void caseAAstmtActionBlock(AAstmtActionBlock node) {
		node.getActionStatement().apply(this);
	}

	@Override
	public void caseAAblkActionBlock(AAblkActionBlock node) {
		node.getActionBlock().apply(this);
		node.getActionStatement().apply(this);
	}

	// action_statement =
	// {empty}
	// | {if} if action_if_then_else2
	// | {for} for identifier in expr do action_block semicolon enddo
	// | {while} while expr do action_block semicolon enddo
	// | {call} call_phrase
	// | {cdel} call_phrase delay expr
	// | {write} write expr
	// | {wrtat} write expr at identifier
	// | {return} return expr
	// | {assign1} identifier_becomes expr
	// | {assign2} time_becomes expr
	// | {assign3} identifier_becomes new_object_phrase
	@Override
	public void caseAEmptyActionStatement(AEmptyActionStatement node) {
	}

	@Override
	public void caseAIfActionStatement(AIfActionStatement node) {
		// action_statement = {if} if action_if_then_else2
		context.writer.sequencePoint(node.getIf().getLine());
		node.getActionIfThenElse2().apply(this);
	}

	@Override
	public void caseAForActionStatement(AForActionStatement node) {
		// action_statement =
		// {for} for identifier in expr do action_block semicolon enddo
		compileForStatement(context, node.getFor(), node.getIdentifier(), node.getExpr(), node.getActionBlock(), this);
	}

	public static void compileForStatement(CompilerContext context, TFor tFor, TIdentifier identifier,
			PExpr collectionExpr, Switchable block, Switch blockCompiler) {
		context.writer.sequencePoint(tFor.getLine());
		collectionExpr.apply(new ExpressionCompiler(context));
		try {
			context.writer.invokeInstance(ArdenValue.class.getMethod("getElements"));
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		int arrayVar = context.allocateVariable();
		context.writer.storeVariable(arrayVar);
		int loopIndexVar = context.allocateVariable();
		context.writer.loadIntegerConstant(0);
		context.writer.storeIntVariable(loopIndexVar);

		String varName = identifier.getText();
		if (context.codeGenerator.getVariable(varName) != null)
			throw new RuntimeCompilerException(identifier, "A variable with the name '" + varName
					+ "' is already defined at this location.");

		ForLoopVariable newLoopVariable = new ForLoopVariable(identifier, context.allocateVariable());
		context.codeGenerator.addVariable(newLoopVariable);
		context.writer.defineLocalVariable(newLoopVariable.variableIndex, varName, ArdenValue.class);

		Label loopCondition = new Label();
		Label loopBody = new Label();
		context.writer.jump(loopCondition);
		context.writer.mark(loopBody);

		// loopVar = arrayVar[loopIndexVar];
		context.writer.loadVariable(arrayVar);
		context.writer.loadIntVariable(loopIndexVar);
		context.writer.loadObjectFromArray();
		context.writer.storeVariable(newLoopVariable.variableIndex);

		block.apply(blockCompiler);

		// loopIndexVar++;
		context.writer.incVariable(loopIndexVar, 1);

		// if (loopIndexVar < arrayVar.length) goto loopBody;
		context.writer.markForwardJumpsOnly(loopCondition);
		context.writer.loadIntVariable(loopIndexVar);
		context.writer.loadVariable(arrayVar);
		context.writer.arrayLength();
		context.writer.jumpIfLessThan(loopBody);

		if (newLoopVariable != null)
			context.codeGenerator.deleteVariable(newLoopVariable);
	}

	@Override
	public void caseAWhileActionStatement(AWhileActionStatement node) {
		// action_statement = {while} while expr do action_block semicolon enddo
		compileWhileStatement(context, node.getWhile(), node.getExpr(), node.getActionBlock(), this);
	}

	public static void compileWhileStatement(CompilerContext context, TWhile tWhile, PExpr expr, Switchable block,
			Switch blockCompiler) {

		Label start = new Label();
		Label end = new Label();
		context.writer.mark(start);
		context.writer.sequencePoint(tWhile.getLine());
		expr.apply(new ExpressionCompiler(context));
		try {
			context.writer.invokeInstance(ArdenValue.class.getMethod("isTrue"));
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		context.writer.jumpIfZero(end);
		block.apply(blockCompiler);
		context.writer.jump(start);
		context.writer.markForwardJumpsOnly(end);
	}

	PExpr currentCallDelay;

	@Override
	public void caseACallActionStatement(ACallActionStatement node) {
		// action_statement = {call} call_phrase
		currentCallDelay = null;
		node.getCallPhrase().apply(this);
	}

	@Override
	public void caseACdelActionStatement(ACdelActionStatement node) {
		// action_statement = {cdel} call_phrase delay expr
		currentCallDelay = node.getExpr();
		node.getCallPhrase().apply(this);
	}

	@Override
	public void caseAWriteActionStatement(AWriteActionStatement node) {
		// action_statement = {write} write expr
		context.writer.sequencePoint(node.getWrite().getLine());
		context.writer.loadVariable(context.executionContextVariable);
		node.getExpr().apply(new ExpressionCompiler(context));
		context.writer.loadNull();
		loadUrgency(context);
		context.writer.invokeInstance(ExecutionContextMethods.write);
	}

	@Override
	public void caseAWrtatActionStatement(AWrtatActionStatement node) {
		// action_statement = {wrtat} write expr at identifier
		context.writer.sequencePoint(node.getWrite().getLine());
		context.writer.loadVariable(context.executionContextVariable);
		node.getExpr().apply(new ExpressionCompiler(context));

		Variable destination = context.codeGenerator.getVariableOrShowError(node.getIdentifier());
		if (!(destination instanceof DestinationVariable))
			throw new RuntimeCompilerException(node.getIdentifier(), "'" + node.getIdentifier().getText()
					+ "' is not a valid destination variable.");
		context.writer.loadThis();
		context.writer.loadInstanceField(((DestinationVariable) destination).field);
		loadUrgency(context);
		context.writer.invokeInstance(ExecutionContextMethods.write);
	}

	static void loadUrgency(CompilerContext context) {
		context.writer.loadThis();
		Method getUrgency;
		try {
			getUrgency = MedicalLogicModuleImplementation.class.getDeclaredMethod("getUrgency");
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		context.writer.invokeInstance(getUrgency);
	}

	@Override
	public void caseAReturnActionStatement(AReturnActionStatement node) {
		// action_statement = {return} return expr

		context.writer.sequencePoint(node.getReturn().getLine());

		// Special case: RETURN a, b; does not return a list, but multiple
		// values.
		// "RETURN (a, b);" and "RETURN a, b;" are not equivalent!
		new ExpressionCompiler(context).buildArrayForCommaSeparatedExpression(node.getExpr());
		context.writer.returnObjectFromFunction();
	}

	@Override
	public void caseAAssign1ActionStatement(AAssign1ActionStatement node) {
		// action_statement = {assign1} identifier_becomes expr
		LeftHandSideResult lhs = LeftHandSideAnalyzer.analyze(node.getIdentifierBecomes());
		lhs.assign(context, node.getExpr());
	}

	@Override
	public void caseAAssign2ActionStatement(AAssign2ActionStatement node) {
		// action_statement = {assign2} time_becomes expr
		LeftHandSideResult lhs = LeftHandSideAnalyzer.analyze(node.getTimeBecomes());
		lhs.assign(context, node.getExpr());
	}

	@Override
	public void caseAAssign3ActionStatement(AAssign3ActionStatement node) {
		// action_statement = {assign3} identifier_becomes new_object_phrase
		LeftHandSideResult lhs = LeftHandSideAnalyzer.analyze(node.getIdentifierBecomes());
		lhs.assign(context, node.getNewObjectPhrase());
	}

	// action_if_then_else2 =
	// expr then action_block semicolon action_elseif;
	@Override
	public void caseAActionIfThenElse2(AActionIfThenElse2 node) {
		compileIfStatement(context, node.getExpr(), node.getActionBlock(), node.getActionElseif(), this);
	}

	public static void compileIfStatement(CompilerContext context, PExpr expr, Switchable trueBlock,
			Switchable falseBlock, Switch blockCompiler) {
		expr.apply(new ExpressionCompiler(context));
		try {
			context.writer.invokeInstance(ArdenValue.class.getMethod("isTrue"));
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		Label falseLabel = new Label();
		Label endLabel = new Label();
		context.writer.jumpIfZero(falseLabel);
		trueBlock.apply(blockCompiler);
		context.writer.jump(endLabel);
		context.writer.markForwardJumpsOnly(falseLabel);
		falseBlock.apply(blockCompiler);
		context.writer.markForwardJumpsOnly(endLabel);
	}

	// action_elseif =
	// {end} endif
	// | {else} else action_block semicolon endif
	// | {elseif} elseif action_if_then_else2;
	@Override
	public void caseAEndActionElseif(AEndActionElseif node) {
	}

	@Override
	public void caseAElseActionElseif(AElseActionElseif node) {
		node.getActionBlock().apply(this);
	}

	@Override
	public void caseAElseifActionElseif(AElseifActionElseif node) {
		context.writer.sequencePoint(node.getElseif().getLine());
		node.getActionIfThenElse2().apply(this);
	}

	// call_phrase =
	// {id} call identifier
	// | {idex} call identifier with expr;
	@Override
	public void caseAIdCallPhrase(AIdCallPhrase node) {
		performCall(node.getCall(), node.getIdentifier(), null);
	}

	@Override
	public void caseAIdexCallPhrase(AIdexCallPhrase node) {
		performCall(node.getCall(), node.getIdentifier(), node.getExpr());
	}

	private void performCall(TCall call, TIdentifier ident, PExpr arguments) {
		Variable var = context.codeGenerator.getVariableOrShowError(ident);
		var.callWithDelay(context, call, arguments, currentCallDelay);
	}
}

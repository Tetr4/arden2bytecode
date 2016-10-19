package arden.compiler;

import arden.compiler.node.AAssGivenStatement;
import arden.compiler.node.ACallThenStatement;
import arden.compiler.node.ACallWhenStatement;
import arden.compiler.node.ACallargsThenStatement;
import arden.compiler.node.ACallargsWhenStatement;
import arden.compiler.node.ACalldelayThenStatement;
import arden.compiler.node.ACalldelayargsThenStatement;
import arden.compiler.node.ACallevtThenStatement;
import arden.compiler.node.ACallevtWhenStatement;
import arden.compiler.node.ACallevtdelayThenStatement;
import arden.compiler.node.ACallevttimeWhenStatement;
import arden.compiler.node.ACallevttimetimeWhenStatement;
import arden.compiler.node.ACallnothingThenStatement;
import arden.compiler.node.AConcludeThenStatement;
import arden.compiler.node.AEmptyScenarioBlock;
import arden.compiler.node.AGblkGivenBlockAnd;
import arden.compiler.node.AGblkScenarioBlock;
import arden.compiler.node.AGstmtGivenBlockAnd;
import arden.compiler.node.AGstmtScenarioBlock;
import arden.compiler.node.AIassGivenStatement;
import arden.compiler.node.AMlmiMlmDefinition;
import arden.compiler.node.AReturnThenStatement;
import arden.compiler.node.AReturnnothingThenStatement;
import arden.compiler.node.ASblkGivenBlockAnd;
import arden.compiler.node.ASblkThenBlockAnd;
import arden.compiler.node.ASblkWhenBlockAnd;
import arden.compiler.node.AScenarioSlot;
import arden.compiler.node.ATblkScenarioBlock;
import arden.compiler.node.ATblkThenBlockAnd;
import arden.compiler.node.ATimeWhenStatement;
import arden.compiler.node.ATimealiasWhenStatement;
import arden.compiler.node.ATriggerThenStatement;
import arden.compiler.node.ATstmtScenarioBlock;
import arden.compiler.node.ATstmtThenBlockAnd;
import arden.compiler.node.AWblkScenarioBlock;
import arden.compiler.node.AWblkWhenBlockAnd;
import arden.compiler.node.AWriteThenStatement;
import arden.compiler.node.AWriteatThenStatement;
import arden.compiler.node.AWritemsgThenStatement;
import arden.compiler.node.AWritemsgatThenStatement;
import arden.compiler.node.AWritenothingThenStatement;
import arden.compiler.node.AWritenothingatThenStatement;
import arden.compiler.node.AWstmtScenarioBlock;
import arden.compiler.node.AWstmtWhenBlockAnd;
import arden.runtime.validation.ScenarioEngine;
import arden.runtime.validation.ScenarioExecutionContext;

public final class ScenarioCompiler extends VisitorBase {

	private final CompilerContext context;
	private int executionContextVar;
	private int engineVar;

	public ScenarioCompiler(CompilerContext context, int contextVar, int engineVar) {
		this.context = context;
		this.executionContextVar = contextVar;
		this.engineVar = engineVar;
	}

	// scenario slot

	@Override
	public void caseAScenarioSlot(AScenarioSlot node) {
		node.getScenarioBlock().apply(this);
	}

	@Override
	public void caseAEmptyScenarioBlock(AEmptyScenarioBlock node) {
		// do nothing
	}

	// given blocks

	@Override
	public void caseAGblkScenarioBlock(AGblkScenarioBlock node) {
		node.getGivenStatement().apply(this);
		node.getGivenBlockAnd().apply(this);
	}

	@Override
	public void caseAGstmtScenarioBlock(AGstmtScenarioBlock node) {
		node.getGivenStatement().apply(this);
	}

	@Override
	public void caseASblkGivenBlockAnd(ASblkGivenBlockAnd node) {
		node.getScenarioBlock().apply(this);
	}

	@Override
	public void caseAGblkGivenBlockAnd(AGblkGivenBlockAnd node) {
		node.getGivenStatement().apply(this);
		node.getGivenBlockAnd().apply(this);
	}

	@Override
	public void caseAGstmtGivenBlockAnd(AGstmtGivenBlockAnd node) {
		node.getGivenStatement().apply(this);
	}

	// when blocks

	@Override
	public void caseAWblkScenarioBlock(AWblkScenarioBlock node) {
		node.getWhenStatement().apply(this);
		node.getWhenBlockAnd().apply(this);
	}

	@Override
	public void caseAWstmtScenarioBlock(AWstmtScenarioBlock node) {
		node.getWhenStatement().apply(this);
	}

	@Override
	public void caseASblkWhenBlockAnd(ASblkWhenBlockAnd node) {
		node.getScenarioBlock().apply(this);
	}

	@Override
	public void caseAWblkWhenBlockAnd(AWblkWhenBlockAnd node) {
		node.getWhenStatement().apply(this);
		node.getWhenBlockAnd().apply(this);
	}

	@Override
	public void caseAWstmtWhenBlockAnd(AWstmtWhenBlockAnd node) {
		node.getWhenStatement().apply(this);
	}

	// then blocks

	@Override
	public void caseATblkScenarioBlock(ATblkScenarioBlock node) {
		node.getThenStatement().apply(this);
		node.getThenBlockAnd().apply(this);
	}

	@Override
	public void caseATstmtScenarioBlock(ATstmtScenarioBlock node) {
		node.getThenStatement().apply(this);
	}

	@Override
	public void caseASblkThenBlockAnd(ASblkThenBlockAnd node) {
		node.getScenarioBlock().apply(this);
	}

	@Override
	public void caseATblkThenBlockAnd(ATblkThenBlockAnd node) {
		node.getThenStatement().apply(this);
		node.getThenBlockAnd().apply(this);
	}

	@Override
	public void caseATstmtThenBlockAnd(ATstmtThenBlockAnd node) {
		node.getThenStatement().apply(this);
	}

	// statements
	
	@Override
	public void caseAAssGivenStatement(AAssGivenStatement node) {
		// normal assignment
		// TODO load execution context local variable
		//context.writer.loadVariable(context.executionContextVariable);
		
		// load mapping string
		context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getMappingFactor()));
		
		// load expression array 
		new ExpressionCompiler(context).buildArrayForCommaSeparatedExpression(node.getExpr());
		
		// context.setQuery(stack.pop(), stack.pop());
		
	}
	
	@Override
	public void caseAIassGivenStatement(AIassGivenStatement node) {
		// interface assignment
	}

	@Override
	public void caseACallWhenStatement(ACallWhenStatement node) {
		// call
	}
	
	@Override
	public void caseACallargsWhenStatement(ACallargsWhenStatement node) {
		// call with args
	}
	
	@Override
	public void caseACallevtWhenStatement(ACallevtWhenStatement node) {
		// event call
	}
	
	@Override
	public void caseACallevttimeWhenStatement(ACallevttimeWhenStatement node) {
		// event call with primary time
	}
	
	@Override
	public void caseACallevttimetimeWhenStatement(ACallevttimetimeWhenStatement node) {
		// event call with primary time and eventtime
	}
	
	@Override
	public void caseATimeWhenStatement(ATimeWhenStatement node) {
		// set time
	}
	
	@Override
	public void caseATimealiasWhenStatement(ATimealiasWhenStatement node) {
		// set time
	}
	
	@Override
	public void caseATriggerThenStatement(ATriggerThenStatement node) {
		// then_statement = {trigger} mlm_definition should_be triggered
		// TODO implement me
//		Method assertTrue;
//		try {
//			assertTrue = Assert.class.getMethod("assertTrue", Boolean.TYPE);
//		} catch (NoSuchMethodException e) {
//			throw new RuntimeException(e);
//		} catch (SecurityException e) {
//			throw new RuntimeException(e);
//		}
//		// context.writer.loadIntegerConstant(1);
//		context.writer.invokeStatic(assertTrue);
	}
	
	@Override
	public void caseAConcludeThenStatement(AConcludeThenStatement node) {
		// then_statement = {conclude} conclude should_be expr
	}
	
	@Override
	public void caseAReturnThenStatement(AReturnThenStatement node) {
		// then_statement = {return} expr_indeterminate should_be returned
	}
	
	@Override
	public void caseAReturnnothingThenStatement(AReturnnothingThenStatement node) {
		// then_statement = {returnnothing} nothing should_be returned
	}
	
	@Override
	public void caseAWriteThenStatement(AWriteThenStatement node) {
		// then_statement = {write} expr_indeterminate should_be written
	}
	
	@Override
	public void caseAWriteatThenStatement(AWriteatThenStatement node) {
		// then_statement = {writeat} expr_indeterminate should_be written at destination? mapping_factor
	}
	
	@Override
	public void caseAWritemsgThenStatement(AWritemsgThenStatement node) {
		// then_statement = {writemsg} message? mapping_factor should_be written
	}
	
	@Override
	public void caseAWritemsgatThenStatement(AWritemsgatThenStatement node) {
		// then_statement = {writemsgat} message? [msgmapping]:mapping_factor should_be written at destination? [destmapping]:mapping_factor
	}
	
	@Override
	public void caseAWritenothingThenStatement(AWritenothingThenStatement node) {
		// then_statement = {writenothing} nothing should_be written
	}
	
	@Override
	public void caseAWritenothingatThenStatement(AWritenothingatThenStatement node) {
		// then_statement = {writenothingat} nothing should_be written at destination? mapping_factor
	}
	
	@Override
	public void caseACallThenStatement(ACallThenStatement node) {
		// then_statement = {call} mlm_definition should_be called
	}
	
	@Override
	public void caseACallargsThenStatement(ACallargsThenStatement node) {
		// then_statement = {callargs} mlm_definition should_be called with expr
	}
	
	@Override
	public void caseACalldelayThenStatement(ACalldelayThenStatement node) {
		// then_statement = {calldelay} mlm_definition should_be called delay [delayexp]:expr
	}
	
	@Override
	public void caseACalldelayargsThenStatement(ACalldelayargsThenStatement node) {
		// then_statement = {calldelayargs} mlm_definition should_be called with expr delay [delayexp]:expr
	}
	
	@Override
	public void caseACallevtThenStatement(ACallevtThenStatement node) {
		// then_statement = {callevt} event? mapping_factor should_be called
	}
	
	@Override
	public void caseACallevtdelayThenStatement(ACallevtdelayThenStatement node) {
		// then_statement = {callevtdelay} event? mapping_factor should_be called delay [delayexp]:expr
	}
	
	@Override
	public void caseACallnothingThenStatement(ACallnothingThenStatement node) {
		// then_statement = {callnothing} nothing should_be called
	}

}

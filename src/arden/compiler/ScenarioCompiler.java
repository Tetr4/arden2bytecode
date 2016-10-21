package arden.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import arden.codegenerator.Label;
import arden.compiler.node.AAnyExprIndeterminate;
import arden.compiler.node.AAssGivenPhrase;
import arden.compiler.node.ACallThenPhrase;
import arden.compiler.node.ACallWhenPhrase;
import arden.compiler.node.ACallargsThenPhrase;
import arden.compiler.node.ACallargsWhenPhrase;
import arden.compiler.node.ACalldelayThenPhrase;
import arden.compiler.node.ACalldelayargsThenPhrase;
import arden.compiler.node.ACallevtThenPhrase;
import arden.compiler.node.ACallevtWhenPhrase;
import arden.compiler.node.ACallevtdelayThenPhrase;
import arden.compiler.node.ACallevttimeWhenPhrase;
import arden.compiler.node.ACallevttimetimeWhenPhrase;
import arden.compiler.node.ACallnothingThenPhrase;
import arden.compiler.node.ACommaExprMixedTail;
import arden.compiler.node.ACommavalueExprMixedTail;
import arden.compiler.node.AConcludeThenPhrase;
import arden.compiler.node.AConcreteExprIndeterminate;
import arden.compiler.node.ADateConstTime;
import arden.compiler.node.ADtimeConstTime;
import arden.compiler.node.AEmptyScenarioBlock;
import arden.compiler.node.AExprMixed;
import arden.compiler.node.AFilterExprIndeterminate;
import arden.compiler.node.AGblkGivenBlockAnd;
import arden.compiler.node.AGblkScenarioBlock;
import arden.compiler.node.AGstmtGivenBlockAnd;
import arden.compiler.node.AGstmtScenarioBlock;
import arden.compiler.node.AIassGivenPhrase;
import arden.compiler.node.AMlmMlmDefinition;
import arden.compiler.node.AMlmiMlmDefinition;
import arden.compiler.node.AMlmsMlmDefinition;
import arden.compiler.node.AReturnThenPhrase;
import arden.compiler.node.AReturnmixedThenPhrase;
import arden.compiler.node.AReturnnothingThenPhrase;
import arden.compiler.node.ASblkGivenBlockAnd;
import arden.compiler.node.ASblkThenBlockAnd;
import arden.compiler.node.ASblkWhenBlockAnd;
import arden.compiler.node.AScenarioSlot;
import arden.compiler.node.ATblkScenarioBlock;
import arden.compiler.node.ATblkThenBlockAnd;
import arden.compiler.node.ATimeWhenPhrase;
import arden.compiler.node.ATimealiasWhenPhrase;
import arden.compiler.node.ATriggerThenPhrase;
import arden.compiler.node.ATstmtScenarioBlock;
import arden.compiler.node.ATstmtThenBlockAnd;
import arden.compiler.node.AValueExprMixedTail;
import arden.compiler.node.AWblkScenarioBlock;
import arden.compiler.node.AWblkWhenBlockAnd;
import arden.compiler.node.AWriteThenPhrase;
import arden.compiler.node.AWriteatThenPhrase;
import arden.compiler.node.AWritemsgThenPhrase;
import arden.compiler.node.AWritemsgatThenPhrase;
import arden.compiler.node.AWritenothingThenPhrase;
import arden.compiler.node.AWritenothingatThenPhrase;
import arden.compiler.node.AWstmtScenarioBlock;
import arden.compiler.node.AWstmtWhenBlockAnd;
import arden.compiler.node.PExprIndeterminate;
import arden.compiler.node.PExprMixed;
import arden.runtime.ArdenTime;
import arden.runtime.MedicalLogicModule;

public final class ScenarioCompiler extends VisitorBase {

	private final CompilerContext context;
	private String mlmSelf;
	private int contextVar;
	private int engineVar;
	private int returnedValuesIndexVar;
	private int returnedValuesVar;
	private int writtenMessagesVar;
	private int writtenMessagesIndexVar;

	public ScenarioCompiler(CompilerContext context, String mlmSelf, int contextVar, int engineVar) {
		this.context = context;
		this.contextVar = contextVar;
		this.engineVar = engineVar;
		this.mlmSelf = mlmSelf;
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
		context.writer.sequencePoint(node.getGiven().getLine());
		node.getGivenPhrase().apply(this);
		node.getGivenBlockAnd().apply(this);
	}

	@Override
	public void caseAGstmtScenarioBlock(AGstmtScenarioBlock node) {
		context.writer.sequencePoint(node.getGiven().getLine());
		node.getGivenPhrase().apply(this);
	}

	@Override
	public void caseASblkGivenBlockAnd(ASblkGivenBlockAnd node) {
		node.getScenarioBlock().apply(this);
	}

	@Override
	public void caseAGblkGivenBlockAnd(AGblkGivenBlockAnd node) {
		context.writer.sequencePoint(node.getAnd().getLine());
		node.getGivenPhrase().apply(this);
		node.getGivenBlockAnd().apply(this);
	}

	@Override
	public void caseAGstmtGivenBlockAnd(AGstmtGivenBlockAnd node) {
		context.writer.sequencePoint(node.getAnd().getLine());
		node.getGivenPhrase().apply(this);
	}

	// when blocks

	@Override
	public void caseAWblkScenarioBlock(AWblkScenarioBlock node) {
		context.writer.sequencePoint(node.getWhen().getLine());
		node.getWhenPhrase().apply(this);
		node.getWhenBlockAnd().apply(this);
	}

	@Override
	public void caseAWstmtScenarioBlock(AWstmtScenarioBlock node) {
		context.writer.sequencePoint(node.getWhen().getLine());
		node.getWhenPhrase().apply(this);
	}

	@Override
	public void caseASblkWhenBlockAnd(ASblkWhenBlockAnd node) {
		node.getScenarioBlock().apply(this);
	}

	@Override
	public void caseAWblkWhenBlockAnd(AWblkWhenBlockAnd node) {
		context.writer.sequencePoint(node.getAnd().getLine());
		node.getWhenPhrase().apply(this);
		node.getWhenBlockAnd().apply(this);
	}

	@Override
	public void caseAWstmtWhenBlockAnd(AWstmtWhenBlockAnd node) {
		context.writer.sequencePoint(node.getAnd().getLine());
		node.getWhenPhrase().apply(this);
	}

	// then blocks

	@Override
	public void caseATblkScenarioBlock(ATblkScenarioBlock node) {
		context.writer.sequencePoint(node.getThen().getLine());
		node.getThenPhrase().apply(this);
		node.getThenBlockAnd().apply(this);
	}

	@Override
	public void caseATstmtScenarioBlock(ATstmtScenarioBlock node) {
		context.writer.sequencePoint(node.getThen().getLine());
		node.getThenPhrase().apply(this);
	}

	@Override
	public void caseASblkThenBlockAnd(ASblkThenBlockAnd node) {
		node.getScenarioBlock().apply(this);
	}

	@Override
	public void caseATblkThenBlockAnd(ATblkThenBlockAnd node) {
		context.writer.sequencePoint(node.getAnd().getLine());
		node.getThenPhrase().apply(this);
		node.getThenBlockAnd().apply(this);
	}

	@Override
	public void caseATstmtThenBlockAnd(ATstmtThenBlockAnd node) {
		context.writer.sequencePoint(node.getAnd().getLine());
		node.getThenPhrase().apply(this);
	}

	// given statements

	@Override
	public void caseAAssGivenPhrase(AAssGivenPhrase node) {
		// given_phrase = {ass} read? mapping_factor P.is expr
		context.writer.loadVariable(contextVar);
		context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getMappingFactor()));
		new ExpressionCompiler(context).buildArrayForCommaSeparatedExpression(node.getExpr());
		context.writer.invokeInstance(ScenarioMethods.setQuery);
	}

	@Override
	public void caseAIassGivenPhrase(AIassGivenPhrase node) {
		// given_phrase = {iass} interface mapping_factor P.is expr
		context.writer.loadVariable(contextVar);
		context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getMappingFactor()));
		new ExpressionCompiler(context).buildArrayForCommaSeparatedExpression(node.getExpr());
		context.writer.invokeInstance(ScenarioMethods.setInterface);
	}

	// when statements

	@Override
	public void caseACallWhenPhrase(ACallWhenPhrase node) {
		// when_phrase = {call} mlm_definition P.is called
		context.writer.loadVariable(engineVar);
		node.getMlmDefinition().apply(this);
		context.writer.loadNull();
		context.writer.invokeInstance(ScenarioMethods.callMlm);
	}

	@Override
	public void caseACallargsWhenPhrase(ACallargsWhenPhrase node) {
		// when_phrase = {callargs} mlm_definition P.is called with expr
		context.writer.loadVariable(engineVar);
		node.getMlmDefinition().apply(this);
		new ExpressionCompiler(context).buildArrayForCommaSeparatedExpression(node.getExpr());
		context.writer.invokeInstance(ScenarioMethods.callMlm);
	}

	@Override
	public void caseACallevtWhenPhrase(ACallevtWhenPhrase node) {
		// when_phrase = {callevt} event? mapping_factor P.is called
		context.writer.loadVariable(engineVar);
		context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getMappingFactor()));
		context.writer.loadNull();
		context.writer.loadNull();
		context.writer.invokeInstance(ScenarioMethods.callEvent);
	}

	@Override
	public void caseACallevttimeWhenPhrase(ACallevttimeWhenPhrase node) {
		// when_phrase = {callevttime} event? mapping_factor P.is called with
		// time const_time
		context.writer.loadVariable(engineVar);
		context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getMappingFactor()));
		node.getConstTime().apply(this);
		context.writer.loadNull();
		context.writer.invokeInstance(ScenarioMethods.callEvent);
	}

	@Override
	public void caseACallevttimetimeWhenPhrase(ACallevttimetimeWhenPhrase node) {
		// when_phrase = {callevttimetime} event? mapping_factor P.is called
		// with time const_time eventtime [eventttime]:const_time
		context.writer.loadVariable(engineVar);
		context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getMappingFactor()));
		node.getConstTime().apply(this);
		node.getEventttime().apply(this);
		context.writer.invokeInstance(ScenarioMethods.callEvent);
	}

	@Override
	public void caseATimeWhenPhrase(ATimeWhenPhrase node) {
		// when_phrase = {time} currenttime P.is const_time
		context.writer.loadVariable(engineVar);
		node.getConstTime().apply(this);
		context.writer.invokeInstance(ScenarioMethods.setTime);
	}

	@Override
	public void caseATimealiasWhenPhrase(ATimealiasWhenPhrase node) {
		// when_phrase = {timealias} now P.is const_time;
		context.writer.loadVariable(engineVar);
		node.getConstTime().apply(this);
		context.writer.invokeInstance(ScenarioMethods.setTime);
	}

	/** Leaves a {@link MedicalLogicModule} on the Stack. */
	@Override
	public void caseAMlmMlmDefinition(AMlmMlmDefinition node) {
		// mlm_definition = {mlm} T.mlm? term
		context.writer.loadVariable(contextVar);
		context.writer.loadStringConstant(ParseHelpers.getMlmName(node.getTerm()));
		context.writer.loadNull();
		context.writer.invokeInstance(ExecutionContextMethods.findModule);
	}

	/** Leaves a {@link MedicalLogicModule} on the Stack. */
	@Override
	public void caseAMlmiMlmDefinition(AMlmiMlmDefinition node) {
		// mlm_definition = {mlmi} T.mlm? term from institution string_literal
		context.writer.loadVariable(contextVar);
		context.writer.loadStringConstant(ParseHelpers.getMlmName(node.getTerm()));
		context.writer.loadStringConstant(ParseHelpers.getLiteralStringValue(node.getStringLiteral()));
		context.writer.invokeInstance(ExecutionContextMethods.findModule);
	}

	/** Leaves a {@link MedicalLogicModule} on the Stack. */
	@Override
	public void caseAMlmsMlmDefinition(AMlmsMlmDefinition node) {
		// mlm_definition = {mlms} T.mlm? T.mlm_self
		loadMlmSelf();
	}

	/** Leaves an {@link ArdenTime} on the Stack. */
	@Override
	public void caseADateConstTime(ADateConstTime node) {
		// const_time = {date} iso_date
		long date = ParseHelpers.parseIsoDate(node.getIsoDate());
		context.writer.loadStaticField(context.codeGenerator.getTimeLiteral(date));
	}

	/** Leaves an {@link ArdenTime} on the Stack. */
	@Override
	public void caseADtimeConstTime(ADtimeConstTime node) {
		// const_time = {dtime} iso_date_time
		long date = ParseHelpers.parseIsoDateTime(node.getIsoDateTime());
		context.writer.loadStaticField(context.codeGenerator.getTimeLiteral(date));
	}
	
	private void loadMlmSelf() {
		context.writer.loadVariable(contextVar);
		context.writer.loadStringConstant(mlmSelf);
		context.writer.loadNull();
		context.writer.invokeInstance(ExecutionContextMethods.findModule);
	}

	// then statements

	@Override
	public void caseATriggerThenPhrase(ATriggerThenPhrase node) {
		// then_phrase = {trigger} mlm_definition should not? be triggered
		context.writer.loadVariable(engineVar);
		loadBoolean(node.getNot() != null);
		node.getMlmDefinition().apply(this);
		context.writer.invokeInstance(ScenarioMethods.assertIsTriggered);
	}

	@Override
	public void caseAConcludeThenPhrase(AConcludeThenPhrase node) {
		// then_phrase = {conclude} conclude should not? be expr
		context.writer.loadVariable(engineVar);
		loadBoolean(node.getNot() != null);
		loadMlmSelf();
		node.getExpr().apply(new ExpressionCompiler(context));
		context.writer.invokeInstance(ScenarioMethods.assertConclude);
	}

	@Override
	public void caseAReturnThenPhrase(AReturnThenPhrase node) {
		// then_phrase = {return} expr_indeterminate should not? be returned
		checkReturned(Collections.singletonList(node.getExprIndeterminate()), node.getNot() != null);
	}
	
	@Override
	public void caseAReturnmixedThenPhrase(AReturnmixedThenPhrase node) {
		// then_phrase = {returnmixed} expr_mixed should not? be returned
		List<PExprIndeterminate> expressions = toCommaSeparatedList(node.getExprMixed());
		checkReturned(expressions, node.getNot() != null);
	}
	
	private void checkReturned(List<PExprIndeterminate> expressions, boolean not) {
		// get return values array
		returnedValuesVar = context.allocateVariable();
		context.writer.loadVariable(engineVar);
		loadMlmSelf();
		context.writer.invokeInstance(ScenarioMethods.getReturnedValues);
		context.writer.storeVariable(returnedValuesVar);

		// index = 0
		returnedValuesIndexVar = context.allocateVariable();
		context.writer.loadIntegerConstant(0);
		context.writer.storeIntVariable(returnedValuesIndexVar);

		if (not) {
			// check return value count. if different then success, else check expressions
			context.writer.loadIntegerConstant(expressions.size());
			context.writer.loadVariable(returnedValuesVar);
			context.writer.invokeStatic(ScenarioMethods.isSameNumberOfValues);
			final Label success = new Label();
			context.writer.jumpIfZero(success);

			ExpressionCompiler notReturnExpressionCheck = new ExpressionCompiler(context) {
				
				@Override
				public void caseAConcreteExprIndeterminate(AConcreteExprIndeterminate node) {
					// expr_indeterminate = {concrete} expr_sort
					node.getExprSort().apply(this); // expected
					loadCurrentReturnedValue(); // actual
					context.writer.invokeInstance(ScenarioMethods.equals);
					context.writer.jumpIfZero(success); // different -> success
				}
				
				@Override
				public void caseAAnyExprIndeterminate(AAnyExprIndeterminate node) {
					// expr_indeterminate = {any} anything
				}
				
				@Override
				public void caseAFilterExprIndeterminate(AFilterExprIndeterminate node) {
					// expr_indeterminate = {filter} anything where expr_sort;
					loadCurrentReturnedValue();

					// store in "IT" variable
					int it = context.allocateItVariable();
					context.writer.storeVariable(it);
					node.getExprSort().apply(this);
					context.popItVariable();

					context.writer.invokeStatic(ScenarioMethods.isValidFilterResult);
					// success if filter does not match
					context.writer.jumpIfZero(success); 
				}

			};

			for (PExprIndeterminate expr : expressions) {
				expr.apply(notReturnExpressionCheck);
				context.writer.incVariable(returnedValuesIndexVar, 1);
			}

			// not jump to success label -> fail
			context.writer.loadStringConstant("The value(s) that should not be returned where returned.");
			context.writer.invokeStatic(ScenarioMethods.fail);

			// success
			context.writer.markForwardJumpsOnly(success);
		} else {
			// check return value count
			context.writer.loadIntegerConstant(expressions.size()); // expected
																	// count
			context.writer.loadVariable(returnedValuesVar); // actual count
			context.writer.invokeStatic(ScenarioMethods.assertSameNumberOfValues);

			ExpressionCompiler returnExpressionCheck = new ExpressionCompiler(context) {

				@Override
				public void caseAConcreteExprIndeterminate(AConcreteExprIndeterminate node) {
					// expr_indeterminate = {concrete} expr_sort
					context.writer.loadStringConstant("Wrong return value");
					node.getExprSort().apply(this); // expected
					loadCurrentReturnedValue(); // actual
					context.writer.invokeStatic(ScenarioMethods.assertEquals);
				}
				
				@Override
				public void caseAAnyExprIndeterminate(AAnyExprIndeterminate node) {
					// expr_indeterminate = {any} anything
				}


				@Override
				public void caseAFilterExprIndeterminate(AFilterExprIndeterminate node) {
					// expr_indeterminate = {filter} anything where expr_sort;
					loadCurrentReturnedValue();

					// store in "IT" variable
					context.writer.dup();
					int it = context.allocateItVariable();
					context.writer.storeVariable(it);
					node.getExprSort().apply(this); // expected
					context.popItVariable();

					context.writer.swap();
					context.writer.loadIntVariable(returnedValuesIndexVar);
					context.writer.invokeStatic(ScenarioMethods.assertValidFilterResult);
				}
			};

			for (PExprIndeterminate expr : expressions) {
				expr.apply(returnExpressionCheck);
				context.writer.incVariable(returnedValuesIndexVar, 1);
			}
		}
	}

	private void loadCurrentReturnedValue() {
		context.writer.loadVariable(returnedValuesVar);
		context.writer.loadIntVariable(returnedValuesIndexVar);
		context.writer.loadObjectFromArray();
	}	
	
	@Override
	public void caseAReturnnothingThenPhrase(AReturnnothingThenPhrase node) {
		// then_phrase = {returnnothing} nothing should not? be returned
		context.writer.loadVariable(engineVar);
		loadBoolean(node.getNot() != null);
		loadMlmSelf();
		context.writer.invokeInstance(ScenarioMethods.assertNothingReturned);
	}
	
	@Override
	public void caseAWriteThenPhrase(AWriteThenPhrase node) {
		// then_phrase = {write} expr_indeterminate should not? be written
		checkWritten(null, node.getExprIndeterminate(), node.getNot() != null);
	}
	
	@Override
	public void caseAWriteatThenPhrase(AWriteatThenPhrase node) {
		// then_phrase = {writeat} expr_indeterminate should not? be written at destination? mapping_factor
		String destination = ParseHelpers.getStringForMapping(node.getMappingFactor());
		checkWritten(destination, node.getExprIndeterminate(), node.getNot() != null);
	}
	
	private void checkWritten(String destination, PExprIndeterminate expr, boolean not) {
		// get messages array
		writtenMessagesVar = context.allocateVariable();
		context.writer.loadVariable(contextVar);
		if (destination != null) {
			context.writer.loadStringConstant(destination);
		} else {
			context.writer.loadNull();
		}
		context.writer.invokeInstance(ScenarioMethods.getWrittenMessages);
		context.writer.storeVariable(writtenMessagesVar);
		
		// index = 0
		writtenMessagesIndexVar = context.allocateVariable();
		context.writer.loadIntegerConstant(0);
		context.writer.storeIntVariable(writtenMessagesIndexVar);
		
		final Label loop = new Label();
		final Label success = new Label();
		final Label fail = new Label();
		
		if (not) {
			// TODO
			ExpressionCompiler notMessageCheck = new ExpressionCompiler(context) {
				
				@Override
				public void caseAConcreteExprIndeterminate(AConcreteExprIndeterminate node) {
					// expr_indeterminate = {concrete} expr_sort
					node.getExprSort().apply(this); // expected
					loadCurrentWrittenMessage(); // actual
					context.writer.invokeInstance(ScenarioMethods.equals);
					context.writer.jumpIfNonZero(fail); // same -> fail
				}
				
				@Override
				public void caseAAnyExprIndeterminate(AAnyExprIndeterminate node) {
					// expr_indeterminate = {any} anything
					context.writer.jump(fail);
				}

				@Override
				public void caseAFilterExprIndeterminate(AFilterExprIndeterminate node) {
					// expr_indeterminate = {filter} anything where expr_sort;
					loadCurrentWrittenMessage();
					
					// store in "IT" variable
					int it = context.allocateItVariable();
					context.writer.storeVariable(it);
					node.getExprSort().apply(this);
					context.popItVariable();
					
					context.writer.invokeStatic(ScenarioMethods.isValidFilterResult);
					context.writer.jumpIfNonZero(fail); // filter matches -> fail
				}
				
			};
			
			// check if no messages were written
			context.writer.loadIntVariable(writtenMessagesIndexVar);
			context.writer.loadVariable(writtenMessagesVar);
			context.writer.invokeStatic(ScenarioMethods.moreMessagesAvailable);
			context.writer.jumpIfZero(success);

			// for each message run the following generated code
			context.writer.mark(loop);
			expr.apply(notMessageCheck);
			context.writer.incVariable(writtenMessagesIndexVar, 1);
			context.writer.loadIntVariable(writtenMessagesIndexVar);
			context.writer.loadVariable(writtenMessagesVar);
			context.writer.invokeStatic(ScenarioMethods.moreMessagesAvailable);
			context.writer.jumpIfZero(success);
			context.writer.jump(loop);
			// endfor
			
			// fail
			context.writer.markForwardJumpsOnly(fail);
			context.writer.loadStringConstant("The message that should not be written was written.");
			context.writer.invokeStatic(ScenarioMethods.fail);
			
			// success
			context.writer.markForwardJumpsOnly(success);
		} else {
			ExpressionCompiler messageCheck = new ExpressionCompiler(context) {
				
				@Override
				public void caseAConcreteExprIndeterminate(AConcreteExprIndeterminate node) {
					// expr_indeterminate = {concrete} expr_sort
					node.getExprSort().apply(this); // expected
					loadCurrentWrittenMessage(); // actual
					context.writer.invokeInstance(ScenarioMethods.equals);
					context.writer.jumpIfNonZero(success); // same -> success
				}
				
				@Override
				public void caseAAnyExprIndeterminate(AAnyExprIndeterminate node) {
					// expr_indeterminate = {any} anything
					context.writer.jump(success);
				}

				@Override
				public void caseAFilterExprIndeterminate(AFilterExprIndeterminate node) {
					// expr_indeterminate = {filter} anything where expr_sort;
					loadCurrentWrittenMessage();
					
					// store in "IT" variable
					int it = context.allocateItVariable();
					context.writer.storeVariable(it);
					node.getExprSort().apply(this);
					context.popItVariable();
					
					context.writer.invokeStatic(ScenarioMethods.isValidFilterResult);
					context.writer.jumpIfNonZero(success); // filter matches -> success
				}
				
			};
			
			// check if messages are available
			context.writer.loadStringConstant("No messages written");
			context.writer.loadIntVariable(writtenMessagesIndexVar);
			context.writer.loadVariable(writtenMessagesVar);
			context.writer.invokeStatic(ScenarioMethods.moreMessagesAvailable);
			context.writer.invokeStatic(ScenarioMethods.assertTrue);

			// for each message run the following generated code
			context.writer.mark(loop);
			expr.apply(messageCheck);
			context.writer.incVariable(writtenMessagesIndexVar, 1);
			context.writer.loadIntVariable(writtenMessagesIndexVar);
			context.writer.loadVariable(writtenMessagesVar);
			context.writer.invokeStatic(ScenarioMethods.moreMessagesAvailable);
			context.writer.jumpIfNonZero(loop);
			// endfor
			
			// fail
			context.writer.markForwardJumpsOnly(fail);
			context.writer.loadStringConstant("No matching message was written.");
			context.writer.invokeStatic(ScenarioMethods.fail);
			
			// success
			context.writer.markForwardJumpsOnly(success);
		}
	}
	
	private void loadCurrentWrittenMessage() {
		context.writer.loadVariable(writtenMessagesVar);
		context.writer.loadIntVariable(writtenMessagesIndexVar);
		context.writer.loadObjectFromArray();
	}

	@Override
	public void caseAWritemsgThenPhrase(AWritemsgThenPhrase node) {
		// then_phrase = {writemsg} message? mapping_factor should not? be written
		
		// load message as ArdenString
		String msgMapping = ParseHelpers.getStringForMapping(node.getMappingFactor());
		context.writer.loadVariable(contextVar);
		context.writer.loadStringConstant(msgMapping);
		context.writer.invokeInstance(ExecutionContextMethods.getMessage);
		
		// get messages array
		context.writer.loadVariable(contextVar);
		context.writer.loadNull();
		context.writer.invokeInstance(ScenarioMethods.getWrittenMessages);
		
		context.writer.invokeStatic(ScenarioMethods.assertMessageInList);
	}

	@Override
	public void caseAWritemsgatThenPhrase(AWritemsgatThenPhrase node) {
		// then_phrase = {writemsgat} message? [msgmapping]:mapping_factor should not? be written at destination? [destmapping]:mapping_factor
		
		// get messages array
		context.writer.loadVariable(contextVar);
		context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getDestmapping()));
		context.writer.invokeInstance(ScenarioMethods.getWrittenMessages);
		
		// load message as ArdenString
		String msgMapping = ParseHelpers.getStringForMapping(node.getMsgmapping());
		context.writer.loadVariable(contextVar);
		context.writer.loadStringConstant(msgMapping);
		context.writer.invokeInstance(ExecutionContextMethods.getMessage);
		
		context.writer.invokeStatic(ScenarioMethods.assertMessageInList);
	}

	@Override
	public void caseAWritenothingThenPhrase(AWritenothingThenPhrase node) {
		// then_phrase = {writenothing} nothing should not? be written
		context.writer.loadVariable(contextVar);
		context.writer.loadNull();
		context.writer.invokeInstance(ScenarioMethods.assertNothingWritten);
	}

	@Override
	public void caseAWritenothingatThenPhrase(AWritenothingatThenPhrase node) {
		// then_phrase = {writenothingat} nothing should not? be written at destination? mapping_factor
		context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getMappingFactor()));
		context.writer.invokeInstance(ScenarioMethods.assertNothingWritten);
	}

	@Override
	public void caseACallThenPhrase(ACallThenPhrase node) {
		// then_phrase = {call} mlm_definition should not? be called
		context.writer.loadVariable(contextVar);
		loadBoolean(node.getNot() != null);
		node.getMlmDefinition().apply(this);
		context.writer.loadNull();
		context.writer.loadNull();
		context.writer.invokeInstance(ScenarioMethods.assertMlmCalled);	
	}

	@Override
	public void caseACallargsThenPhrase(ACallargsThenPhrase node) {
		// then_phrase = {callargs} mlm_definition should not? be called with expr
		context.writer.loadVariable(contextVar);
		loadBoolean(node.getNot() != null);
		node.getMlmDefinition().apply(this);
		new ExpressionCompiler(context).buildArrayForCommaSeparatedExpression(node.getExpr());
		context.writer.loadNull();
		context.writer.invokeInstance(ScenarioMethods.assertMlmCalled);
	}

	@Override
	public void caseACalldelayThenPhrase(ACalldelayThenPhrase node) {
		// then_phrase = {calldelay} mlm_definition should not? be called delay [delayexp]:expr
		context.writer.loadVariable(contextVar);
		loadBoolean(node.getNot() != null);
		node.getMlmDefinition().apply(this);
		context.writer.loadNull();
		node.getDelayexp().apply(new ExpressionCompiler(context));
		context.writer.invokeInstance(ScenarioMethods.assertMlmCalled);
	}

	@Override
	public void caseACalldelayargsThenPhrase(ACalldelayargsThenPhrase node) {
		// then_phrase = {calldelayargs} mlm_definition should not? be called with expr delay [delayexp]:expr
		context.writer.loadVariable(contextVar);
		loadBoolean(node.getNot() != null);
		node.getMlmDefinition().apply(this);
		new ExpressionCompiler(context).buildArrayForCommaSeparatedExpression(node.getExpr());
		node.getDelayexp().apply(new ExpressionCompiler(context));
		context.writer.invokeInstance(ScenarioMethods.assertMlmCalled);
	}

	@Override
	public void caseACallevtThenPhrase(ACallevtThenPhrase node) {
		// then_phrase = {callevt} event? mapping_factor should not? be called
		context.writer.loadVariable(contextVar);
		loadBoolean(node.getNot() != null);
		context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getMappingFactor()));
		context.writer.loadNull();
		context.writer.invokeInstance(ScenarioMethods.assertEventCalled);
	}

	@Override
	public void caseACallevtdelayThenPhrase(ACallevtdelayThenPhrase node) {
		// then_phrase = {callevtdelay} event? mapping_factor should not? be called delay [delayexp]:expr
		context.writer.loadVariable(contextVar);
		loadBoolean(node.getNot() != null);
		context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getMappingFactor()));
		node.getDelayexp().apply(new ExpressionCompiler(context));
		context.writer.invokeInstance(ScenarioMethods.assertEventCalled);
	}

	@Override
	public void caseACallnothingThenPhrase(ACallnothingThenPhrase node) {
		// then_phrase = {callnothing} nothing should not? be called
		context.writer.loadVariable(contextVar);
		loadBoolean(node.getNot() != null);
		context.writer.invokeInstance(ScenarioMethods.assertNothingCalled);
	}
	
	private void loadBoolean(boolean b) {
		context.writer.loadIntegerConstant(b ? 1 : 0);
	}
	
	private List<PExprIndeterminate> toCommaSeparatedList(PExprMixed expr) {
		final ArrayList<PExprIndeterminate> output = new ArrayList<>();
		expr.apply(new VisitorBase() {
			@Override
			public void caseAExprMixed(AExprMixed node) {
				// expr_mixed = expr_indeterminate comma expr_mixed_tail
				output.add(node.getExprIndeterminate());
				node.getExprMixedTail().apply(this);
			}
			
			@Override
			public void caseAValueExprMixedTail(AValueExprMixedTail node) {
				// expr_mixed_tail = {value} expr_indeterminate
				output.add(node.getExprIndeterminate());
			}
			
			@Override
			public void caseACommavalueExprMixedTail(ACommavalueExprMixedTail node) {
			    // expr_mixed_tail = {commavalue} expr_mixed_tail comma expr_indeterminate
				node.getExprMixedTail().apply(this);
				output.add(node.getExprIndeterminate());
			}
			
			@Override
			public void caseACommaExprMixedTail(ACommaExprMixedTail node) {
			    // expr_mixed_tail = {comma} comma expr_indeterminate;
				output.add(node.getExprIndeterminate());
			}
		});
		return output;
	}
	
}

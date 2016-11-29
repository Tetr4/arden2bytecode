package arden.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import arden.codegenerator.Label;
import arden.compiler.node.AAnyExprAny;
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
import arden.compiler.node.ACommavalueExprAnyTail;
import arden.compiler.node.AConcludeThenPhrase;
import arden.compiler.node.AConcreteExprAny;
import arden.compiler.node.AEmptyScenarioBlock;
import arden.compiler.node.AFilterExprAny;
import arden.compiler.node.AGblkGivenBlockAnd;
import arden.compiler.node.AGblkScenarioBlock;
import arden.compiler.node.AGstmtGivenBlockAnd;
import arden.compiler.node.AGstmtScenarioBlock;
import arden.compiler.node.AIassGivenPhrase;
import arden.compiler.node.AMlmMlmDefinition;
import arden.compiler.node.AMlmiMlmDefinition;
import arden.compiler.node.AMlmsMlmDefinition;
import arden.compiler.node.AReturnThenPhrase;
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
import arden.compiler.node.AValueExprAnyTail;
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
import arden.compiler.node.PExprAny;
import arden.compiler.node.PExprAnyTail;
import arden.runtime.MedicalLogicModule;
import arden.runtime.validation.Call;

public final class ScenarioCompiler extends VisitorBase {

	private final CompilerContext context;
	private int mlmUnderTestVar;
	private int contextVar;
	private int engineVar;
	private int loopIndexVar;
	private int loopItemsVar;
	private String institutionSelf;

	public ScenarioCompiler(CompilerContext context, String institutionSelf, int mlmUnderTestVar, int contextVar,
			int engineVar) {
		this.context = context;
		this.contextVar = contextVar;
		this.engineVar = engineVar;
		this.mlmUnderTestVar = mlmUnderTestVar;
		this.institutionSelf = institutionSelf;
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
		new ScenarioExpressionsCompiler(context, contextVar).buildArrayForCommaSeparatedExpression(node.getExpr());
		context.writer.invokeInstance(ScenarioMethods.setQuery);
	}

	@Override
	public void caseAIassGivenPhrase(AIassGivenPhrase node) {
		// given_phrase = {iass} interface mapping_factor P.is expr
		context.writer.loadVariable(contextVar);
		context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getMappingFactor()));
		new ScenarioExpressionsCompiler(context, contextVar).buildArrayForCommaSeparatedExpression(node.getExpr());
		context.writer.invokeInstance(ScenarioMethods.setInterface);
	}

	// when statements

	@Override
	public void caseACallWhenPhrase(ACallWhenPhrase node) {
		// when_phrase = {call} T.mlm P.is called
		context.writer.loadVariable(engineVar);
		context.writer.loadNull();
		context.writer.invokeInstance(ScenarioMethods.callMlm);
	}

	@Override
	public void caseACallargsWhenPhrase(ACallargsWhenPhrase node) {
		// when_phrase = {callargs} T.mlm P.is called with expr
		context.writer.loadVariable(engineVar);
		new ScenarioExpressionsCompiler(context, contextVar).buildArrayForCommaSeparatedExpression(node.getExpr());
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
		// when_phrase = {callevttime} event? mapping_factor P.is called with time expr
		context.writer.loadVariable(engineVar);
		context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getMappingFactor()));
		node.getExpr().apply(new ScenarioExpressionsCompiler(context, contextVar));
		context.writer.loadNull();
		context.writer.invokeInstance(ScenarioMethods.callEvent);
	}

	@Override
	public void caseACallevttimetimeWhenPhrase(ACallevttimetimeWhenPhrase node) {
		// when_phrase = {callevttimetime} event? mapping_factor P.is called with time expr eventtime [eventttime]:expr
		context.writer.loadVariable(engineVar);
		context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getMappingFactor()));
		node.getExpr().apply(new ScenarioExpressionsCompiler(context, contextVar));
		node.getEventttime().apply(new ScenarioExpressionsCompiler(context, contextVar));
		context.writer.invokeInstance(ScenarioMethods.callEvent);
	}

	@Override
	public void caseATimeWhenPhrase(ATimeWhenPhrase node) {
		// when_phrase = {time} currenttime P.is expr
		context.writer.loadVariable(engineVar);
		node.getExpr().apply(new ScenarioExpressionsCompiler(context, contextVar));
		context.writer.invokeInstance(ScenarioMethods.setTime);
	}

	@Override
	public void caseATimealiasWhenPhrase(ATimealiasWhenPhrase node) {
		// when_phrase = {timealias} now P.is expr;
		context.writer.loadVariable(engineVar);
		node.getExpr().apply(new ScenarioExpressionsCompiler(context, contextVar));
		context.writer.invokeInstance(ScenarioMethods.setTime);
	}

	/** Leaves a {@link MedicalLogicModule} on the Stack. */
	@Override
	public void caseAMlmMlmDefinition(AMlmMlmDefinition node) {
		// mlm_definition = {mlm} T.mlm? term
		context.writer.loadVariable(contextVar);
		context.writer.loadStringConstant(ParseHelpers.getMlmName(node.getTerm()));
		context.writer.loadStringConstant(institutionSelf);
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
		context.writer.loadVariable(mlmUnderTestVar);
	}

	// then statements

	@Override
	public void caseATriggerThenPhrase(ATriggerThenPhrase node) {
		// then_phrase = {trigger} T.mlm should not? be triggered
		context.writer.loadVariable(engineVar);
		loadBoolean(node.getNot() != null);
		context.writer.invokeInstance(ScenarioMethods.assertIsTriggered);
	}

	@Override
	public void caseAConcludeThenPhrase(AConcludeThenPhrase node) {
		// then_phrase = {conclude} expr_any should not? be concluded
		if (node.getNot() != null) {
			checkNotConclude(node.getExprAny());
		} else {
			checkConclude(node.getExprAny());
		}

	}

	private void checkConclude(PExprAny expr) {
		expr.apply(new ScenarioExpressionsCompiler(context, contextVar) {

			@Override
			public void caseAConcreteExprAny(AConcreteExprAny node) {
				// expr_any = {concrete} expr_sort
				context.writer.loadStringConstant("Wrong conclude value");
				
				 // expected
				node.getExprSort().apply(this);
				
				// actual
				context.writer.loadVariable(engineVar);
				context.writer.invokeInstance(ScenarioMethods.getConclude);
				context.writer.invokeStatic(ScenarioMethods.assertEquals);
			}

			@Override
			public void caseAAnyExprAny(AAnyExprAny node) {
				// expr_any = {any} any value
			}

			@Override
			public void caseAFilterExprAny(AFilterExprAny node) {
				// expr_any = {filter} any value where expr_sort;
				context.writer.loadVariable(engineVar);
				context.writer.invokeInstance(ScenarioMethods.getConclude);

				// store in "IT" variable
				int it = context.allocateItVariable();
				context.writer.storeVariable(it);
				node.getExprSort().apply(this); // expected
				context.popItVariable();

				context.writer.invokeStatic(ScenarioMethods.isValidFilterResult);
				context.writer.loadStringConstant("Conclude value did not match");
				context.writer.swap();
				context.writer.invokeStatic(ScenarioMethods.assertTrue);
			}
		});
	}

	private void checkNotConclude(PExprAny expr) {
		expr.apply(new ScenarioExpressionsCompiler(context, contextVar) {

			@Override
			public void caseAConcreteExprAny(AConcreteExprAny node) {
				// expr_any = {concrete} expr_sort
				context.writer.loadStringConstant("Conclude matched");
				
				 // expected
				node.getExprSort().apply(this);
				
				// actual
				context.writer.loadVariable(engineVar);
				context.writer.invokeInstance(ScenarioMethods.getConclude);
				context.writer.invokeStatic(ScenarioMethods.assertNotEquals);
			}

			@Override
			public void caseAAnyExprAny(AAnyExprAny node) {
				// expr_any = {any} any value
				context.writer.loadStringConstant("Conclude matched");
				context.writer.invokeStatic(ScenarioMethods.fail);
			}

			@Override
			public void caseAFilterExprAny(AFilterExprAny node) {
				// expr_any = {filter} any value where expr_sort;
				context.writer.loadVariable(engineVar);
				context.writer.invokeInstance(ScenarioMethods.getConclude);

				// store in "IT" variable
				int it = context.allocateItVariable();
				context.writer.storeVariable(it);
				node.getExprSort().apply(this); // expected
				context.popItVariable();

				context.writer.invokeStatic(ScenarioMethods.isValidFilterResult);
				context.writer.loadStringConstant("Conclude value matched");
				context.writer.swap();
				context.writer.invokeStatic(ScenarioMethods.assertFalse);
			}
		});
		
	}

	@Override
	public void caseAReturnThenPhrase(AReturnThenPhrase node) {
		// then_phrase = {return} expr_any expr_any_tail? should not? be returned
		List<PExprAny> expressions = toCommaSeparatedList(node.getExprAny(), node.getExprAnyTail());
		checkReturned(expressions, node.getNot() != null);
	}
	
	@Override
	public void caseAReturnnothingThenPhrase(AReturnnothingThenPhrase node) {
		// then_phrase = {returnnothing} nothing should not? be returned
		context.writer.loadVariable(engineVar);
		loadBoolean(node.getNot() != null);
		context.writer.invokeInstance(ScenarioMethods.assertNothingReturned);
	}

	private void checkReturned(List<PExprAny> expressions, boolean not) {
		// get return values array
		loopItemsVar = context.allocateVariable();
		context.writer.loadVariable(engineVar);
		context.writer.invokeInstance(ScenarioMethods.getReturnedValues);
		context.writer.storeVariable(loopItemsVar);

		// index = 0
		loopIndexVar = context.allocateVariable();
		context.writer.loadIntegerConstant(0);
		context.writer.storeIntVariable(loopIndexVar);

		if (not) {
			// check return value count. if different then success, else check expressions
			context.writer.loadIntegerConstant(expressions.size());
			context.writer.loadVariable(loopItemsVar);
			context.writer.invokeStatic(ScenarioMethods.isSameNumberOfValues);
			final Label success = new Label();
			context.writer.jumpIfZero(success);

			for (PExprAny expr : expressions) {
				checkNotReturnExpression(success, expr);
				context.writer.incVariable(loopIndexVar, 1);
			}

			// fail
			context.writer.loadStringConstant("The value(s) that should not be returned where returned.");
			context.writer.invokeStatic(ScenarioMethods.fail);

			// success
			context.writer.markForwardJumpsOnly(success);
		} else {
			// check return value count
			context.writer.loadIntegerConstant(expressions.size()); // expected
			context.writer.loadVariable(loopItemsVar); // actual
			context.writer.invokeStatic(ScenarioMethods.assertSameNumberOfReturnValues);

			for (PExprAny expr : expressions) {
				checkReturnExpression(expr);
				context.writer.incVariable(loopIndexVar, 1);
			}
		}
	}
	
	private void checkNotReturnExpression(final Label success, PExprAny expr) {
		expr.apply(new ScenarioExpressionsCompiler(context, contextVar) {

			@Override
			public void caseAConcreteExprAny(AConcreteExprAny node) {
				// expr_any = {concrete} expr_sort
				node.getExprSort().apply(this); // expected
				loadCurrentLoopItem(); // actual
				context.writer.invokeInstance(ScenarioMethods.equals);
				context.writer.jumpIfZero(success); // different -> success
			}

			@Override
			public void caseAAnyExprAny(AAnyExprAny node) {
				// expr_any = {any} any value
			}

			@Override
			public void caseAFilterExprAny(AFilterExprAny node) {
				// expr_any = {filter} any value where expr_sort;
				loadCurrentLoopItem();

				// store in "IT" variable
				int it = context.allocateItVariable();
				context.writer.storeVariable(it);
				node.getExprSort().apply(this);
				context.popItVariable();

				context.writer.invokeStatic(ScenarioMethods.isValidFilterResult);
				// success if filter does not match
				context.writer.jumpIfZero(success);
			}
		});
	}

	private void checkReturnExpression(PExprAny expr) {
		expr.apply(new ScenarioExpressionsCompiler(context, contextVar) {

			@Override
			public void caseAConcreteExprAny(AConcreteExprAny node) {
				// expr_any = {concrete} expr_sort
				context.writer.loadStringConstant("Wrong return value");
				node.getExprSort().apply(this); // expected
				loadCurrentLoopItem(); // actual
				context.writer.invokeStatic(ScenarioMethods.assertEquals);
			}

			@Override
			public void caseAAnyExprAny(AAnyExprAny node) {
				// expr_any = {any} any value
			}

			@Override
			public void caseAFilterExprAny(AFilterExprAny node) {
				// expr_any = {filter} any value where expr_sort;
				loadCurrentLoopItem();

				// store in "IT" variable
				context.writer.dup();
				int it = context.allocateItVariable();
				context.writer.storeVariable(it);
				node.getExprSort().apply(this); // expected
				context.popItVariable();

				context.writer.swap();
				context.writer.loadIntVariable(loopIndexVar);
				context.writer.invokeStatic(ScenarioMethods.assertValidFilterResult);
			}
		});
	}

	private void loadCurrentLoopItem() {
		context.writer.loadVariable(loopItemsVar);
		context.writer.loadIntVariable(loopIndexVar);
		context.writer.loadObjectFromArray();
	}	
	
	@Override
	public void caseAWriteThenPhrase(AWriteThenPhrase node) {
		// then_phrase = {write} expr_any should not? be written
		checkWritten(null, node.getExprAny(), node.getNot() != null);
	}
	
	@Override
	public void caseAWriteatThenPhrase(AWriteatThenPhrase node) {
		// then_phrase = {writeat} expr_any should not? be written at destination? mapping_factor
		String destinationMapping = ParseHelpers.getStringForMapping(node.getMappingFactor());
		checkWritten(destinationMapping, node.getExprAny(), node.getNot() != null);
	}
	
	private void checkWritten(String destination, PExprAny expr, boolean not) {
		// get messages array
		loopItemsVar = context.allocateVariable();
		context.writer.loadVariable(contextVar);
		String destName;
		if (destination != null) {
			destName = "{" + destination + "}";
			context.writer.loadStringConstant(destination);
		} else {
			destName = "<default destination>";
			context.writer.loadNull();
		}
		context.writer.invokeInstance(ScenarioMethods.getWrittenMessages);
		context.writer.storeVariable(loopItemsVar);
		
		// index = 0
		loopIndexVar = context.allocateVariable();
		context.writer.loadIntegerConstant(0);
		context.writer.storeIntVariable(loopIndexVar);
		
		final Label next = new Label();
		final Label success = new Label();
		final Label fail = new Label();
		
		if (not) {
			// check if no messages were written
			context.writer.loadIntVariable(loopIndexVar);
			context.writer.loadVariable(loopItemsVar);
			context.writer.invokeStatic(ScenarioMethods.moreItemsAvailable);
			context.writer.jumpIfZero(success);

			// for each message run the following generated code
			context.writer.mark(next);
			checkNotMessageWritten(fail, expr);
			context.writer.incVariable(loopIndexVar, 1);
			context.writer.loadIntVariable(loopIndexVar);
			context.writer.loadVariable(loopItemsVar);
			context.writer.invokeStatic(ScenarioMethods.moreItemsAvailable);
			context.writer.jumpIfZero(success);
			context.writer.jump(next);
			// endfor
			
			// fail
			context.writer.markForwardJumpsOnly(fail);
			context.writer.loadStringConstant("The message that should not be written at " + destName + " was written.");
			context.writer.invokeStatic(ScenarioMethods.fail);
			
			// success
			context.writer.markForwardJumpsOnly(success);
		} else {
			// check if messages are available
			context.writer.loadStringConstant("No messages written at " + destName);
			context.writer.loadIntVariable(loopIndexVar);
			context.writer.loadVariable(loopItemsVar);
			context.writer.invokeStatic(ScenarioMethods.moreItemsAvailable);
			context.writer.invokeStatic(ScenarioMethods.assertTrue);

			// for each message run the following generated code
			context.writer.mark(next);
			checkMessageWritten(success, expr);
			context.writer.incVariable(loopIndexVar, 1);
			context.writer.loadIntVariable(loopIndexVar);
			context.writer.loadVariable(loopItemsVar);
			context.writer.invokeStatic(ScenarioMethods.moreItemsAvailable);
			context.writer.jumpIfNonZero(next);
			// endfor
			
			// fail
			context.writer.loadStringConstant("No matching message was written at " + destName);
			context.writer.invokeStatic(ScenarioMethods.fail);
			
			// success
			context.writer.markForwardJumpsOnly(success);
		}
	}
	
	private void checkMessageWritten(final Label success, PExprAny expr) {
		expr.apply(new ScenarioExpressionsCompiler(context, contextVar) {

			@Override
			public void caseAConcreteExprAny(AConcreteExprAny node) {
				// expr_any = {concrete} expr_sort
				node.getExprSort().apply(this); // expected
				loadCurrentLoopItem(); // actual
				context.writer.invokeInstance(ScenarioMethods.equals);
				context.writer.jumpIfNonZero(success); // same -> success
			}

			@Override
			public void caseAAnyExprAny(AAnyExprAny node) {
				// expr_any = {any} any value
				context.writer.jump(success);
			}

			@Override
			public void caseAFilterExprAny(AFilterExprAny node) {
				// expr_any = {filter} any value where expr_sort;
				loadCurrentLoopItem();

				// store in "IT" variable
				int it = context.allocateItVariable();
				context.writer.storeVariable(it);
				node.getExprSort().apply(this);
				context.popItVariable();

				context.writer.invokeStatic(ScenarioMethods.isValidFilterResult);
				// filter matches -> success
				context.writer.jumpIfNonZero(success);
			}
		});
	}

	private void checkNotMessageWritten(final Label fail, PExprAny expr) {
		expr.apply(new ScenarioExpressionsCompiler(context, contextVar) {

			@Override
			public void caseAConcreteExprAny(AConcreteExprAny node) {
				// expr_any = {concrete} expr_sort
				node.getExprSort().apply(this); // expected
				loadCurrentLoopItem(); // actual
				context.writer.invokeInstance(ScenarioMethods.equals);
				context.writer.jumpIfNonZero(fail); // same -> fail
			}

			@Override
			public void caseAAnyExprAny(AAnyExprAny node) {
				// expr_any = {any} any value
				context.writer.jump(fail);
			}

			@Override
			public void caseAFilterExprAny(AFilterExprAny node) {
				// expr_any = {filter} any value where expr_sort;
				loadCurrentLoopItem();

				// store in "IT" variable
				int it = context.allocateItVariable();
				context.writer.storeVariable(it);
				node.getExprSort().apply(this);
				context.popItVariable();

				context.writer.invokeStatic(ScenarioMethods.isValidFilterResult);
				// filter matches -> fail
				context.writer.jumpIfNonZero(fail);
			}
		});
	}

	@Override
	public void caseAWritemsgThenPhrase(AWritemsgThenPhrase node) {
		// then_phrase = {writemsg} message? mapping_factor should not? be written
		
		loadBoolean(node.getNot() != null);
		
		// load message as ArdenString
		String msgMapping = ParseHelpers.getStringForMapping(node.getMappingFactor());
		context.writer.loadVariable(contextVar);
		context.writer.loadStringConstant(msgMapping);
		context.writer.invokeInstance(ExecutionContextMethods.getMessage);
		
		// get messages array
		context.writer.loadVariable(contextVar);
		context.writer.loadNull();
		context.writer.invokeInstance(ScenarioMethods.getWrittenMessages);
		
		context.writer.loadStringConstant("<default destination>");
		context.writer.invokeStatic(ScenarioMethods.assertMessageInList);
	}

	@Override
	public void caseAWritemsgatThenPhrase(AWritemsgatThenPhrase node) {
		// then_phrase = {writemsgat} message? [msgmapping]:mapping_factor should not? be written at destination? [destmapping]:mapping_factor
		String destinationMapping = ParseHelpers.getStringForMapping(node.getDestmapping());
		
		loadBoolean(node.getNot() != null);
		
		// load message as ArdenString
		String msgMapping = ParseHelpers.getStringForMapping(node.getMsgmapping());
		context.writer.loadVariable(contextVar);
		context.writer.loadStringConstant(msgMapping);
		context.writer.invokeInstance(ExecutionContextMethods.getMessage);
		
		// get messages array
		context.writer.loadVariable(contextVar);
		context.writer.loadStringConstant(destinationMapping);
		context.writer.invokeInstance(ScenarioMethods.getWrittenMessages);
		
		context.writer.loadStringConstant(destinationMapping);
		context.writer.invokeStatic(ScenarioMethods.assertMessageInList);
	}

	@Override
	public void caseAWritenothingThenPhrase(AWritenothingThenPhrase node) {
		// then_phrase = {writenothing} nothing should not? be written
		context.writer.loadVariable(contextVar);
		loadBoolean(node.getNot() != null);
		context.writer.loadNull();
		context.writer.invokeInstance(ScenarioMethods.assertNothingWritten);
	}

	@Override
	public void caseAWritenothingatThenPhrase(AWritenothingatThenPhrase node) {
		// then_phrase = {writenothingat} nothing should not? be written at destination? mapping_factor
		context.writer.loadVariable(contextVar);
		loadBoolean(node.getNot() != null);
		context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getMappingFactor()));
		context.writer.invokeInstance(ScenarioMethods.assertNothingWritten);
	}

	@Override
	public void caseACallThenPhrase(ACallThenPhrase node) {
		// then_phrase = {call} mlm_definition should not? be called
		loopItemsVar = context.allocateVariable();
		context.writer.loadVariable(contextVar);
		node.getMlmDefinition().apply(this);
		context.writer.invokeInstance(ScenarioMethods.getMlmCalls); // Returns object of type "Call"
		context.writer.storeVariable(loopItemsVar);
		
		checkCall(node.getNot() != null, null, null);
	}

	@Override
	public void caseACallargsThenPhrase(ACallargsThenPhrase node) {
		// then_phrase = {callargs} mlm_definition should not? be called with expr_any expr_any_tail?
		loopItemsVar = context.allocateVariable();
		context.writer.loadVariable(contextVar);
		node.getMlmDefinition().apply(this);
		context.writer.invokeInstance(ScenarioMethods.getMlmCalls);
		context.writer.storeVariable(loopItemsVar);
		
		List<PExprAny> params = toCommaSeparatedList(node.getExprAny(), node.getExprAnyTail());
		checkCall(node.getNot() != null, params, null);
	}
	
	@Override
	public void caseACalldelayThenPhrase(ACalldelayThenPhrase node) {
		// then_phrase = {calldelay} mlm_definition should not? be called delay [expr_delay]:expr_any
		loopItemsVar = context.allocateVariable();
		context.writer.loadVariable(contextVar);
		node.getMlmDefinition().apply(this);
		context.writer.invokeInstance(ScenarioMethods.getMlmCalls);
		context.writer.storeVariable(loopItemsVar);
		
		checkCall(node.getNot() != null, null, node.getExprDelay());
	}

	@Override
	public void caseACalldelayargsThenPhrase(ACalldelayargsThenPhrase node) {
		// then_phrase = {calldelayargs} mlm_definition should not? be called with expr_any expr_any_tail? delay [expr_delay]:expr_any
		loopItemsVar = context.allocateVariable();
		context.writer.loadVariable(contextVar);
		node.getMlmDefinition().apply(this);
		context.writer.invokeInstance(ScenarioMethods.getMlmCalls); // Returns object of type "Call"
		context.writer.storeVariable(loopItemsVar);
		
		List<PExprAny> params = toCommaSeparatedList(node.getExprAny(), node.getExprAnyTail());
		checkCall(node.getNot() != null, params, node.getExprDelay());
	}

	/** Requires that an array of {@link Call}s must be loaded in the loopItemsVar variable! */
	private void checkCall(boolean not, List<PExprAny> args, PExprAny delay) {
		// index = -1; because of do-while loop
		loopIndexVar = context.allocateVariable();
		context.writer.loadIntegerConstant(-1);
		context.writer.storeIntVariable(loopIndexVar);
		
		final Label next = new Label();
		final Label success = new Label();
		final Label fail = new Label();
		
		if (not) {
			// check if nothing was called
			context.writer.loadIntVariable(loopIndexVar);
			context.writer.loadVariable(loopItemsVar);
			context.writer.invokeStatic(ScenarioMethods.moreItemsAvailable);
			context.writer.jumpIfZero(success);

			// for each call run the following generated code
			context.writer.mark(next);
			context.writer.incVariable(loopIndexVar, 1);
			context.writer.loadIntVariable(loopIndexVar);
			context.writer.loadVariable(loopItemsVar);
			context.writer.invokeStatic(ScenarioMethods.moreItemsAvailable);
			context.writer.jumpIfZero(success); // not matching call found
			checkArgsMatch(next, args); // go on if matched, else check next
			checkDelayMatches(next, delay); // fail if matched, else check next
			// endfor
			
			// fail
			context.writer.loadStringConstant("The call was made.");
			context.writer.invokeStatic(ScenarioMethods.fail);
			
			// success
			context.writer.markForwardJumpsOnly(success);
		} else {
			// for each message
			context.writer.mark(next);
			context.writer.incVariable(loopIndexVar, 1);
			context.writer.loadIntVariable(loopIndexVar);
			context.writer.loadVariable(loopItemsVar);
			context.writer.invokeStatic(ScenarioMethods.moreItemsAvailable);
			context.writer.jumpIfZero(fail); // no calls left to check -> fail
			checkArgsMatch(next, args); // continue with next call if not matched
			checkDelayMatches(next, delay); // continue with next call if not matched
			context.writer.jump(success); // not jump back -> success
			// endfor

			// fail
			context.writer.markForwardJumpsOnly(fail);
			context.writer.loadStringConstant("A matching call was not made.");
			context.writer.invokeStatic(ScenarioMethods.fail);

			// success
			context.writer.markForwardJumpsOnly(success);
		}
	}
	
	/** Generates code that jumps to fail label if args do not match  */
	private void checkArgsMatch(final Label fail, List<PExprAny> args) {
		// for each arg if not matches -> fail
		if (args == null) {
			args = Collections.emptyList();
		}
		
		// get args array from MlmCall
		final int argsVar = context.allocateVariable();
		loadCurrentLoopItem(); 
		context.writer.loadInstanceField(ScenarioMethods.callArgs);
		context.writer.storeVariable(argsVar);

		// index = 0
		final int argsIndexVar = context.allocateVariable();
		context.writer.loadIntegerConstant(0);
		context.writer.storeIntVariable(argsIndexVar);
		
		// check args count
		context.writer.loadIntegerConstant(args.size()); // expected
		context.writer.loadVariable(argsVar); // actual
		context.writer.invokeStatic(ScenarioMethods.isSameNumberOfValues);
		context.writer.jumpIfZero(fail); // not same number of args

		for (PExprAny argExpr : args) {
			
			argExpr.apply(new ScenarioExpressionsCompiler(context, contextVar) {

				@Override
				public void caseAConcreteExprAny(AConcreteExprAny node) {
					// expr_any = {concrete} expr_sort
					// expected
					node.getExprSort().apply(this); 
					
					// actual
					context.writer.loadVariable(argsVar);
					context.writer.loadIntVariable(argsIndexVar);
					context.writer.loadObjectFromArray();
					
					context.writer.invokeInstance(ScenarioMethods.equals);
					context.writer.jumpIfZero(fail); // args not equal
				}

				@Override
				public void caseAAnyExprAny(AAnyExprAny node) {
					// expr_any = {any} any value
				}

				@Override
				public void caseAFilterExprAny(AFilterExprAny node) {
					// expr_any = {filter} any value where expr_sort;
					context.writer.loadVariable(argsVar);
					context.writer.loadIntVariable(argsIndexVar);
					context.writer.loadObjectFromArray();

					// store in "IT" variable
					int it = context.allocateItVariable();
					context.writer.storeVariable(it);
					node.getExprSort().apply(this); // expected
					context.popItVariable();

					context.writer.invokeStatic(ScenarioMethods.isValidFilterResult);
					context.writer.jumpIfZero(fail); // filter did not match
				}
			});
			
			context.writer.incVariable(argsIndexVar, 1);
		}
	}
	
	/** Generates code that jumps to fail label if delay does not match */
	private void checkDelayMatches(final Label fail, PExprAny delay) {
		// if not matches -> fail
		if (delay == null) {
			loadCurrentLoopItem();
			context.writer.loadInstanceField(ScenarioMethods.callDelay);
			context.writer.invokeStatic(ScenarioMethods.isZeroDelay);
			context.writer.jumpIfZero(fail); // delay not equal
		} else {
			delay.apply(new ScenarioExpressionsCompiler(context, contextVar) {

				@Override
				public void caseAConcreteExprAny(AConcreteExprAny node) {
					// expr_any = {concrete} expr_sort

					// expected delay
					node.getExprSort().apply(this);

					// actual delay
					loadCurrentLoopItem();
					context.writer.loadInstanceField(ScenarioMethods.callDelay);

					context.writer.invokeInstance(ScenarioMethods.equals);
					context.writer.jumpIfZero(fail); // delay not equal
				}

				@Override
				public void caseAAnyExprAny(AAnyExprAny node) {
					// expr_any = {any} any value
				}

				@Override
				public void caseAFilterExprAny(AFilterExprAny node) {
					// expr_any = {filter} any value where expr_sort;

					// load delay
					loadCurrentLoopItem();
					context.writer.loadInstanceField(ScenarioMethods.callDelay);

					// store in "IT" variable
					int it = context.allocateItVariable();
					context.writer.storeVariable(it);
					node.getExprSort().apply(this); // expected delay
					context.popItVariable();

					context.writer.invokeStatic(ScenarioMethods.isValidFilterResult);
					context.writer.jumpIfZero(fail); // filter did not match
				}
			});
		}
	}

	@Override
	public void caseACallevtThenPhrase(ACallevtThenPhrase node) {
		// then_phrase = {callevt} event? mapping_factor should not? be called
		loopItemsVar = context.allocateVariable();
		context.writer.loadVariable(contextVar);
		context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getMappingFactor()));
		context.writer.invokeInstance(ScenarioMethods.getEventCalls); // Returns object of type "Call"
		context.writer.storeVariable(loopItemsVar);
		
		checkCall(node.getNot() != null, null, null);
	}

	@Override
	public void caseACallevtdelayThenPhrase(ACallevtdelayThenPhrase node) {
		// then_phrase = {callevtdelay} event? mapping_factor should not? be called delay [expr_delay]:expr_any
		loopItemsVar = context.allocateVariable();
		context.writer.loadVariable(contextVar);
		context.writer.loadStringConstant(ParseHelpers.getStringForMapping(node.getMappingFactor()));
		context.writer.invokeInstance(ScenarioMethods.getEventCalls); // Returns object of type "Call"
		context.writer.storeVariable(loopItemsVar);
		
		checkCall(node.getNot() != null, null, node.getExprDelay());
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
	
	private List<PExprAny> toCommaSeparatedList(PExprAny head, PExprAnyTail tail) {
		final List<PExprAny> output = new ArrayList<>();
		if (head != null) {
			output.add(head);
		}

		if (tail != null) {
			tail.apply(new VisitorBase() {
				
				@Override
				public void caseAValueExprAnyTail(AValueExprAnyTail node) {
					// expr_any_tail = {value} comma expr_any
					output.add(node.getExprAny());
				}
				
				@Override
				public void caseACommavalueExprAnyTail(ACommavalueExprAnyTail node) {
					// expr_any_tail = {commavalue} comma expr_any expr_any_tail;
					output.add(node.getExprAny());
					node.getExprAnyTail().apply(this);
				}
			});
		}

		return output;
	}
	
}

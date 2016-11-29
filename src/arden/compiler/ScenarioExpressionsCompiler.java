package arden.compiler;

import arden.compiler.node.AAgoExprAgo;
import arden.compiler.node.ACtimTimeValue;
import arden.compiler.node.AEtimTimeValue;
import arden.compiler.node.ANowTimeValue;
import arden.compiler.node.ATtimTimeValue;
import arden.runtime.BinaryOperator;

public class ScenarioExpressionsCompiler extends ExpressionCompiler {

	private final CompilerContext context;
	private final int scenarioContextVar;

	public ScenarioExpressionsCompiler(CompilerContext context, int scenarioContextVar) {
		super(context);
		this.context = context;
		this.scenarioContextVar = scenarioContextVar;
	}

	@Override
	public void caseAAgoExprAgo(AAgoExprAgo node) {
		// expr_ago = {ago} expr_duration ago
		loadOperator(BinaryOperator.BEFORE);
		node.getExprDuration().apply(this);
		context.writer.loadVariable(scenarioContextVar);
		context.writer.invokeInstance(ExecutionContextMethods.getCurrentTime);
		invokeLoadedBinaryOperator();
	}

	@Override
	public void caseANowTimeValue(ANowTimeValue node) {
		// time_value = {now} now
		context.writer.loadVariable(scenarioContextVar);
		context.writer.invokeInstance(ExecutionContextMethods.getCurrentTime);
	}

	@Override
	public void caseACtimTimeValue(ACtimTimeValue node) {
		// time_value = {ctim} currenttime
		context.writer.loadVariable(scenarioContextVar);
		context.writer.invokeInstance(ExecutionContextMethods.getCurrentTime);
	}

	@Override
	public void caseAEtimTimeValue(AEtimTimeValue node) {
		// time_value = {etim} eventtime
		throw new RuntimeException("EVENTTIME can't be used in Scenarios");
	}

	@Override
	public void caseATtimTimeValue(ATtimTimeValue node) {
		// time_value = {ttim} triggertime
		throw new RuntimeException("TRIGGERTIME can't be used in Scenarios");
	}

}

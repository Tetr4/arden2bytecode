package arden.compiler;

import java.lang.reflect.Method;

import org.junit.Assert;

import arden.compiler.node.AEmptyThenBlock;
import arden.compiler.node.AEmptyThenBlockAnd;
import arden.compiler.node.AGblkGivenBlock;
import arden.compiler.node.AGblkGivenBlockAnd;
import arden.compiler.node.AGivenBlock;
import arden.compiler.node.AGivenBlockAnd;
import arden.compiler.node.AGivenStatement;
import arden.compiler.node.AGstmtGivenBlock;
import arden.compiler.node.AGstmtGivenBlockAnd;
import arden.compiler.node.AScenarioSlot;
import arden.compiler.node.ATblkThenBlock;
import arden.compiler.node.ATblkThenBlockAnd;
import arden.compiler.node.AThenStatement;
import arden.compiler.node.ATstmtThenBlock;
import arden.compiler.node.ATstmtThenBlockAnd;
import arden.compiler.node.AWblkWhenBlock;
import arden.compiler.node.AWblkWhenBlockAnd;
import arden.compiler.node.AWhenBlock;
import arden.compiler.node.AWhenBlockAnd;
import arden.compiler.node.AWhenStatement;
import arden.compiler.node.AWstmtWhenBlock;
import arden.compiler.node.AWstmtWhenBlockAnd;

public final class ScenarioCompiler extends VisitorBase {

	private final CompilerContext context;

	public ScenarioCompiler(CompilerContext context) {
		this.context = context;
	}

	// scenario slot

	@Override
	public void caseAScenarioSlot(AScenarioSlot node) {
		node.getGivenBlock().apply(this);
	}

	// given block

	@Override
	public void caseAGblkGivenBlock(AGblkGivenBlock node) {
		node.getGivenStatement().apply(this);
		node.getGivenBlockAnd().apply(this);
	}

	@Override
	public void caseAGstmtGivenBlock(AGstmtGivenBlock node) {
		node.getGivenStatement().apply(this);
	}

	@Override
	public void caseAGivenBlock(AGivenBlock node) {
		node.getWhenBlock().apply(this);
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

	@Override
	public void caseAGivenBlockAnd(AGivenBlockAnd node) {
		node.getWhenBlock().apply(this);
	}

	// when block

	@Override
	public void caseAWblkWhenBlock(AWblkWhenBlock node) {
		node.getWhenStatement().apply(this);
		node.getWhenBlockAnd().apply(this);
	}

	@Override
	public void caseAWstmtWhenBlock(AWstmtWhenBlock node) {
		node.getWhenStatement().apply(this);
	}

	@Override
	public void caseAWhenBlock(AWhenBlock node) {
		node.getThenBlock().apply(this);
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

	@Override
	public void caseAWhenBlockAnd(AWhenBlockAnd node) {
		node.getThenBlock().apply(this);
	}

	// then block

	@Override
	public void caseATblkThenBlock(ATblkThenBlock node) {
		node.getThenStatement().apply(this);
		node.getThenBlockAnd().apply(this);
	}

	@Override
	public void caseATstmtThenBlock(ATstmtThenBlock node) {
		node.getThenStatement().apply(this);
	}

	@Override
	public void caseAEmptyThenBlock(AEmptyThenBlock node) {
		// do nothing
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

	@Override
	public void caseAEmptyThenBlockAnd(AEmptyThenBlockAnd node) {
		// do nothing
	}

	// statements

	@Override
	public void caseAGivenStatement(AGivenStatement node) {
		// TODO implement me
		context.writer.loadIntegerConstant(1);
	}

	@Override
	public void caseAWhenStatement(AWhenStatement node) {
		// TODO implement me
	}

	@Override
	public void caseAThenStatement(AThenStatement node) {
		// TODO implement me
		Method assertTrue;
		try {
			assertTrue = Assert.class.getMethod("assertTrue", Boolean.TYPE);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		// context.writer.loadIntegerConstant(1);
		context.writer.invokeStatic(assertTrue);
	}

}

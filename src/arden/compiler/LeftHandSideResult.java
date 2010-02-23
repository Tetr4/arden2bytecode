package arden.compiler;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import arden.codegenerator.FieldReference;
import arden.compiler.node.Switchable;
import arden.compiler.node.TIdentifier;
import arden.compiler.node.TNow;
import arden.compiler.node.Token;
import arden.runtime.ArdenValue;
import arden.runtime.UnaryOperator;

/**
 * Represents the result of a LeftHandSideAnalyzer-run. That is, a high-level
 * representation of the syntax tree on the left hand side of expressions.
 * 
 * @author Daniel Grunwald
 * 
 */
abstract class LeftHandSideResult {
	public abstract Token getPosition();

	/**
	 * Assigns an expression (to be compiled with ExpressionCompiler) to the
	 * variable represented by this LeftHandSideResult
	 */
	public abstract void assign(CompilerContext context, Switchable expr);

	/** Reads the current value of the variable. */
	public abstract void loadValue(CompilerContext context);
}

/** Represents a simple identifier on the left-hand-side: "identifier := ...;" */
final class LeftHandSideIdentifier extends LeftHandSideResult {
	public final TIdentifier identifier;

	public LeftHandSideIdentifier(TIdentifier identifier) {
		this.identifier = identifier;
	}

	@Override
	public Token getPosition() {
		return identifier;
	}

	@Override
	public void assign(CompilerContext context, Switchable expr) {
		context.writer.sequencePoint(identifier.getLine());
		Variable v = context.codeGenerator.getVariable(identifier.getText());
		if (v == null) {
			FieldReference f = context.codeGenerator.createInitializedField(identifier.getText(), Modifier.PRIVATE);
			v = new DataVariable(identifier, f);
			context.codeGenerator.addVariable(v);
		}
		expr.apply(new ExpressionCompiler(context));
		v.saveValue(context, identifier);
	};

	@Override
	public void loadValue(CompilerContext context) {
		Variable v = context.codeGenerator.getVariableOrShowError(identifier);
		v.loadValue(context, getPosition());
	}
}

/** Represents an field access on the left-hand-side: "obj.identifier := ...;" */
final class LeftHandSideObjectMember extends LeftHandSideResult {
	private final LeftHandSideResult target;
	private final TIdentifier identifier;

	public LeftHandSideObjectMember(LeftHandSideResult target, TIdentifier identifier) {
		this.target = target;
		this.identifier = identifier;
	}

	@Override
	public Token getPosition() {
		return identifier;
	}

	@Override
	public void assign(CompilerContext context, Switchable expr) {
		context.writer.sequencePoint(getPosition().getLine());
		target.loadValue(context);
		context.writer.loadStringConstant(identifier.getText().toUpperCase(Locale.ENGLISH));
		expr.apply(new ExpressionCompiler(context));
		context.writer.invokeStatic(Compiler.getRuntimeHelper("setObjectMember", ArdenValue.class, String.class,
				ArdenValue.class));
	}

	@Override
	public void loadValue(CompilerContext context) {
		target.loadValue(context);
		context.writer.loadStringConstant(identifier.getText().toUpperCase(Locale.ENGLISH));
		context.writer.invokeStatic(Compiler.getRuntimeHelper("getObjectMember", ArdenValue.class, String.class));
	}
}

/**
 * Represents a list of identifiers on the left-hand-side:
 * "(ident1, ident2) := ...;"
 */
final class LeftHandSideIdentifierList extends LeftHandSideResult {
	private final ArrayList<LeftHandSideIdentifier> list = new ArrayList<LeftHandSideIdentifier>();

	public void add(LeftHandSideIdentifier ident) {
		list.add(ident);
	}

	public List<LeftHandSideIdentifier> getList() {
		return list;
	}

	@Override
	public Token getPosition() {
		if (list.size() == 0)
			return null;
		else
			return list.get(0).getPosition();
	}

	@Override
	public void assign(CompilerContext context, Switchable expr) {
		throw new RuntimeCompilerException(getPosition(),
				"A READ or CALL query must be used for initializing multiple variables.");
	}

	@Override
	public void loadValue(CompilerContext context) {
		throw new RuntimeCompilerException(getPosition(),
				"A READ or CALL query must be used for initializing multiple variables.");
	}
}

/**
 * Represents the time-of operator on the left-hand-side:
 * "TIME identifier := ...;"
 */
final class LeftHandSideTimeOfIdentifier extends LeftHandSideResult {
	public final TIdentifier identifier;

	public LeftHandSideTimeOfIdentifier(TIdentifier identifier) {
		this.identifier = identifier;
	}

	@Override
	public Token getPosition() {
		return identifier;
	}

	@Override
	public void assign(CompilerContext context, Switchable expr) {
		context.writer.sequencePoint(identifier.getLine());
		Variable v = context.codeGenerator.getVariableOrShowError(identifier);
		v.loadValue(context, identifier);
		expr.apply(new ExpressionCompiler(context));
		context.writer.invokeStatic(Compiler.getRuntimeHelper("changeTime", ArdenValue.class, ArdenValue.class));
		v.saveValue(context, identifier);
	}

	@Override
	public void loadValue(CompilerContext context) {
		Variable v = context.codeGenerator.getVariableOrShowError(identifier);
		ExpressionCompiler c = new ExpressionCompiler(context);
		c.loadOperator(UnaryOperator.TIME);
		v.loadValue(context, identifier);
		c.invokeLoadedUnaryOperator();
	}
}

/** Represents the now keyword on the left-hand-side: "NOW := ...;" */
final class LeftHandSideNow extends LeftHandSideResult {
	private final TNow now;

	public LeftHandSideNow(TNow now) {
		this.now = now;
	}

	@Override
	public Token getPosition() {
		return now;
	}

	@Override
	public void assign(CompilerContext context, Switchable expr) {
		context.writer.sequencePoint(now.getLine());
		context.writer.loadThis();
		expr.apply(new ExpressionCompiler(context));
		context.writer.storeInstanceField(context.codeGenerator.getNowField());
	}

	@Override
	public void loadValue(CompilerContext context) {
		context.writer.loadThis();
		context.writer.loadInstanceField(context.codeGenerator.getNowField());
	}
}

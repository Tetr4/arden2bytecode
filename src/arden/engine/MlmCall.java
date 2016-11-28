package arden.engine;

import java.lang.reflect.InvocationTargetException;

import arden.runtime.ArdenRunnable;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.evoke.Trigger;

public final class MlmCall extends Call {
	private final ArdenRunnable runnable;
	private final ArdenValue[] args;
	private final Trigger trigger;
	private final ExecutionContext context;

	public MlmCall(ExecutionContext context, ArdenRunnable runnable, ArdenValue[] args, Trigger trigger, int priority) {
		super(priority);
		this.context = context;
		this.runnable = runnable;
		this.args = args;
		this.trigger = trigger;
	}

	@Override
	public void run() {
		// run MLM now
		try {
			runnable.run(context, args, trigger);
		} catch (InvocationTargetException e) {
			// print error and skip this MLM
			e.printStackTrace();
		}
	}
}
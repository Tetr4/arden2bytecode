package arden.tests.specification.testcompiler.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import arden.compiler.CompilerException;
import arden.runtime.ArdenDuration;
import arden.runtime.ArdenEvent;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.MedicalLogicModule;
import arden.runtime.evoke.CallTrigger;
import arden.tests.specification.testcompiler.ArdenVersion;
import arden.tests.specification.testcompiler.TestCompiler;
import arden.tests.specification.testcompiler.TestCompilerCompiletimeException;
import arden.tests.specification.testcompiler.TestCompilerDelayedMessage;
import arden.tests.specification.testcompiler.TestCompilerException;
import arden.tests.specification.testcompiler.TestCompilerMappings;
import arden.tests.specification.testcompiler.TestCompilerResult;
import arden.tests.specification.testcompiler.TestCompilerRuntimeException;
import arden.tests.specification.testcompiler.TestCompilerSettings;

public class TestCompilerImpl implements TestCompiler {

	private arden.compiler.Compiler compiler = new arden.compiler.Compiler();

	private TestCompilerMappings mappings = new TestCompilerMappings(
			TestContext.INTERFACE_MAPPING,
			TestContext.MESSAGE_MAPPING,
			TestContext.DESTINATION_MAPPING,
			TestContext.READ_MAPPING,
			TestContext.READ_MULTIPLE_MAPPING);

	private TestCompilerSettings settings = new TestCompilerSettings(
			ArdenVersion.V2_5, ArdenVersion.V1,
			true, true, false);

	@Override
	public TestCompilerSettings getSettings() {
		return settings;
	}

	@Override
	public TestCompilerMappings getMappings() {
		return mappings;
	}

	@Override
	public void compile(String code) throws TestCompilerCompiletimeException {
		try {
			compiler.compile(new StringReader(code));
		} catch (CompilerException e) {
			throw new TestCompilerCompiletimeException(e);
		} catch (IOException e) {
			throw new TestCompilerCompiletimeException(e);
		}
	}

	@Override
	public TestCompilerResult compileAndRun(String code) throws TestCompilerException {
		// compile
		List<MedicalLogicModule> compiledMlms = new ArrayList<>();
		try {
			compiledMlms.addAll(compiler.compile(new StringReader(code)));
		} catch (CompilerException e) {
			throw new TestCompilerCompiletimeException(e);
		} catch (IOException e) {
			throw new TestCompilerCompiletimeException(e);
		}

		// run and save return values
		MedicalLogicModule firstMlm = compiledMlms.get(0);
		TestContext context = new TestContext(compiledMlms);
		TestCompilerResult result = new TestCompilerResult();

		ArdenValue[] returnValues;
		try {
			returnValues = firstMlm.run(context, null, new CallTrigger());
		} catch (Exception e) {
			throw new TestCompilerRuntimeException(e);
		} catch (Error e) {
			throw new TestCompilerRuntimeException(e);
		}
		if (returnValues != null) {
			for (ArdenValue returnValue : returnValues) {
				String stringValue = new NormalizedArdenValue(returnValue).toString();
				result.returnValues.add(stringValue);
			}
		}
		result.messages.addAll(context.getMessages());

		return result;
	}

	@Override
	public TestCompilerDelayedMessage[] compileAndRunForEvent(String code, String eventMapping, int messagesToCollect)
			throws TestCompilerException {
		// compile
		List<MedicalLogicModule> compiledMlms = new ArrayList<>();
		try {
			compiledMlms.addAll(compiler.compile(new StringReader(code)));
		} catch (CompilerException e) {
			throw new TestCompilerCompiletimeException(e);
		} catch (IOException e) {
			throw new TestCompilerCompiletimeException(e);
		}

		// create constant time for deterministic tests
		Calendar calendar = new GregorianCalendar();
		calendar.clear();
		ArdenTime startTime = new ArdenTime(calendar.getTimeInMillis());
		ArdenEvent event = new ArdenEvent(eventMapping, calendar.getTimeInMillis());
		
		TestEngine engine = new TestEngine(compiledMlms, startTime);
		engine.call(event, ArdenDuration.ZERO, 50);

		// collect messages
		List<TestCompilerDelayedMessage> messages = new ArrayList<>();
		try {
			while (messages.size() < messagesToCollect) {
				messages.add(engine.getNextDelayedMessage());
			}
		} catch (Exception e) {
			throw new TestCompilerRuntimeException(e);
		}

		return messages.toArray(new TestCompilerDelayedMessage[messages.size()]);
	}

}

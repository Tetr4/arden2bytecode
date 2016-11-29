package arden.tests.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import arden.compiler.CompiledMlm;
import arden.compiler.Compiler;
import arden.compiler.CompilerException;

public class ValidationTest extends ImplementationTest {

	static CompiledMlm parseTemplate(String dataCode, String evokeCode, String actionCode, String description,
			String scenarioCode) throws CompilerException {
		try {
			InputStream s = ValidationSyntaxTest.class.getResourceAsStream("ValidationTemplate.mlm");
			String fullCode = inputStreamToString(s)
					.replace("$DATA", dataCode)
					.replace("$EVOKE", evokeCode)
					.replace("$ACTION", actionCode)
					.replace("$DESCRIPTION", description)
					.replace("$SCENARIO", scenarioCode);
			Compiler c = new Compiler();
			return c.compileMlm(new StringReader(fullCode));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void expectFailure(String dataCode, String evokeCode, String actionCode, String scenarioCode)
			throws CompilerException {
		CompiledMlm mlm = parseTemplate(dataCode, evokeCode, actionCode, "test scenario", scenarioCode);

		JUnitCore junit = new JUnitCore();
		junit.addListener(new RunListener() {
			@Override
			public void testRunFinished(Result result) throws Exception {
				Assert.assertFalse(result.wasSuccessful());
			}
		});

		mlm.test(junit);
	}

	private void expectFailure(String actionCode, String scenarioCode) throws CompilerException {
		expectFailure("", "", actionCode, scenarioCode);
	}

	private void expectSuccess(String dataCode, String evokeCode, String actionCode, String scenarioCode)
			throws CompilerException {
		CompiledMlm mlm = parseTemplate(dataCode, evokeCode, actionCode, "test scenario", scenarioCode);

		JUnitCore junit = new JUnitCore();
		junit.addListener(new RunListener() {
			@Override
			public void testFailure(Failure failure) throws Exception {
				Assert.fail(failure.getTrace());
			}
		});

		mlm.test(junit);
	}

	private void expectSuccess(String actionCode, String scenarioCode) throws CompilerException {
		expectSuccess("", "", actionCode, scenarioCode);
	}

	@Test
	public void testAny1() throws Exception {
		expectSuccess("RETURN 5.5;",
				"WHEN THE MLM IS CALLED;"
		      + "THEN ANY VALUE SHOULD BE RETURNED;");
	}

	@Test
	public void testAny2() throws Exception {
		expectSuccess("RETURN 5.5;",
				"WHEN THE MLM IS CALLED;"
		      + "THEN ANY VALUE WHERE 10 > IT SHOULD BE RETURNED;");
	}

	@Test
	public void testAny3() throws Exception {
		expectSuccess("RETURN 5.5;",
				"WHEN THE MLM IS CALLED;"
		      + "THEN ANY VALUE WHERE IT IS WITHIN 5 TO 6 SHOULD BE RETURNED;");
	}

	@Test
	public void testAnyFailure() throws Exception {
		expectFailure("RETURN 5.5;",
				"WHEN THE MLM IS CALLED;"
		      + "THEN ANY VALUE WHERE 10 < IT SHOULD BE RETURNED;");
	}

	@Test
	public void testReadSuccess() throws Exception {
		expectSuccess("(x,y,z) := READ {XYZ};", "", "RETURN y;",
				"GIVEN {XYZ} IS 1,2,3;"
			  + "WHEN THE MLM IS CALLED;"
			  + "THEN 2 SHOULD BE RETURNED;");
	}

	@Test
	public void testReadFailure() throws Exception {
		expectFailure("(x,y,z) := READ {XYZ};", "", "RETURN y;",
				"GIVEN {xyz} IS 1,2,3;"
		      + "WHEN THE MLM IS CALLED;"
			  + "THEN 3 SHOULD BE RETURNED;");
	}

	@Test
	public void testMessage1() throws Exception {
		expectSuccess("x := MESSAGE {X}; y := DESTINATION {Y};", "", "WRITE x AT y;",
				"WHEN MLM IS CALLED;"
			  + "THEN {X} SHOULD BE WRITTEN AT {Y};");
	}

	@Test
	public void testMessage2() throws Exception {
		expectSuccess("WRITE \"yay xyz bla\";",
				"WHEN MLM IS CALLED;"
			  + "THEN ANY VALUE WHERE IT MATCHES PATTERN \"%xyz%\" SHOULD BE WRITTEN;");
	}

	@Test
	public void testMessageFailure() throws Exception {
		expectFailure("x := MESSAGE {X}; y := DESTINATION {Y};", "", "WRITE x AT y;",
				"WHEN MLM IS CALLED;"
			  + "THEN {x} SHOULD BE WRITTEN;");
	}

	@Test
	public void testMessageAs() throws Exception {
		expectSuccess("obj := OBJECT[m]; x := MESSAGE AS obj {X};", "", "WRITE x;",
				"WHEN MLM IS CALLED;"
			  + "THEN {X} SHOULD BE WRITTEN;");
	}

	@Test
	public void testInterface() throws Exception {
		expectSuccess("i := INTERFACE {inter}; x := CALL i;", "", "RETURN x",
				"GIVEN INTERFACE {inter} IS TRUE;"
			  + "WHEN MLM IS CALLED;"
			  + "THEN TRUE SHOULD BE RETURNED;");
	}

	@Test
	public void testArgument() throws Exception {
		expectSuccess("(a1, a2, a3) := ARGUMENT;", "", "RETURN a1, a2, a3",
			    "WHEN MLM IS CALLED WITH 3,2,1;"
			  + "THEN 3,2,1 SHOULD BE RETURNED;");
	}

	@Test
	public void testCall1() throws Exception {
		expectSuccess("self := MLM MLM_SELF;", "", "CALL self",
			    "WHEN MLM IS CALLED;"
			  + "THEN MLM_SELF SHOULD BE CALLED;");
	}

	@Test
	public void testCall2() throws Exception {
		expectFailure("self := MLM MLM_SELF;", "", "CALL self",
			    "WHEN MLM IS CALLED;"
			  + "THEN MLM_SELF SHOULD NOT BE CALLED;");
	}

	@Test
	public void testCall3() throws Exception {
		expectSuccess("self := MLM MLM_SELF;", "", "CALL self WITH 1,2,3",
			    "WHEN MLM IS CALLED;"
			  + "THEN MLM_SELF SHOULD NOT BE CALLED;");
	}

	@Test
	public void testCall4() throws Exception {
		expectSuccess("self := MLM MLM_SELF;", "", "CALL self WITH 1,2,3",
			    "WHEN MLM IS CALLED;"
			  + "THEN MLM_SELF SHOULD BE CALLED WITH 1,2,ANY VALUE;");
	}

	@Test
	public void testCall5() throws Exception {
		expectSuccess("self := MLM MLM_SELF;", "", "CALL self WITH 1,2,3",
			    "WHEN MLM IS CALLED;"
			  + "THEN MLM_SELF SHOULD BE CALLED WITH 1,2,ANY VALUE DELAY 0 SECONDS;");
	}

	@Test
	public void testCall6() throws Exception {
		expectSuccess("self := MLM MLM_SELF;", "", "CALL self WITH 1,2,3 DELAY 1 SECOND",
			    "WHEN MLM IS CALLED;"
			  + "THEN MLM_SELF SHOULD BE CALLED WITH 1,2,ANY VALUE DELAY ANY VALUE WHERE IT IS WITHIN .5 SECONDS TO 2 SECONDS;");
	}

	@Test
	public void testEventCall() throws Exception {
		expectSuccess("evt := EVENT {e};", "", "CALL evt DELAY 1 SECOND",
			    "WHEN MLM IS CALLED;"
			  + "THEN {e} SHOULD BE CALLED DELAY 1 SECOND;");
	}

	@Test
	public void testTrigger1() throws Exception {
		expectSuccess("", "1970-01-01", "",
				"WHEN NOW IS 1980-01-01;"
		      + "THEN THE MLM SHOULD BE TRIGGERED;");
	}

	@Test
	public void testTrigger2() throws Exception {
		expectSuccess("", "1970-01-01", "",
				"WHEN NOW IS 1960-01-01;"
		      + "THEN THE MLM SHOULD NOT BE TRIGGERED;");
	}

	@Test
	public void testConclude() throws Exception {
		expectSuccess("",
				"WHEN THE MLM IS CALLED;"
		      + "THEN TRUE SHOULD BE CONCLUDED;");
	}

	@Test
	public void testPrimaryTimeConstruction() throws Exception {
		expectSuccess("RETURN TRUE|2010-01-01;",
				"WHEN THE MLM IS CALLED;"
		      + "THEN ANY VALUE WHERE TIME OF IT = 2010-01-01 SHOULD BE RETURNED;");
	}

	@Test
	public void testPrimaryTimeConstructionCurrenttime() throws Exception {
		expectSuccess("x := READ {X};", "", "RETURN TIME OF x;",
				"GIVEN {X} IS TRUE|CURRENTTIME;"
			  + "WHEN THE MLM IS CALLED;"
		      + "THEN 1800-01-01 SHOULD BE RETURNED;");
	}
	
	@Test
	public void testPrimaryTimeConstructionAgo() throws Exception {
		expectSuccess("x := READ {X};", "", "RETURN TIME OF x;",
				"WHEN NOW IS 2000-01-01;"
			  +	"GIVEN {X} IS TRUE|(1 YEAR AGO);"
			  + "WHEN THE MLM IS CALLED;"
		      + "THEN 1999-01-01 SHOULD BE RETURNED;");
	}

	@Test
	public void testPrimaryTimeConstructionFailure() throws Exception {
		expectFailure("RETURN TRUE;",
				"WHEN THE MLM IS CALLED;"
		      + "THEN ANY VALUE WHERE TIME OF IT = 2010-01-01 SHOULD BE RETURNED;");
	}

	@Test
	public void testUndefinedValue() throws Exception {
		expectSuccess("(x,y,z) := READ {XYZ};", "", "RETURN y;",
				"WHEN THE MLM IS CALLED;"
		      + "THEN NULL SHOULD BE RETURNED;");
	}

	@Test
	public void testUndefinedTime() throws Exception {
		expectSuccess("RETURN CURRENTTIME;",
				"WHEN THE MLM IS CALLED;"
		      + "THEN 1800-01-01 SHOULD BE RETURNED;");
	}

	@Test
	public void testWhenTime() throws Exception {
		expectSuccess("RETURN CURRENTTIME;",
				"WHEN NOW IS 2001-01-01;"
		      + "WHEN THE MLM IS CALLED;"
			  + "THEN 2001-01-01 SHOULD BE RETURNED;");
	}

	@Test
	public void testEvent() throws Exception {
		expectSuccess("evt := EVENT {evt};", "evt;", "RETURN TIME OF evt;",
				"WHEN NOW IS 2001-01-01;"
			  +	"AND {evt} IS CALLED WITH TIME 2010-01-01;"
			  + "THEN 2010-01-01 SHOULD BE RETURNED;");
	}

	@Test
	public void testDelayedEvent() throws Exception {
		expectSuccess("evt := EVENT {evt};", "1 WEEK AFTER TIME OF evt;", "RETURN EVENTTIME, TRIGGERTIME;",
				"WHEN NOW IS 2001-01-01;"
			  +	"AND {evt} IS CALLED;"
			  +	"AND NOW IS 2001-02-10;"
			  + "THEN THE MLM SHOULD BE TRIGGERED;"
			  + "AND 2001-01-01, 2001-01-08 SHOULD BE RETURNED;");
	}
}

package arden.tests.implementation;

import org.junit.Test;

import arden.compiler.CompiledMlm;
import arden.compiler.CompilerException;

public class ValidationSyntaxTest extends ImplementationTest {

	private static CompiledMlm parseTemplate(String scenarioCode) throws CompilerException {
		return ValidationTest.parseTemplate("", "", "", "test scenario", scenarioCode);
	}

	@Test
	public void testFullScenario() throws Exception {
		parseTemplate("GIVEN {x} IS 5; WHEN MLM IS CALLED; THEN 5 SHOULD BE RETURNED;");
	}

	@Test
	public void testEmptyScenario() throws Exception {
		parseTemplate("");
	}

	@Test
	public void testGiven() throws Exception {
		parseTemplate("GIVEN {asdf} IS 5;");
		parseTemplate("GIVEN {asdf} IS 5");
		parseTemplate("GIVEN READ {asdf} IS 5;");
		parseTemplate("GIVEN {XYZ} IS 1, TRUE, (\"asdf\", 123);");
	}

	@Test
	public void testGivenInterface() throws Exception {
		parseTemplate("GIVEN INTERFACE {asdf} IS 5;");
	}

	@Test
	public void testWhenCallMlm() throws Exception {
		parseTemplate("WHEN MLM IS CALLED;");
		parseTemplate("WHEN MLM IS CALLED WITH \"x\"|2011-07-03, FALSE|2010-06-10;");
	}

	@Test
	public void testWhenCallEvent() throws Exception {
		parseTemplate("WHEN {asdf} IS CALLED;");
		parseTemplate("WHEN EVENT {asdf} IS CALLED;");
		parseTemplate("WHEN {asdf} IS CALLED WITH TIME 2011-07-03;");
		parseTemplate("WHEN {asdf} IS CALLED WITH TIME 2011-07-03 EVENTTIME 2010-06-10;");
	}

	@Test
	public void testWhenSetTime() throws Exception {
		parseTemplate("WHEN CURRENTTIME IS 1986-04-28T00:00:00;");
		parseTemplate("WHEN NOW IS 1986-04-28;");
	}

	@Test
	public void testThenTriggered() throws Exception {
		parseTemplate("THEN MLM SHOULD BE TRIGGERED;");
		parseTemplate("THEN THE MLM SHOULD BE TRIGGERED;");
	}

	@Test
	public void testThenReturn() throws Exception {
		parseTemplate("THEN 5 SHOULD BE RETURNED;");
		parseTemplate("THEN 1970-01-01 + 1 WEEK SHOULD BE RETURNED;");
		// parseTemplate("THEN (5|NULL|TRUTH VALUE 0.3) SHOULD BE RETURNED;");
		parseTemplate("THEN 5, (\"asdf\", 123) SHOULD BE RETURNED;");
		// parseTemplate("THEN LOCALIZED ’msg’ SHOULD BE RETURNED;");
	}

	@Test
	public void testThenWrite() throws Exception {
		parseTemplate("THEN \"asdf\" SHOULD BE WRITTEN;");
		parseTemplate("THEN \"asdf\" SHOULD BE WRITTEN AT {dest};");
		// parseTemplate("THEN LOCALIZED 'msg' SHOULD BE WRITTEN;");
		parseTemplate("THEN {msg mapping} SHOULD BE WRITTEN;");
		parseTemplate("THEN {msg mapping} SHOULD BE WRITTEN AT {dest mapping};");
		parseTemplate("THEN MESSAGE {msg mapping} SHOULD BE WRITTEN AT DESTINATION {dest mapping};");
	}

	@Test
	public void testThenCall() throws Exception {
		parseTemplate("THEN MLM 'other_mlm' SHOULD BE CALLED;");
		parseTemplate("THEN MLM 'other_mlm' SHOULD BE CALLED WITH \"x\", \"y\";");
		parseTemplate("THEN MLM 'other_mlm' SHOULD BE CALLED WITH \"x\", \"y\" DELAY 5 SECONDS;");
	}

	@Test
	public void testThenIndeterminate() throws Exception {
		parseTemplate("THEN NOTHING SHOULD BE RETURNED;");
		parseTemplate("THEN NOTHING SHOULD BE WRITTEN;");
		parseTemplate("THEN NOTHING SHOULD BE CALLED;");
		parseTemplate("THEN ANY VALUE SHOULD BE RETURNED;");
		parseTemplate("THEN ANY VALUE SHOULD BE WRITTEN;");
		// parseTemplate("THEN ANYTHING SHOULD BE CALLED;");
		parseTemplate("THEN ANY VALUE, ANY VALUE, ANY VALUE SHOULD BE RETURNED;");
		parseTemplate("THEN ANY VALUE, 5, (1,2,3) SHOULD BE RETURNED;");
	}

	@Test
	public void testThenFilter() throws Exception {
		parseTemplate("THEN ANY VALUE WHERE IT IS WITHIN 3 TO 5 SHOULD BE RETURNED;");
		parseTemplate("THEN ANY VALUE WHERE IT MATCHES PATTERN \"%pulmonary%\" SHOULD BE RETURNED;");
		parseTemplate("THEN 123, ANY VALUE WHERE IT IS WITHIN 3 TO 5, \"a string\" SHOULD BE RETURNED;");
	}

	@Test
	public void testThenConclude() throws Exception {
		parseTemplate("THEN FALSE SHOULD BE CONCLUDED;");
	}

	@Test
	public void testAnd() throws Exception {
		parseTemplate("GIVEN {x} IS 3; "
				    + "AND {y} IS 5; "
				    + "AND {z} IS 3; "
				    + "WHEN NOW IS 1970-01-01; "
				    + "AND MLM IS CALLED; "
				    + "THEN 5 SHOULD BE RETURNED; "
				    + "AND \"ASDF\" SHOULD BE WRITTEN; "
				    + "AND MLM 'xyz' SHOULD BE CALLED;");
	}

	@Test
	public void testMixedAnd() throws Exception {
		parseTemplate("GIVEN {x} IS \"x\"; "
					+ "AND {y} IS \"y\"; "
				    + "GIVEN {z} IS \"z\"; "
					+ "AND {v} IS 2; "
				    + "THEN 5 SHOULD BE RETURNED; "
					+ "AND \"x\" SHOULD BE WRITTEN; "
				    + "THEN MLM 'xyz' SHOULD BE CALLED;");
	}

	@Test
	public void testSkip() throws Exception {
		parseTemplate("WHEN THE MLM IS CALLED; THEN 5 SHOULD BE RETURNED;");
	}

	@Test
	public void testRepeat() throws Exception {
		parseTemplate("GIVEN {y} IS FALSE; "
	                + "WHEN THE MLM IS CALLED; "
				    + "THEN THE MLM SHOULD BE TRIGGERED; "
				    + "GIVEN {y} IS TRUE; "
				    + "WHEN MLM IS CALLED; "
				    + "THEN MLM SHOULD NOT BE TRIGGERED; ");
	}

	@Test
	public void testOptionalSemicolon() throws Exception {
		parseTemplate("GIVEN {x} IS TRUE");
		parseTemplate("WHEN MLM IS CALLED; THEN 5 SHOULD BE RETURNED");
		parseTemplate("WHEN MLM IS CALLED; THEN 5 SHOULD BE RETURNED; AND {msg} SHOULD BE WRITTEN");
	}

	@Test
	public void testPrimaryTimeConstruction() throws Exception {
		parseTemplate("GIVEN {x} IS 123|1970-01-01");
		parseTemplate("GIVEN {x} IS \"xyz\"|1970-01-01, 5");
	}

}

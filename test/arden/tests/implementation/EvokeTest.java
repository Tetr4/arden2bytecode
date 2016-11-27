// arden2bytecode
// Copyright (c) 2010, Daniel Grunwald
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice, this list
//   of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright notice, this list
//   of conditions and the following disclaimer in the documentation and/or other materials
//   provided with the distribution.
//
// - Neither the name of the owner nor the names of its contributors may be used to
//   endorse or promote products derived from this software without specific prior written
//   permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS &AS IS& AND ANY EXPRESS
// OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
// IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package arden.tests.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import arden.compiler.CompiledMlm;
import arden.compiler.Compiler;
import arden.compiler.CompilerException;
import arden.runtime.ArdenEvent;
import arden.runtime.MedicalLogicModule;
import arden.runtime.evoke.AfterTrigger;
import arden.runtime.evoke.CyclicTrigger;
import arden.runtime.evoke.Trigger;

public class EvokeTest extends ImplementationTest {

	private static CompiledMlm parseTemplate(String dataCode, String evokeCode, String logicCode, String actionCode)
			throws CompilerException {
		try {
			InputStream s = EvokeTest.class.getResourceAsStream("EvokeTemplate.mlm");
			String fullCode = inputStreamToString(s)
					.replace("$ACTION", actionCode)
					.replace("$DATA", dataCode)
					.replace("$EVOKE", evokeCode)
					.replace("$LOGIC", logicCode);
			Compiler c = new Compiler();
			return c.compileMlm(new StringReader(fullCode));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static CompiledMlm parseEvoke(String evokeCode) throws CompilerException {
		return parseEvoke("", evokeCode);
	}

	private static CompiledMlm parseEvoke(String data, String evokeCode) throws CompilerException {
		return parseEvoke(data, evokeCode, "");
	}

	private static CompiledMlm parseEvoke(String data, String evokeCode, String actionCode) throws CompilerException {
		return parseTemplate(data, evokeCode, "conclude true;", actionCode);
	}

	@Test
	public void testAfterFixedDateOperator() throws Exception {
		TestContext context = new TestContext();

		MedicalLogicModule mlm = parseEvoke("3 days after 1992-01-01T00:00:00");

		Trigger trigger = mlm.getTriggers(context)[0];
		Assert.assertEquals(createDate(1992, 0, 4), trigger.getNextRunTime());
		Assert.assertNull(trigger.getTriggeringEvent());
	}

	@Test
	public void testEventVariable() throws Exception {
		TestContext context = new TestContext();

		MedicalLogicModule mlm = parseEvoke("penicillin_storage := EVENT{penicillin storage}", "penicillin_storage");
		Trigger trigger = mlm.getTriggers(context)[0];

		ArdenEvent event = new ArdenEvent("penicillin storage");
		Assert.assertTrue(trigger.runOnEvent(event));
		trigger.scheduleEvent(event);
		Assert.assertEquals(event, trigger.getTriggeringEvent());
	}

	@Test
	public void testAfterTimeOfEventOperator() throws Exception {
		TestContext context = new TestContext();

		CompiledMlm mlm = parseEvoke("event1 := EVENT{penicillin storage}", "3 days after time of event1");
		Trigger trigger = mlm.getTriggers(context)[0];
		ArdenEvent event = new ArdenEvent("penicillin storage", createDate(1992, 0, 1).value);

		Assert.assertFalse(trigger.runOnEvent(event));
		trigger.scheduleEvent(event);
		Assert.assertEquals(event, trigger.getTriggeringEvent());

		Assert.assertEquals(createDate(1992, 0, 4), trigger.getNextRunTime());
	}

	@Test
	public void testAfterAfterTimeOfEvent() throws Exception {
		TestContext context = new TestContext();

		CompiledMlm mlm = parseEvoke("event1 := EVENT{penicillin storage}", "3 days after 2 days after time of event1");
		Trigger trigger = mlm.getTriggers(context)[0];
		ArdenEvent event = new ArdenEvent("penicillin storage", createDate(1992, 0, 1).value);

		Assert.assertFalse(trigger.runOnEvent(event));
		trigger.scheduleEvent(event);
		Assert.assertEquals(event, trigger.getTriggeringEvent());

		Assert.assertEquals(createDate(1992, 0, 6), trigger.getNextRunTime());
		Assert.assertEquals(1000 * 60 * 60 * 24 * 5, trigger.getDelay());
	}

	@Test
	public void testFixedDate() throws Exception {
		TestContext context = new TestContext();

		MedicalLogicModule mlm = parseEvoke("1992-03-04");
		Trigger trigger = mlm.getTriggers(context)[0];

		Assert.assertEquals(createDate(1992, 2, 4), trigger.getNextRunTime());
	}

	@Test
	public void testFixedDateTime() throws Exception {
		TestContext context = new TestContext();

		MedicalLogicModule mlm = parseEvoke("1992-01-03T14:23:17.0");
		Trigger trigger = mlm.getTriggers(context)[0];

		Assert.assertEquals(createDateTime(1992, 0, 3, 14, 23, 17), trigger.getNextRunTime());

		Assert.assertNull(trigger.getTriggeringEvent());
	}

	@Test
	public void testOrOperator() throws Exception {
		TestContext context = new TestContext();

		MedicalLogicModule mlm = parseEvoke(
				"penicillin_storage := EVENT{penicillin storage};"
						+ "cephalosporin_storage := EVENT{cephalosporin storage};"
						+ "aminoglycoside_storage := EVENT{aminoglycoside storage};",
				"penicillin_storage OR cephalosporin_storage OR aminoglycoside_storage");
		Trigger trigger = mlm.getTriggers(context)[0];

		Assert.assertFalse(trigger.runOnEvent(new ArdenEvent("other storage")));
		Assert.assertTrue(trigger.runOnEvent(new ArdenEvent("aminoglycoside storage")));
		Assert.assertTrue(trigger.runOnEvent(new ArdenEvent("cephalosporin storage")));
		Assert.assertTrue(trigger.runOnEvent(new ArdenEvent("aminoglycoside storage")));

		ArdenEvent event = new ArdenEvent("aminoglycoside storage");
		trigger.scheduleEvent(event);
		Assert.assertEquals(event, trigger.getTriggeringEvent());
	}

	@Test
	public void testAnyOperator() throws Exception {
		TestContext context = new TestContext();

		MedicalLogicModule mlm = parseEvoke(
				"penicillin_storage := EVENT{penicillin storage};"
						+ "cephalosporin_storage := EVENT{cephalosporin storage};"
						+ "aminoglycoside_storage := EVENT{aminoglycoside storage};",
				"ANY OF (penicillin_storage, cephalosporin_storage, aminoglycoside_storage)");
		Trigger trigger = mlm.getTriggers(context)[0];

		Assert.assertFalse(trigger.runOnEvent(new ArdenEvent("other storage")));
		Assert.assertTrue(trigger.runOnEvent(new ArdenEvent("aminoglycoside storage")));
		Assert.assertTrue(trigger.runOnEvent(new ArdenEvent("cephalosporin storage")));
		Assert.assertTrue(trigger.runOnEvent(new ArdenEvent("aminoglycoside storage")));

		ArdenEvent event = new ArdenEvent("cephalosporin storage");
		trigger.scheduleEvent(event);
		Assert.assertEquals(event, trigger.getTriggeringEvent());
	}

	@Test
	public void testAnyOperator2() throws Exception {
		TestContext context = new TestContext();

		MedicalLogicModule mlm = parseEvoke(
				"cephalosporin_storage := EVENT{cephalosporin storage};"
						+ "aminoglycoside_storage := EVENT{aminoglycoside storage};",
				"ANY OF (cephalosporin_storage, aminoglycoside_storage)");
		Trigger trigger = mlm.getTriggers(context)[0];

		Assert.assertFalse(trigger.runOnEvent(new ArdenEvent("other storage")));
		Assert.assertTrue(trigger.runOnEvent(new ArdenEvent("cephalosporin storage")));
		Assert.assertTrue(trigger.runOnEvent(new ArdenEvent("aminoglycoside storage")));

		ArdenEvent event = new ArdenEvent("aminoglycoside storage");
		trigger.scheduleEvent(event);
		Assert.assertEquals(event, trigger.getTriggeringEvent());
	}

	@Test
	public void testAfterTimeOfEventOperator2() throws Exception {
		TestContext context = new TestContext(createDate(1990, 0, 1));

		CompiledMlm mlm = parseEvoke("event1 := EVENT{test}", "3 days after time of event1");
		Trigger trigger = mlm.getTriggers(context)[0];

		Assert.assertTrue(trigger instanceof AfterTrigger);
		Assert.assertEquals(null, trigger.getNextRunTime());

		ArdenEvent event = new ArdenEvent("test", context.getCurrentTime().value);
		Assert.assertFalse(trigger.runOnEvent(event));

		trigger.scheduleEvent(event);
		Assert.assertFalse(trigger.runOnEvent(event));
		Assert.assertEquals(event, trigger.getTriggeringEvent());
		Assert.assertEquals(1000 * 60 * 60 * 24 * 3, trigger.getDelay());
		Assert.assertEquals(createDate(1990, 0, 4), trigger.getNextRunTime());
	}

	@Test
	public void testCyclicEvent() throws Exception {
		TestContext context = new TestContext(createDate(1990, 0, 1));

		CompiledMlm mlm = parseEvoke("every 5 days for 10 years starting 5 days after 1992-03-04");
		Trigger trigger = mlm.getTriggers(context)[0];
		Assert.assertTrue(trigger instanceof CyclicTrigger);

		Assert.assertEquals(createDate(1992, 2, 9), trigger.getNextRunTime());
		context.setCurrentTime(createDate(1992, 2, 10));
		Assert.assertEquals(createDate(1992, 2, 14), trigger.getNextRunTime());
	}

	@Test
	public void testCyclicEventDuration() throws Exception {
		TestContext context = new TestContext(createDate(1990, 0, 1));

		CompiledMlm mlm = parseEvoke("event1 := EVENT{test}",
				"every 1 day for 2 days starting 3 days after time of event1");
		Trigger trigger = mlm.getTriggers(context)[0];
		ArdenEvent event = new ArdenEvent("test", context.getCurrentTime().value);

		trigger.scheduleEvent(event);
		Assert.assertEquals(createDate(1990, 0, 4), trigger.getNextRunTime());
		Assert.assertEquals(event, trigger.getTriggeringEvent());
		Assert.assertEquals(1000 * 60 * 60 * 24 * 3, trigger.getDelay());

		context.setCurrentTime(createDateTime(1990, 0, 4, 0, 0, 1));
		Assert.assertEquals(createDate(1990, 0, 5), trigger.getNextRunTime());
		Assert.assertEquals(event, trigger.getTriggeringEvent());
		Assert.assertEquals(1000 * 60 * 60 * 24 * 4, trigger.getDelay());

		context.setCurrentTime(createDateTime(1990, 0, 5, 0, 0, 1));
		Assert.assertEquals(createDate(1990, 0, 6), trigger.getNextRunTime());
		Assert.assertEquals(event, trigger.getTriggeringEvent());
		Assert.assertEquals(1000 * 60 * 60 * 24 * 5, trigger.getDelay());

		context.setCurrentTime(createDateTime(1990, 0, 6, 0, 0, 1));
		Assert.assertNull(trigger.getNextRunTime());

		trigger.scheduleEvent(event);
	}

	@Test
	public void testCyclicAnyEvents() throws Exception {
		TestContext context = new TestContext(createDate(1990, 0, 1));
		MedicalLogicModule mlm = parseEvoke(
						"event1 := EVENT{test 1};"
						+ "event2 := EVENT{test 2};"
						+ "event3 := EVENT{test 3};",
				"every 5 days for 10 days starting time of any of (event1, event2, event3)");
		Trigger trigger = mlm.getTriggers(context)[0];
		Assert.assertTrue(trigger instanceof CyclicTrigger);
		
		ArdenEvent event = new ArdenEvent("test 2", context.getCurrentTime().value);
		trigger.scheduleEvent(event);

		Assert.assertEquals(createDate(1990, 0, 1), trigger.getNextRunTime());
		Assert.assertEquals(createDate(1990, 0, 6), trigger.getNextRunTime());
		Assert.assertEquals(createDate(1990, 0, 11), trigger.getNextRunTime());
		Assert.assertNull(trigger.getNextRunTime());
	}
}

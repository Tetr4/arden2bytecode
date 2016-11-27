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

import org.junit.Assert;
import org.junit.Test;

import arden.runtime.ArdenDuration;
import arden.runtime.ArdenEvent;
import arden.runtime.ArdenList;
import arden.runtime.ArdenNull;
import arden.runtime.ArdenString;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.DatabaseQuery;
import arden.runtime.MedicalLogicModule;
import arden.runtime.MemoryQuery;
import arden.runtime.evoke.CyclicTrigger;
import arden.runtime.evoke.EventTrigger;
import arden.runtime.evoke.Trigger;

public class ExampleEvokeTest extends ImplementationTest {

	@Test
	public void x31() throws Exception {
		MedicalLogicModule mlm = compile("x3.1.mlm");

		TestContext context = new TestContext();
		Trigger trigger = mlm.getTriggers(context)[0];

		Assert.assertTrue(trigger instanceof EventTrigger);
		ArdenEvent event = new ArdenEvent("storage of urine electrolytes", context.getCurrentTime().value);
		Assert.assertTrue(trigger.runOnEvent(event));
	}

	@Test
	public void x32() throws Exception {
		MedicalLogicModule mlm = compile("x3.2.mlm");

		TestContext context = new TestContext();
		Trigger trigger = mlm.getTriggers(context)[0];

		Assert.assertTrue(trigger instanceof EventTrigger);
		ArdenEvent event = new ArdenEvent("'06210519','06210669'", context.getCurrentTime().value);
		Assert.assertTrue(trigger.runOnEvent(event));
	}

	@Test
	public void x33noAllergies() throws Exception {
		MedicalLogicModule mlm = compile("x3.3.mlm");
		TestContext context = new TestContext();
		Trigger trigger = mlm.getTriggers(context)[0];
		
		Assert.assertTrue(trigger instanceof EventTrigger);
		ArdenEvent event = new ArdenEvent("medication_order where class = penicillin", context.getCurrentTime().value);
		Assert.assertTrue(trigger.runOnEvent(event));
	}

	@Test
	public void x33allergies() throws Exception {
		MedicalLogicModule mlm = compile("x3.3.mlm");
		TestContext context = new TestContext() {
			@Override
			public DatabaseQuery createQuery(String mapping) {
				Assert.assertEquals("allergy where agent_class = penicillin", mapping);
				ArdenList list = new ArdenList(new ArdenValue[] { new ArdenString("all1"), new ArdenString("all2") });
				return new MemoryQuery(new ArdenValue[] { list });
			}
		};
		Trigger trigger = mlm.getTriggers(context)[0];
		
		Assert.assertTrue(trigger instanceof EventTrigger);
		ArdenEvent event = new ArdenEvent("medication_order where class = penicillin", context.getCurrentTime().value);
		Assert.assertTrue(trigger.runOnEvent(event));
	}

	@Test
	public void x33allergiesButLastIsNull() throws Exception {
		MedicalLogicModule mlm = compile("x3.3.mlm");
		TestContext context = new TestContext() {
			@Override
			public DatabaseQuery createQuery(String mapping) {
				Assert.assertEquals("allergy where agent_class = penicillin", mapping);
				ArdenList list = new ArdenList(new ArdenValue[] { new ArdenString("all1"), ArdenNull.INSTANCE });
				return new MemoryQuery(new ArdenValue[] { list });
			}
		};
		Trigger trigger = mlm.getTriggers(context)[0];
		
		Assert.assertTrue(trigger instanceof EventTrigger);
		ArdenEvent event = new ArdenEvent("medication_order where class = penicillin", context.getCurrentTime().value);
		Assert.assertTrue(trigger.runOnEvent(event));
	}

	@Test
	public void x34() throws Exception {
		MedicalLogicModule mlm = compile("x3.4.mlm");

		TestContext context = new TestContext();
		Trigger trigger = mlm.getTriggers(context)[0];

		Assert.assertTrue(trigger instanceof EventTrigger);
		ArdenEvent event = new ArdenEvent("medication_order where class = gentamicin", context.getCurrentTime().value);
		Assert.assertTrue(trigger.runOnEvent(event));
	}

	@Test
	public void x35() throws Exception {
		MedicalLogicModule mlm = compile("x3.5.mlm");
		ArdenTime defaultTime = createDateTime(1980, 0, 1, 0, 0, 0); // this is the default myExecutionContext.getCurrentTime()
		ArdenTime defaultEventDate = createDateTime(2000, 0, 1, 0, 0, 0); // this is the default myExecutionContext.getEvent()
		ArdenEvent defaultEvent = new ArdenEvent("gentamicin_order", defaultEventDate.value);

		TestContext context = new TestContext(defaultEvent, defaultTime);
		Trigger trigger = mlm.getTriggers(context)[0];

		Assert.assertTrue(trigger instanceof CyclicTrigger);
		
		trigger.scheduleEvent(defaultEvent);
		ArdenDuration fiveDays = (ArdenDuration)ArdenDuration.create(
				60.0 * 60 * 24 * 5, 
				false, 
				context.getCurrentTime().value); 
		ArdenTime fiveDaysLater = new ArdenTime(
				defaultEventDate.add(fiveDays)); // add 5 days as in x3.5.mlm
		// default runtime should be 5 days after the event as declared in the MLM:
		Assert.assertEquals(
				fiveDaysLater, 
				trigger.getNextRunTime());

		ArdenTime tenDaysLater = new ArdenTime(fiveDaysLater.add(fiveDays));
		ArdenDuration oneSecond = (ArdenDuration)ArdenDuration.create(1, false, context.getCurrentTime().value);
		
		context.setCurrentTime(new ArdenTime(fiveDaysLater.add(oneSecond)));
		// after the first runtime has been passed, the mlm should be re-run another 5 days later:
		Assert.assertEquals(
				tenDaysLater,
				trigger.getNextRunTime());
	}

	@Test
	public void x36() throws Exception {
		MedicalLogicModule mlm = compile("x3.6.mlm");

		TestContext context = new TestContext();
		Trigger trigger = mlm.getTriggers(context)[0];

		Assert.assertTrue(trigger instanceof EventTrigger);
		ArdenEvent event = new ArdenEvent("STORAGE OF ABSOLUTE_NEUTROPHILE_COUNT", context.getCurrentTime().value);
		Assert.assertTrue(trigger.runOnEvent(event));
	}

	@Test
	public void x37() throws Exception {
		MedicalLogicModule mlm = compile("x3.7.mlm");

		TestContext context = new TestContext();
		Trigger[] triggers = mlm.getTriggers(context);

		Assert.assertTrue(triggers.length == 0);
	}

	@Test
	public void x38() throws Exception {
		MedicalLogicModule mlm = compile("x3.8.mlm");
		TestContext context = new TestContext();
		Trigger[] triggers = mlm.getTriggers(context);
		Assert.assertTrue(triggers.length == 0);
	}
}

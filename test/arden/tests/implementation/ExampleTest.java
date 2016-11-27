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

import arden.runtime.ArdenList;
import arden.runtime.ArdenNull;
import arden.runtime.ArdenString;
import arden.runtime.ArdenValue;
import arden.runtime.DatabaseQuery;
import arden.runtime.MedicalLogicModule;
import arden.runtime.MedicalLogicModuleImplementation;
import arden.runtime.MemoryQuery;
import arden.runtime.evoke.CallTrigger;

public class ExampleTest extends ImplementationTest {
	
	@Test
	public void x31() throws Exception {
		MedicalLogicModule mlm = compile("x3.1.mlm");

		TestContext context = new TestContext();
		mlm.run(context, null, new CallTrigger());

		Assert.assertEquals("", context.getOutputText());
	}

	@Test
	public void x32() throws Exception {
		MedicalLogicModule mlm = compile("x3.2.mlm");

		TestContext context = new TestContext();
		mlm.run(context, null, new CallTrigger());

		Assert.assertEquals("", context.getOutputText());
	}

	@Test
	public void x33noAllergies() throws Exception {
		MedicalLogicModule mlm = compile("x3.3.mlm");
		TestContext context = new TestContext();
		mlm.run(context, null, new CallTrigger());
		Assert.assertEquals("", context.getOutputText());
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
		mlm.run(context, null, new CallTrigger());
		Assert.assertEquals("Caution, the patient has the following allergy to penicillin documented: all2\n", context
				.getOutputText());
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
		mlm.run(context, null, new CallTrigger());
		Assert.assertEquals("", context.getOutputText());
	}

	@Test
	public void x33urgency() throws Exception {
		MedicalLogicModule mlm = compile("x3.3.mlm");
		MedicalLogicModuleImplementation instance = mlm.createInstance(new TestContext(), null, new CallTrigger());
		Assert.assertEquals(51.0, instance.getUrgency(), 0);
	}

	@Test
	public void x34() throws Exception {
		MedicalLogicModule mlm = compile("x3.4.mlm");

		TestContext context = new TestContext();
		mlm.run(context, null, new CallTrigger());

		Assert.assertEquals("", context.getOutputText());
	}

	@Test
	public void x35() throws Exception {
		MedicalLogicModule mlm = compile("x3.5.mlm");

		TestContext context = new TestContext();
		mlm.run(context, null, new CallTrigger());

		Assert.assertEquals(
				"Suggest obtaining a serum creatinine to follow up on renal function in the setting of gentamicin.\n",
				context.getOutputText());
	}

	@Test
	public void x36() throws Exception {
		MedicalLogicModule mlm = compile("x3.6.mlm");

		TestContext context = new TestContext();
		mlm.run(context, null, new CallTrigger());

		Assert.assertEquals("", context.getOutputText());
	}

	@Test
	public void x37() throws Exception {
		MedicalLogicModule mlm = compile("x3.7.mlm");

		TestContext context = new TestContext();
		mlm.run(context, null, new CallTrigger());

		Assert.assertEquals("", context.getOutputText());
	}

	@Test
	public void x38() throws Exception {
		MedicalLogicModule mlm = compile("x3.8.mlm");

		TestContext context = new TestContext();
		ArdenList medOrders = new ArdenList(new ArdenValue[] { new ArdenString("order1"), new ArdenString("order2"),
				new ArdenString("order3") });
		ArdenList medAllergens = new ArdenList(new ArdenValue[] { new ArdenString("a1"), new ArdenString("a2"),
				new ArdenString("a3") });
		ArdenList patientAllergies = new ArdenList(new ArdenValue[] { new ArdenString("a2"), new ArdenString("a2"),
				new ArdenString("a1") });
		ArdenList patientReactions = new ArdenList(new ArdenValue[] { new ArdenString("r1"), new ArdenString("r2"),
				new ArdenString("r3") });
		ArdenValue[] result = mlm.run(context, new ArdenValue[] { medOrders, medAllergens, patientAllergies,
				patientReactions }, new CallTrigger());
		Assert.assertEquals(3, result.length);

		Assert.assertEquals("(\"order1\",\"order2\")", result[0].toString());
		Assert.assertEquals("(\"a1\",\"a2\")", result[1].toString());
		Assert.assertEquals("(\"r3\",\"r1\",\"r2\")", result[2].toString());

		Assert.assertEquals("", context.getOutputText());
	}
}

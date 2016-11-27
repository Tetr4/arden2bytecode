package arden.tests.implementation;

import org.junit.Assert;
import org.junit.Test;

import arden.runtime.ArdenNumber;
import arden.runtime.ArdenValue;
import arden.runtime.MedicalLogicModule;
import arden.runtime.evoke.CallTrigger;

public class GetValueTest extends ImplementationTest {

	@Test
	public void x37() throws Exception {
		MedicalLogicModule mlm = compile("x3.7.mlm");

		// mlm has not been not run yet:
		Assert.assertNull(mlm.getValue("low_dose_beta_use"));
		
		TestContext context = new TestContext();
		mlm.run(context, null, new CallTrigger());

		Assert.assertTrue(mlm.getValue("low_dose_beta_use").isFalse());
		Assert.assertNull(mlm.getValue("does_not_exist"));
	}
	
	@Test
	public void x38() throws Exception {
		MedicalLogicModule mlm = compile("x3.8.mlm");

		// mlm has not been not run yet:
		Assert.assertNull(mlm.getValue("num"));
		
		TestContext context = new TestContext();
		mlm.run(context, null, new CallTrigger());
		
		Assert.assertEquals(ArdenNumber.create(2.0, ArdenValue.NOPRIMARYTIME), mlm.getValue("num"));
		Assert.assertNull(mlm.getValue("does_not_exist"));
	}
}

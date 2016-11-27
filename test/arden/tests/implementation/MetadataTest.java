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

import arden.runtime.ExecutionContext;
import arden.runtime.LibraryMetadata;
import arden.runtime.MaintenanceMetadata;
import arden.runtime.MaintenanceMetadata.ArdenVersion;
import arden.runtime.MaintenanceMetadata.Validation;
import arden.runtime.evoke.CallTrigger;
import arden.runtime.MedicalLogicModule;
import arden.runtime.MedicalLogicModuleImplementation;

public class MetadataTest extends ImplementationTest {
	@Test
	public void x31() throws Exception {
		MedicalLogicModule mlm = compile("x3.1.mlm");

		MaintenanceMetadata m = mlm.getMaintenance();
		Assert.assertEquals("Fractional excretion of sodium", m.getTitle());
		Assert.assertEquals("fractional_na", mlm.getName());
		Assert.assertEquals(ArdenVersion.V2, m.getArdenVersion());
		Assert.assertEquals("1.00", m.getVersion());
		Assert.assertEquals("Columbia-Presbyterian Medical Center", m.getInstitution());
		Assert.assertEquals("George Hripcsak, M.D.(hripcsak@cucis.cis.columbia.edu)", m.getAuthor());
		Assert.assertNull(m.getSpecialist());
		Assert.assertEquals(Validation.TESTING, m.getValidation());

		Assert.assertEquals(3, mlm.getLibrary().getKeywords().size());
		Assert.assertEquals("fractional excretion", mlm.getLibrary().getKeywords().get(0));
		Assert.assertEquals("serum sodium", mlm.getLibrary().getKeywords().get(1));
		Assert.assertEquals("azotemia", mlm.getLibrary().getKeywords().get(2));

		Assert.assertEquals(50, mlm.getPriority(), 0);
	}
	
	@Test
	public void x33() throws Exception {
		MedicalLogicModule mlm = compile("x3.3.mlm");

		MaintenanceMetadata m = mlm.getMaintenance();
		Assert.assertEquals("Check for penicillin allergy", m.getTitle());
		Assert.assertEquals("pen_allergy", mlm.getName());
		Assert.assertEquals(ArdenVersion.V1, m.getArdenVersion());
		Assert.assertEquals("1.00", m.getVersion());
		Assert.assertEquals("Columbia-Presbyterian Medical Center", m.getInstitution());
		Assert.assertEquals("George Hripcsak, M.D.", m.getAuthor());
		Assert.assertNull(m.getSpecialist());
		Assert.assertEquals(Validation.TESTING, m.getValidation());

		Assert.assertEquals(2, mlm.getLibrary().getKeywords().size());
		Assert.assertEquals("penicillin", mlm.getLibrary().getKeywords().get(0));
		Assert.assertEquals("allergy", mlm.getLibrary().getKeywords().get(1));

		Assert.assertEquals(42, mlm.getPriority(), 0);
	}
	
	@Test
	public void x31FromByteCode() throws Exception {
		MedicalLogicModule mlm = compile("x3.1.mlm");
		
		ExecutionContext cx = new TestContext();
		
		MedicalLogicModuleImplementation impl = mlm.createInstance(cx, null, new CallTrigger());

		MaintenanceMetadata m = impl.getMaintenanceMetadata();
		Assert.assertEquals("Fractional excretion of sodium", m.getTitle());
		Assert.assertEquals("fractional_na", m.getMlmName());
		Assert.assertEquals(ArdenVersion.V2, m.getArdenVersion());
		Assert.assertEquals("1.00", m.getVersion());
		Assert.assertEquals("Columbia-Presbyterian Medical Center", m.getInstitution());
		Assert.assertEquals("George Hripcsak, M.D.(hripcsak@cucis.cis.columbia.edu)", m.getAuthor());
		Assert.assertNull(m.getSpecialist());
		Assert.assertEquals(Validation.TESTING, m.getValidation());

		LibraryMetadata l = impl.getLibraryMetadata();
		Assert.assertEquals(3, l.getKeywords().size());
		Assert.assertEquals("fractional excretion", l.getKeywords().get(0));
		Assert.assertEquals("serum sodium", l.getKeywords().get(1));
		Assert.assertEquals("azotemia", l.getKeywords().get(2));
	}
	
	@Test
	public void x33FromByteCode() throws Exception {
		MedicalLogicModule mlm = compile("x3.3.mlm");

		ExecutionContext cx = new TestContext();
		
		MedicalLogicModuleImplementation impl = mlm.createInstance(cx, null, new CallTrigger());
		
		MaintenanceMetadata m = impl.getMaintenanceMetadata();
		Assert.assertEquals("Check for penicillin allergy", m.getTitle());
		Assert.assertEquals("pen_allergy", m.getMlmName());
		Assert.assertEquals(ArdenVersion.V1, m.getArdenVersion());
		Assert.assertEquals("1.00", m.getVersion());
		Assert.assertEquals("Columbia-Presbyterian Medical Center", m.getInstitution());
		Assert.assertEquals("George Hripcsak, M.D.", m.getAuthor());
		Assert.assertNull(m.getSpecialist());
		Assert.assertEquals(Validation.TESTING, m.getValidation());

		LibraryMetadata l = impl.getLibraryMetadata();
		Assert.assertEquals(2, l.getKeywords().size());
		Assert.assertEquals("penicillin", l.getKeywords().get(0));
		Assert.assertEquals("allergy", l.getKeywords().get(1));

		Assert.assertEquals(42, impl.getPriority(), 0);
	}
}

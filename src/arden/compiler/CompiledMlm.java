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

package arden.compiler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import arden.MainClass;
import arden.runtime.ArdenList;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.LibraryMetadata;
import arden.runtime.MaintenanceMetadata;
import arden.runtime.MedicalLogicModule;
import arden.runtime.MedicalLogicModuleImplementation;
import arden.runtime.evoke.CallTrigger;
import arden.runtime.evoke.Trigger;

/**
 * Represents a compiled MedicalLogicModule with minimal Metadata (as loaded from a .class File)
 * 
 * Allows saving the compiled bytecode into a .class file by calling the
 * saveClassFile() method.
 * 
 * Allows loading of MedicalLogicModuleImplementation .class file by 
 * calling loadClassFile().
 * 
 * When createInstance() or run() is called, the compiled bytecode is loaded
 * using the InMemoryClassLoader for execution.
 * 
 * @author Daniel Grunwald, Hannes Flicka
 * 
 */
public final class CompiledMlm implements MedicalLogicModule {
	private byte[] data;
	Class<? extends MedicalLogicModuleImplementation> clazz = null;
	private MedicalLogicModuleImplementation uninitializedInstance = null;	
	private MedicalLogicModuleImplementation initializedInstance = null;
	private Trigger[] triggers = null;
	private String mlmname;

	public CompiledMlm(byte[] data, String mlmname) {
		this.data = data;
		this.mlmname = mlmname;
	}
	
	public CompiledMlm(InputStream in, String mlmname) throws IOException {
		this((byte[]) null, mlmname);
		loadClassData(in, in.available());
		this.mlmname = getName();
	}
	
	public CompiledMlm(Class<? extends MedicalLogicModuleImplementation> clazz, String mlmname) throws IOException {
		this((byte[]) null, mlmname);
		this.clazz = clazz;
	}

	public CompiledMlm(File mlmfile, String mlmname) throws IOException {		
		this((byte[]) null, mlmname);
		if (this.mlmname == null) {
			this.mlmname = MainClass.getFilenameBase(mlmfile.getName());
		}
		loadClassFile(mlmfile);
		this.mlmname = getName();
	}

	public void saveClassFile(OutputStream os) throws IOException {
		os.write(data);
	}
	
	private void loadClassFile(File file) throws IOException {
		loadClassData(
				new BufferedInputStream(
						new FileInputStream(file)),
				(int)(file.length()));
	}
	
	private void loadClassData(InputStream in, int len) throws IOException {
		data = new byte[len];
		in.read(data, 0, len);
	}

	@SuppressWarnings("unchecked")
	private Class<? extends MedicalLogicModuleImplementation> loadClazz() {
		if (clazz == null) {
			try {
				ClassLoader classLoader = new InMemoryClassLoader(mlmname, data);
				clazz = (Class<? extends MedicalLogicModuleImplementation>) classLoader.loadClass(mlmname);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return clazz;
	}
	
	private synchronized Constructor<? extends MedicalLogicModuleImplementation> getConstructor() {
		Constructor<? extends MedicalLogicModuleImplementation> ctor = null;
		loadClazz();
		// We know the class has an appropriate constructor because we
		// compiled it, so wrap all the checked exceptions that should never
		// occur.
		try {
			ctor = clazz.getConstructor(ExecutionContext.class, MedicalLogicModule.class, ArdenValue[].class, Trigger.class);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		return ctor;
	}
	
	private synchronized Constructor<? extends MedicalLogicModuleImplementation> getParameterlessConstructor() {		
		Constructor<? extends MedicalLogicModuleImplementation> ctor = null;
		loadClazz();
		//loadClazz();
		// We know the class has an appropriate constructor because we
		// compiled it, so wrap all the checked exceptions that should never
		// occur.
		try {
			ctor = clazz.getConstructor();
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		return ctor;
	}

	/** Creates an instance of the implementation class. */
	@Override
	public MedicalLogicModuleImplementation createInstance(ExecutionContext context, ArdenValue[] arguments, Trigger evokingTrigger)
			throws InvocationTargetException {
		if (context == null)
			throw new NullPointerException();

		if (arguments == null)
			arguments = ArdenList.EMPTY.values;

		if (evokingTrigger == null)
			evokingTrigger = new CallTrigger();

		try {
			return getConstructor().newInstance(context, this, arguments, evokingTrigger);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Executes the MLM.
	 * 
	 * @return Returns the value(s) provided by the "return" statement, or
	 *         (Java) null if no return statement was executed.
	 */
	@Override
	public ArdenValue[] run(ExecutionContext context, ArdenValue[] arguments, Trigger evokingTrigger) throws InvocationTargetException {
		MedicalLogicModuleImplementation instance = createInstance(context, arguments, evokingTrigger);
		initializedInstance = instance;
		try {
			if (instance.logic(context))
				return instance.action(context);
			else
				return null;
		} catch (Exception ex) {
			throw new InvocationTargetException(ex);
		}
	}
	
	@Override
	public Result test(JUnitCore junit) {
		return junit.run(loadClazz());
	}
	
	/** use this method only to access static fields in the MLM implementation */
	private MedicalLogicModuleImplementation getNonInitializedInstance() {
		if (uninitializedInstance == null) {
			try {
				uninitializedInstance = getParameterlessConstructor().newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			} 
		}
		return uninitializedInstance;
	}
	
	@Override
	public MaintenanceMetadata getMaintenance() {
		return getNonInitializedInstance().getMaintenanceMetadata();
	}

	@Override
	public LibraryMetadata getLibrary() {
		return getNonInitializedInstance().getLibraryMetadata();
	}
	
	@Override
	public double getUrgency() {
		return getNonInitializedInstance().getUrgency();
	}

	@Override
	public String getName() {
		return getMaintenance().getMlmName();
	}

	@Override
	public double getPriority() {
		return getNonInitializedInstance().getPriority();
	}

	/**
	 * Gets a trigger telling when to run the MLM. As that trigger may depend on
	 * data set in the constructor, the data section of the MLM is run.
	 */
	@Override
	public Trigger[] getTriggers(ExecutionContext context) throws InvocationTargetException {
		if (triggers == null) {
			MedicalLogicModuleImplementation instance = initializedInstance;
			if (instance == null) {
				instance = createInstance(context, null, null);
			}
			triggers = instance.getTriggers(context);
		}
		return triggers;
	}

	public ArdenValue getValue(String name) {
		if (initializedInstance != null) {
			return initializedInstance.getValue(name);
		}
		return null;
	}
}

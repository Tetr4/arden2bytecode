package arden.runtime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import arden.CommandLineOptions;
import arden.MainClass;
import arden.compiler.CompiledMlm;
import arden.compiler.Compiler;
import arden.compiler.CompilerException;
import arden.engine.EvokeEngine;
import arden.runtime.MaintenanceMetadata.Validation;
import arden.runtime.evoke.CallTrigger;
import arden.runtime.evoke.Trigger;

/**
 * <p>
 * Implements the search strategy to find a matching MLM by name and institution
 * from a local searchpath (classpath + working directory).
 * </p>
 * <p>
 * Also allows calling other MLMs in the action slot (directly or via event). If
 * an {@link EvokeEngine} is not set, cyclic or delayed triggers are not run and
 * delayed calls are run immediately.
 * </p>
 */
public class BaseExecutionContext extends ExecutionContext {
	private List<URL> mlmSearchPath = new LinkedList<URL>();
	private Map<URL, MedicalLogicModule> initializedMlms = new HashMap<URL, MedicalLogicModule>();
	private EvokeEngine engine;

	public BaseExecutionContext(URL[] mlmSearchPath) {
		setURLs(mlmSearchPath);
	}

	public BaseExecutionContext(CommandLineOptions options) {
		try {
			// look for MLMs in classpath + working directory
			addURL(new File(".").toURI().toURL());
			if (options.isClasspath()) {
				String[] paths = options.getClasspath().split(File.pathSeparator);
				for (String path : paths) {
					URL url = new File(path).toURI().toURL();
					addURL(url);
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void addURL(URL url) {
		mlmSearchPath.add(url);
	}

	public void setURLs(URL[] urls) {
		mlmSearchPath.clear();
		if (urls != null) {
			mlmSearchPath.addAll(Arrays.asList(urls));
		}
	}

	@Override
	public MedicalLogicModule findModule(String name, String institution) {
		if (!name.matches("[a-zA-Z0-9\\-_]+")) {
			throw new RuntimeException("Malformed module name: " + name);
		}

		name = name.toLowerCase().trim();
		URLClassLoader loader = new URLClassLoader(mlmSearchPath.toArray(new URL[] {}));

		try {
			// look for matching .class files
			Enumeration<URL> mlmClassUrls = loader.getResources(name + ".class");
			while (mlmClassUrls.hasMoreElements()) {
				URL url = mlmClassUrls.nextElement();
				if (initializedMlms.containsKey(url)) {
					// already loaded
					continue;
				}

				InputStream in = url.openStream();
				if (in != null) {
					MedicalLogicModule module = new CompiledMlm(in, name);
					cacheModule(url, module);
				}
			}

			// look for matching .mlm files
			Enumeration<URL> mlmUrls = loader.getResources(name + MainClass.MLM_FILE_EXTENSION);
			while (mlmUrls.hasMoreElements()) {
				URL url = mlmUrls.nextElement();
				if (initializedMlms.containsKey(url)) {
					// already loaded
					continue;
				}

				InputStream in = url.openStream();

				Compiler compiler = new Compiler();
				MedicalLogicModule mlm;
				mlm = compiler.compile(new InputStreamReader(in, "UTF-8")).get(0);
				cacheModule(url, mlm);
			}
		} catch (CompilerException | IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				loader.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		MedicalLogicModule[] mlms = initializedMlms.values().toArray(new MedicalLogicModule[initializedMlms.size()]);
		MedicalLogicModule foundMlm = ExecutionContextHelpers.findModule(name, institution, mlms, null);
		if (foundMlm == null) {
			throw new RuntimeException("MLM not found");
		}
		return foundMlm;
	}

	@Override
	public MedicalLogicModule[] findModules(ArdenEvent event) {
		if (engine != null) {
			try {
				return engine.findModules(event);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Could not load MLMs for event" + event.name);
			}
		} else {
			// TODO How to find MLMs for an event without initializing every MLM in the classpath?
			return super.findModules(event);
		}
	}

	private void cacheModule(URL url, MedicalLogicModule mlm) {
		/*
		 * Reuse already initialized MLM (e.g. already loaded from .class file
		 * instead of .mlm file).
		 */
		for (MedicalLogicModule initializedMlm : initializedMlms.values()) {
			MaintenanceMetadata m1 = initializedMlm.getMaintenance();
			String m1Name = m1.getMlmName().toLowerCase().trim();
			String m1Institution = m1.getInstitution().toLowerCase().trim();
			Validation m1Validation = m1.getValidation();
			String m1Version = m1.getVersion();
			MaintenanceMetadata m2 = mlm.getMaintenance();
			String m2Name = m2.getMlmName().toLowerCase().trim();
			String m2Institution = m2.getInstitution().toLowerCase().trim();
			Validation m2Validation = m2.getValidation();
			String m2Version = m2.getVersion();
			if (m1Name.equals(m2Name) && m1Institution.equals(m2Institution) && m1Validation == m2Validation
					&& m1Version.equals(m2Version)) {
				initializedMlms.put(url, initializedMlm);
				return;
			}
		}
		initializedMlms.put(url, mlm);
	}

	public void setEngine(EvokeEngine engine) {
		this.engine = engine;
	}

	@Override
	public void call(ArdenRunnable mlm, ArdenValue[] arguments, ArdenValue delay, Trigger callerTrigger, double urgency) {
		long delayMillis = ExecutionContextHelpers.delayToMillis(delay);
		Trigger calleeTrigger = ExecutionContextHelpers.combine(callerTrigger, delayMillis);
		if (engine != null) {
			// run on engine
			engine.call(mlm, arguments, delayMillis, calleeTrigger, (int) urgency);
		} else {
			// print delay and run now
			System.out.println("delay (skipped): " + delay.toString());
			try {
				mlm.run(this, arguments, calleeTrigger);
			} catch (InvocationTargetException e) {
				System.err.println("Could not run MLM:");
				e.printStackTrace();
			}
		}
	}

	@Override
	public void call(ArdenEvent event, ArdenValue delay, double urgency) {
		long delayMillis = ExecutionContextHelpers.delayToMillis(delay);
		ArdenEvent eventAfterDelay = ExecutionContextHelpers.combine(event, delayMillis);
		if (engine != null) {
			// run on engine
			engine.call(eventAfterDelay, delayMillis, (int) urgency);
		} else {
			// run MLMs for event now
			ArdenRunnable[] mlms = findModules(eventAfterDelay);
			System.out.println("delay (skipped): " + delay.toString());
			System.out.println("event: " + eventAfterDelay.name);

			for (ArdenRunnable mlm : mlms) {
				try {
					mlm.run(this, null, new CallTrigger(eventAfterDelay, 0));
				} catch (InvocationTargetException e) {
					System.err.println("Could not run MLM:");
					e.printStackTrace();
				}
			}
		}
	}

}

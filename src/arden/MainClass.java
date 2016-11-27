// Arden2ByteCode
// Copyright (c) 2010, Daniel Grunwald, Hannes Flicka
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

package arden;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.Cli;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.HelpRequestedException;

import arden.compiler.CompiledMlm;
import arden.compiler.Compiler;
import arden.compiler.CompilerException;
import arden.constants.ConstantParser;
import arden.constants.ConstantParserException;
import arden.engine.EvokeEngine;
import arden.runtime.ArdenValue;
import arden.runtime.BaseExecutionContext;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;
import arden.runtime.StdIOExecutionContext;
import arden.runtime.evoke.CallTrigger;
import arden.runtime.jdbc.JDBCExecutionContext;

public class MainClass {
	public final static String MLM_FILE_EXTENSION = ".mlm";
	private final static String COMPILED_MLM_FILE_EXTENSION = ".class";
	private final static Pattern JAVA_CLASS_NAME = Pattern
			.compile("[A-Za-z$_][A-Za-z0-9$_]*(?:\\.[A-Za-z$_][A-Za-z0-9$_]*)*");

	// wrap and indent help message
	public static final int MAX_LINE_LENGTH = 80;
	private static final String ARGUMENTS_INDENT = "    ";
	private static final String DESCRIPTION_INDENT = "    ";

	private CommandLineOptions options;

	public static void main(String[] args) {
		boolean success = new MainClass().handleCommandLineArgs(args);
		if (!success) {
			System.exit(1);
		}
		System.exit(0);
	}

	private boolean handleCommandLineArgs(String[] args) {
		// suggest using help if no options given
		if (args.length < 1) {
			printLogo();
			Cli<CommandLineOptions> cli = CliFactory.createCli(CommandLineOptions.class);
			System.out.println(formatHelpMessage(cli.getHelpMessage()));
			printAdditionalHelp();
			return false;
		}

		// parse command line using JewelCli
		try {
			options = CliFactory.parseArguments(CommandLineOptions.class, args);
		} catch (HelpRequestedException e) {
			printLogo();
			String message = e.getMessage();
			System.out.println(formatHelpMessage(message));
			printAdditionalHelp();
			return false;
		} catch (ArgumentValidationException e) {
			printLogo();
			String message = e.getMessage();
			System.err.println(message);
			return false;
		}

		if (!options.getNologo()) {
			printLogo();
		}

		if (options.isClasspath()) {
			extendClasspath();
		}

		// input files may be regular files or classnames
		List<File> inputFiles;
		try {
			inputFiles = getFileForInputNames(options.getFiles());
		} catch (MainException e) {
			e.print();
			return false;
		}

		// if verbose output is requested, list input files
		if (options.getVerbose()) {
			for (File file : inputFiles) {
				System.out.println("Input file: " + file.getPath());
			}
			System.out.println();
		}

		// check which mode (run, compile, engine) is selected
		if (options.getRun()) {
			return runFiles(inputFiles);
		} else if (options.getTest()) {
			return testFiles(inputFiles);
		} else if (options.getCompile()) {
			return compileFiles(inputFiles);
		} else if (options.getEngine()) {
			return runEngine(inputFiles);
		} else {
			System.err.println("You should specify -r to run the files directly, "
					+ "-c to compile the files or -e to start the evoke engine for the files.");
			System.err.println("Specifying files without telling what to do with them is not implemented.");
			return false;
		}

	}

	private void printAdditionalHelp() {
		System.out.println("All further arguments that are non-options are regarded as input files.");
		System.out.println();
		System.out.println("For a detailed command-line reference, see:");
		System.out.println("https://plri.github.io/arden2bytecode/docs/command-line-options/");
	}

	private static void printLogo() {
		System.out.println("Arden2ByteCode Compiler and Runtime Environment");
		System.out.println("Copyright 2010-2016 Daniel Grunwald, Hannes Flicka, Mike Klimek");
		System.out.println();
		System.out.println("This program is free software; you can redistribute it and/or modify it "
				+ "under the terms of the GNU General Public License.");
		System.out.println();
	}

	private void extendClasspath() {
		String classpath = options.getClasspath();
		String[] paths = classpath.split(File.pathSeparator);
		List<URL> urls = new LinkedList<URL>();
		for (String path : paths) {
			File f = new File(path);
			if (!f.exists()) {
				System.err.println("Warning: Classpath file/directory \"" + path + "\" does not exist.");
				// skip file
				continue;
			}
			URL url;
			try {
				url = f.toURI().toURL();
			} catch (MalformedURLException e) {
				System.err.println("Warning: Classpath file/directory \"" + path + "\" could not be loaded:");
				e.printStackTrace();
				// skip file
				continue;
			}
			if (options.getVerbose()) {
				System.out.println("Adding to classpath: " + url);
			}
			urls.add(url);
		}

		// change classloader to contain new classpath entries
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		URLClassLoader ulc = new URLClassLoader(urls.toArray(new URL[] {}), currentClassLoader);
		Thread.currentThread().setContextClassLoader(ulc);

		if (options.getVerbose()) {
			System.out.println();
		}
	}

	private static List<File> getFileForInputNames(List<String> names) throws MainException {
		if (names == null || names.isEmpty()) {
			throw new MainException("No input files given.");
		}

		List<String> errors = new ArrayList<>();
		List<File> inputFiles = new ArrayList<File>();
		for (String name : names) {
			File file = new File(name);
			if (file.exists()) {
				// regular file
				inputFiles.add(file);
			} else {
				// may be a classname
				Matcher m = JAVA_CLASS_NAME.matcher(name);
				if (m.matches()) {
					String classFileName = name.replace('.', File.separatorChar) + COMPILED_MLM_FILE_EXTENSION;
					File classFile = new File(classFileName);
					if (classFile.exists()) {
						inputFiles.add(classFile);
					} else {
						errors.add("File " + name + " or class file " + classFileName + " does not exist.");
					}
				} else {
					errors.add("File \"" + name + "\" is neither an existing file nor a valid class name.");
				}
			}
		}

		if (!errors.isEmpty()) {
			throw new MainException(errors);
		}

		return inputFiles;
	}

	public boolean runFiles(List<File> files) {
		ArdenValue[] arguments;
		try {
			arguments = getArguments();
		} catch (MainException e) {
			e.print();
			return false;
		}

		List<MedicalLogicModule> mlms;
		try {
			mlms = getMlmsFromFiles(files);
		} catch (MainException e) {
			e.print();
			return false;
		}

		ExecutionContext context = createExecutionContext();
		for (MedicalLogicModule mlm : mlms) {
			if (options.getVerbose()) {
				System.out.println("Running MLM " + mlm.getName() + "...");
				System.out.println();
			}

			// run the mlm
			try {
				runMlm(mlm, context, arguments);
			} catch (MainException e) {
				e.print();
				return false;
			}
		}
		return true;
	}
	
	public boolean testFiles(List<File> files) {
		List<MedicalLogicModule> mlms;
		try {
			mlms = getMlmsFromFiles(files);
		} catch (MainException e) {
			e.print();
			return false;
		}
		
		boolean success = true;
		JUnitCore junit = new JUnitCore();
		junit.addListener(new TextListener(System.out));
		for (MedicalLogicModule mlm : mlms) {
			try {
				Result result = mlm.test(junit);
				if(!result.wasSuccessful()) {
					success = false;
				}
			} catch (RuntimeException e) {
				e.printStackTrace();
				success = false;
			}
		}
		return success;
	}

	public boolean compileFiles(List<File> files) {
		boolean success = true;

		// get output directory
		File outputDir = null;
		if (options.isDirectory()) {
			outputDir = options.getDirectory();
			if (!outputDir.exists()) {
				if (options.getVerbose()) {
					System.out.println("Creating directory: " + outputDir.getPath());
				}
				outputDir.mkdirs();
			}
		}

		for (File file : files) {
			// compile
			CompiledMlm mlm;
			if (options.getVerbose()) {
				System.out.println("Compiling " + file.getPath());
			}
			try {
				mlm = compileMlm(file);
			} catch (MainException e) {
				e.print();
				success = false;
				// skip MLM
				continue;
			}

			// get output file name
			String outName = mlm.getName() + COMPILED_MLM_FILE_EXTENSION;
			File outputFile;
			if (outputDir != null) {
				outputFile = new File(outputDir, outName);
				if (options.getVerbose()) {
					System.out.println("Saved to " + outputFile.getPath());
				}
			} else {
				outputFile = new File(file.getParentFile(), outName);
				System.err.println(
						"Warning: File " + file.getPath() + " compiled, but no output directory given. Assuming "
								+ outputFile.getPath() + " as output file.");
			}

			// write compiled MLM to file.
			try {

				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile));
				mlm.saveClassFile(bos);
				bos.close();
			} catch (IOException e) {
				System.err.println("Could not write output file " + outputFile.getPath() + " :");
				e.printStackTrace();
				success = false;
			}
		}

		return success;
	}

	public boolean runEngine(List<File> files) {
		List<MedicalLogicModule> mlms;
		try {
			mlms = getMlmsFromFiles(files);
		} catch (MainException e) {
			e.print();
			return false;
		}

		// Shut down gracefully on SIGINT
		final Thread engineThread = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (options.getVerbose()) {
					System.out.println("Shutting down evoke engine.");
				}
				engineThread.interrupt();
				try {
					engineThread.join(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		BaseExecutionContext context = createExecutionContext();
		EvokeEngine engine = new EvokeEngine(context, mlms);
		context.setEngine(engine);

		// start event server
		if (options.isPort()) {
			new EventServer(context, options.getVerbose(), options.getPort()).startServer();
		}

		// launch engine loop on main thread -> only exits on interrupt
		engine.run();

		return true;
	}

	private ArdenValue[] getArguments() throws MainException {
		List<ArdenValue> args = new ArrayList<>();

		if (options.isArguments()) {
			try {
				for (String arg : options.getArguments()) {
					args.add(ConstantParser.parse(arg));
				}
			} catch (ConstantParserException e) {
				throw new MainException("Error parsing arguments", e);
			}
		}

		return args.toArray(new ArdenValue[args.size()]);
	}

	private BaseExecutionContext createExecutionContext() {
		if (options.getEnvironment().startsWith("jdbc")) {
			return new JDBCExecutionContext(options);
		} else if ("stdio".equalsIgnoreCase(options.getEnvironment())) {
			return new StdIOExecutionContext(options);
		} else {
			return new StdIOExecutionContext(options);
		}
	}

	private List<MedicalLogicModule> getMlmsFromFiles(List<File> files) throws MainException {
		List<MedicalLogicModule> mlms = new ArrayList<>();
		List<String> errors = new ArrayList<String>();
		for (File file : files) {
			String filename = file.getName();
			if (filename.endsWith(COMPILED_MLM_FILE_EXTENSION)) {
				// load compiled mlm (.class file)
				try {
					mlms.add(new CompiledMlm(file, getFilenameBase(filename)));
				} catch (IOException e) {
					throw new MainException("Error loading " + file.getPath(), e);
				}
			} else if (file.getName().endsWith(MLM_FILE_EXTENSION)) {
				// compile .mlm file
				if (options.getVerbose()) {
					System.out.println("Compiling " + file.getPath() + " ...");
				}
				mlms.add(compileMlm(file));
			} else {
				errors.add("File \"" + file.getPath() + "\" is neither .class nor .mlm file. Can't run such a file.");
			}
		}
		if (!errors.isEmpty()) {
			throw new MainException(errors);
		}
		return mlms;
	}

	public static CompiledMlm compileMlm(File file) throws MainException {
		CompiledMlm mlm;
		Compiler compiler = new Compiler();
		compiler.enableDebugging(file.getPath());
		try {
			mlm = compiler.compileMlm(new FileReader(file.getPath()));
		} catch (CompilerException e) {
			throw new MainException("Could not compile " + file.getPath(), e);
		} catch (FileNotFoundException e) {
			throw new MainException("File not found: " + file.getPath());
		} catch (IOException e) {
			throw new MainException("Could not read " + file.getPath(), e);
		}
		return mlm;
	}

	public static ArdenValue[] runMlm(MedicalLogicModule mlm, ExecutionContext context, ArdenValue[] arguments) throws MainException {
		ArdenValue[] result = null;
		try {
			result = mlm.run(context, arguments, new CallTrigger());
			if (result != null && result.length == 1) {
				System.out.println("Return Value: " + result[0].toString());
			} else if (result != null && result.length > 1) {
				for (int i = 0; i < result.length; i++) {
					System.out.println("ReturnValue[" + i + "]: " + result[i].toString());
				}
			} else {
				System.out.println("There was no return value.");
			}
		} catch (InvocationTargetException e) {
			throw new MainException("Could not run MLM", e);
		}
		return result;
	}
	
	public static String getFilenameBase(String filename) {
		int sepindex = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
		int fnindex = filename.lastIndexOf('.');
		if (fnindex < sepindex) {
			fnindex = -1;
		}
		if (fnindex < 0) {
			return filename.substring(sepindex + 1);
		}
		return filename.substring(sepindex + 1, fnindex);
	}

	public static String formatHelpMessage(String message) {
		String newline = System.getProperty("line.separator");
		String indentline = newline + ARGUMENTS_INDENT + DESCRIPTION_INDENT;

		StringBuilder messageBuilder = new StringBuilder(message.length() * 2);
		for (String line : message.split(newline)) {
			line = line.replaceAll("\t", ARGUMENTS_INDENT);
			StringBuilder lineBuilder = new StringBuilder(line);
			// wrap message
			int spacePos = 0;
			while (spacePos + MAX_LINE_LENGTH < lineBuilder.length()) {
				spacePos = lineBuilder.lastIndexOf(" ", spacePos + MAX_LINE_LENGTH);
				lineBuilder.replace(spacePos, spacePos + 1, indentline);
			}
			messageBuilder.append(lineBuilder.toString() + newline);
		}

		return messageBuilder.toString();
	}

	@SuppressWarnings("serial")
	private static class MainException extends Exception {

		public MainException(String message) {
			super(message);
		}

		public MainException(String message, Throwable cause) {
			super(message, cause);
		}

		public MainException(List<String> errors) {
			super(join(errors));
		}

		public static String join(List<String> strings) {
			StringBuilder builder = new StringBuilder();
			for (String string : strings) {
				builder.append(string);
				builder.append(System.lineSeparator());
			}
			return builder.toString();
		}

		public void print() {
			System.err.println(getMessage());
			if (getCause() != null) {
				printStackTrace();
			}
		}
	}

}

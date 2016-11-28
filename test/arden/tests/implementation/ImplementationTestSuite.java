package arden.tests.implementation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	CliTest.class,
	ConstantParserTest.class,
	EvokeTest.class,
	ExampleEvokeTest.class,
	ExampleTest.class,
	GetValueTest.class,
	JDBCQueryTest.class,
	LoadMlmFromBytecodeTest.class,
	MetadataTest.class,
	RuntimeTest.class,
	ValidationTest.class,
	ValidationSyntaxTest.class
})
public class ImplementationTestSuite {
}

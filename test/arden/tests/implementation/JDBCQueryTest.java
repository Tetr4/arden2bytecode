package arden.tests.implementation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Assert;
import org.junit.Test;

import com.lexicalscope.jewel.cli.CliFactory;

import arden.CommandLineOptions;
import arden.compiler.CompiledMlm;
import arden.compiler.Compiler;
import arden.compiler.CompilerException;
import arden.runtime.ArdenList;
import arden.runtime.ArdenNumber;
import arden.runtime.ArdenString;
import arden.runtime.ArdenValue;
import arden.runtime.ExecutionContext;
import arden.runtime.MedicalLogicModule;
import arden.runtime.evoke.CallTrigger;
import arden.runtime.jdbc.DriverHelper;
import arden.runtime.jdbc.JDBCExecutionContext;
import arden.runtime.jdbc.JDBCQuery;

public class JDBCQueryTest extends ImplementationTest {
	private static boolean SQLiteLoaded = false;
	private static final String SQLITE_PATH = "./sqlite-jdbc-3.7.2.jar";
	
	private static CompiledMlm parseTemplate(String dataCode, String logicCode, String actionCode)
			throws CompilerException {
		try {
			InputStream s = JDBCQueryTest.class.getResourceAsStream("ActionTemplate.mlm");
			String fullCode = inputStreamToString(s).replace("$ACTION", actionCode).replace("$DATA", dataCode).replace(
					"$LOGIC", logicCode);
			Compiler c = new Compiler();
			return c.compileMlm(new StringReader(fullCode));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Driver loadSQLite() throws 
			MalformedURLException, 
			InstantiationException, 
			IllegalAccessException, 
			SQLException {
		if (SQLiteLoaded) {
			return DriverManager.getDriver("jdbc:sqlite:");
		}
		URL urlA = 
			new File(
					SQLITE_PATH
					).toURI().toURL();
		URL[] urls = { urlA };
		URLClassLoader ulc = new URLClassLoader(urls);
		Driver driver;
		try {
			driver = (Driver)Class.forName("org.sqlite.JDBC", true, ulc).newInstance();
		} catch (ClassNotFoundException e) {
			System.err.println("SQLite JDBC driver not found. Skipping associated test in " + this.getClass().getName());
			return null;
		}
		DriverManager.registerDriver(new DriverHelper(driver));
		SQLiteLoaded = true;
		return driver;
	}
	
	private Statement initDb() {
		Connection connection = null;
		Statement statement = null;
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:");
		    statement = connection.createStatement();
		
		    statement.executeUpdate("drop table if exists person");
		    statement.executeUpdate("create table person (id integer, name string)");
		    statement.executeUpdate("insert into person values(1, 'A')");
		    statement.executeUpdate("insert into person values(2, 'B')");		    
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return statement;
	}
	
	@Test
	public void testObjectToArdenValue() throws Exception {
		Assert.assertEquals(new ArdenNumber(1.0), 
				JDBCQuery.objectToArdenValue(new Integer(1)));
		Assert.assertEquals(new ArdenNumber(1.0), 
				JDBCQuery.objectToArdenValue(new Double(1.0)));
		Assert.assertEquals(new ArdenString("hey"),
				JDBCQuery.objectToArdenValue(new String("hey")));
	}
	
	@Test
	public void testResultSetToArdenValues() throws Exception {
		if (loadSQLite() == null) {
			return;
		}
		Statement stmt = initDb();
		ResultSet results = stmt.executeQuery("select * from person");
		ArdenValue[] ardenValues = JDBCQuery.resultSetToArdenValues(results);
		
		ArdenValue[] expectedA = {new ArdenNumber(1), new ArdenNumber(2)};
		ArdenValue[] expectedB = {new ArdenString("A"), new ArdenString("B")};
		ArdenList[] expectedArrA = {new ArdenList(expectedA), new ArdenList(expectedB)};
		Assert.assertArrayEquals(expectedArrA, ardenValues);
		
		ArdenValue[] expectedC = {new ArdenString("A"), new ArdenString("X")};
		ArdenList[] expectedArrB = {new ArdenList(expectedA), new ArdenList(expectedC)};
		assertArrayNotEquals(expectedArrB, ardenValues);		
		
		ArdenValue[] expectedD = {new ArdenString("1"), new ArdenNumber(2)};
		ArdenList[] expectedArrC = {new ArdenList(expectedD), new ArdenList(expectedB)};
		assertArrayNotEquals(expectedArrC, ardenValues);	
	}
	
	@Test
	public void testJDBCExecutionContextRead() throws Exception {
		if (loadSQLite() == null) {
			return;
		}
		String[] args = new String[]{"--env", "jdbc:sqlite:"};
		CommandLineOptions options = 
				CliFactory.parseArguments(CommandLineOptions.class, args);
		
		ExecutionContext testContext = new JDBCExecutionContext(options);
		MedicalLogicModule mlm = parseTemplate(
				"varA := read {drop table if exists person};\n" +
				"varB := read {create table person (id integer, name string)};\n" +
				"varC := read {insert into person values (1, 'A')};\n" +
				"varD := read {insert into person values (2, 'B')};\n" +
				"(varE, varF) := read {select * from person};\n", 
				"conclude true;", 
				"return (varE, varF);");
		ArdenValue[] result = mlm.run(testContext, null, new CallTrigger());
		Assert.assertEquals(1, result.length);

		ArdenValue[] expected = {new ArdenNumber(1), new ArdenNumber(2), 
				new ArdenString("A"), new ArdenString("B")};
		ArdenValue[] resultList = ((ArdenList)(result[0])).values;
		
		Assert.assertArrayEquals(expected, resultList);
	}
}

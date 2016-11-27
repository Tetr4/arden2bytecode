package arden.tests.specification.structureslots;

import org.junit.Test;

import arden.tests.specification.testcompiler.ArdenCodeBuilder;
import arden.tests.specification.testcompiler.ArdenVersion;
import arden.tests.specification.testcompiler.CompatibilityRule.Compatibility;
import arden.tests.specification.testcompiler.SpecificationTest;
import arden.tests.specification.testcompiler.TestCompilerException;

public class LogicSlotTest extends SpecificationTest {
	// TODO error on illegal statements

	@Test
	public void testAssignmentStatement() throws Exception {
		String let = createEmptyLogicSlotCodeBuilder().addLogic("LET v BE 5;").toString();
		assertValid(let);

		String assignNull = createEmptyLogicSlotCodeBuilder().addLogic("x := NULL;").toString();
		assertValid(assignNull);

		String notReference = createEmptyLogicSlotCodeBuilder()
				.addLogic("a := 5;")
				.addLogic("b := a + 3;")
				.addLogic("a := 0;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN b;")
				.toString();
		assertReturns(notReference, "8");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_5)
	public void testObjectAttributeAssignment() throws Exception {
		String data = createEmptyLogicSlotCodeBuilder()
				.addData("Pixel := OBJECT [x, y];")
				.addData("p := new Pixel;")
				.toString();
		String attrs = new ArdenCodeBuilder(data)
				.addLogic("p.x := 0;")
				.addLogic("p.y := 10;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN p.x;")
				.toString();
		assertReturns(attrs, "0");

		String invalidAttr = new ArdenCodeBuilder(data)
				.addLogic("p.w := 10;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN p.w;")
				.toString();
		assertReturns(invalidAttr, "NULL");

		String reference = new ArdenCodeBuilder(data)
				.addLogic("p.x := 10;")
				.addLogic("p2 := p;")
				.addLogic("p2.x := 5;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN p.x;")
				.toString();
		assertReturns(reference, "5");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_6)
	public void testEnhancedAssignmentStatement() throws Exception {
		String index = createEmptyLogicSlotCodeBuilder()
				.addData("values := 2, 4, 8;")
				.addLogic("values[3] := 6;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN values;")
				.toString();
		assertReturns(index, "(2,4,6)");

		String data = createEmptyLogicSlotCodeBuilder()
				.addData("Patient := OBJECT [name, date_of_birth, id];")
				.addData("john := NEW Patient WITH \"John\";")
				.addData("jane := NEW Patient WITH \"Jane\";")
				.addData("alice := NEW Patient WITH \"Alice\";")
				.addData("Room := OBJECT [room_number, patients];")
				.addData("room_40 := NEW Room WITH 40, (john, jane, alice);")
				.toString();

		String simple = new ArdenCodeBuilder(data)
				.addLogic("room_40.patients[2].id := 123;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN jane.id")
				.toString();
		assertReturns(simple, "123");

		String replaceItem = new ArdenCodeBuilder(data)
				.addLogic("room_40.patients[3] := NEW Patient WITH \"Bob\";")
				.addLogic("bob := room_40.patients[3];")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN bob.name")
				.toString();
		assertReturns(replaceItem, "\"Bob\"");

		String massAssignment = new ArdenCodeBuilder(data)
				.addLogic("room_40.patients.date_of_birth := 1970-01-01T01:02:03;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN alice.date_of_birth")
				.toString();
		assertReturns(massAssignment, "1970-01-01T01:02:03");
	}

	@Test(expected = TestCompilerException.class)
	public void testEventReassignment() throws Exception {
		String event = createEmptyLogicSlotCodeBuilder()
				.addData("e := EVENT {" + getMappings().createEventMapping() + "};")
				.addLogic("e := 5;")
				.toString();
		getCompiler().compileAndRun(event);
	}

	@Test(expected = TestCompilerException.class)
	public void testMlmReassignment() throws Exception {
		String mlm = createEmptyLogicSlotCodeBuilder()
				.addData("mymlm := MLM MLM_SELF;")
				.addLogic("mymlm := 3;")
				.toString();
		getCompiler().compileAndRun(mlm);
	}

	@Test(expected = TestCompilerException.class)
	@Compatibility(min = ArdenVersion.V2)
	public void testInterfaceReassignment() throws Exception {
		String interface_ = createEmptyLogicSlotCodeBuilder()
				.addData("i := INTERFACE {" + getMappings().getInterfaceMapping() + "};")
				.addLogic("i := 5;")
				.toString();
		getCompiler().compileAndRun(interface_);
	}

	@Test(expected = TestCompilerException.class)
	@Compatibility(min = ArdenVersion.V2_5)
	public void testObjectReassignment() throws Exception {
		String object = createEmptyLogicSlotCodeBuilder()
				.addData("Patient := OBJECT [Name, DateOfBirth, Id];")
				.addLogic("Patient := 5;")
				.toString();
		getCompiler().compileAndRun(object);
	}

	@Test
	@Compatibility(min = ArdenVersion.V2)
	public void testBranchingAssignment() throws Exception {
		String otherMlm = createCodeBuilder()
				.replaceSlotContent("mlmname:", "other_mlm")
				.addAction("RETURN 1")
				.toString();
		String chooseMlmOrInterface = createEmptyLogicSlotCodeBuilder()
				.addMlm(otherMlm)
				.addData("IF FALSE THEN x := MLM 'other_mlm';")
				.addData("ELSE x := INTERFACE {" + getMappings().getInterfaceMapping() + "};")
				.addData("ENDIF;")
				.addLogic("result := CALL x WITH 3, 4;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN result;")
				.toString();
		assertReturns(chooseMlmOrInterface, "7");
	}

	@Test
	public void testIfThenStatement() throws Exception {
		String if_ = createEmptyLogicSlotCodeBuilder()
				.addLogic("IF 5 = 5 THEN a := 0;")
				.addLogic("ENDIF;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN a;")
				.toString();
		assertReturns(if_, "0");

		String else_ = createEmptyLogicSlotCodeBuilder()
				.addLogic("IF ,TRUE THEN a := 0;")
				.addLogic("ELSE a := 1;")
				.addLogic("ENDIF;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN a;")
				.toString();
		assertReturns(else_, "1");

		String elseif1 = createEmptyLogicSlotCodeBuilder()
				.addLogic("IF 5 THEN a := 0;")
				.addLogic("ELSEIF TRUE THEN a := 1;")
				.addLogic("ENDIF;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN a;")
				.toString();
		assertReturns(elseif1, "1");

		String elseif2 = createEmptyLogicSlotCodeBuilder()
				.addLogic("IF NULL THEN a := 0;")
				.addLogic("ELSEIF FALSE THEN a := 1;")
				.addLogic("ELSEIF TRUE THEN a := 3;")
				.addLogic("ELSE a := 4;")
				.addLogic("ENDIF;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN a;")
				.toString();
		assertReturns(elseif2, "3");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_9)
	public void testIfThenAggregate() throws Exception {
		String simpleAggregate = createEmptyLogicSlotCodeBuilder()
				.addData("a := 0;")
				.addLogic("IF TRUTH VALUE 0.3 THEN a := 2;")
				.addLogic("ELSE a := 5;")
				.addLogic("ENDIF AGGREGATE;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN a IS WITHIN 4.099 TO 4.101;")
				.toString();
		assertReturns(simpleAggregate, "TRUE");

		String nestedAggregate = createEmptyLogicSlotCodeBuilder()
				.addData("a := 0;")
				.addLogic("IF TRUTH VALUE 0.2 THEN a := a + 1;")
				  .addLogic("IF TRUTH VALUE 0.3 THEN a := a + 1;")
				  .addLogic("ELSE a := a + 3;")
				  .addLogic("ENDIF;")
				.addLogic("ELSE a := a + 3;")
				.addLogic("ENDIF AGGREGATE;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN a IS WITHIN 3.079 TO 3.081;")
				.toString();
		assertReturns(nestedAggregate, "TRUE");

		String primaryTimePreserved = createEmptyLogicSlotCodeBuilder()
				.addData("a := 0;")
				.addData("TIME a := 1970-01-01T00:00:00;")
				.addLogic("IF TRUTH VALUE 0.3 THEN a := 2;")
				.addLogic("ELSE a := 5;")
				.addLogic("ENDIF AGGREGATE;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN TIME a;")
				.toString();
		assertReturns(primaryTimePreserved, "1970-01-01T00:00:00");

		String primaryTimeLost = createEmptyLogicSlotCodeBuilder()
				.addData("a := 0;")
				.addData("TIME a := 1970-01-01T00:00:00;")
				.addLogic("IF TRUTH VALUE 0.3 THEN a := 2;")
				  .addLogic("TIME a := 1990-01-01T00:00:00;")
				.addLogic("ELSE a := 5;")
				.addLogic("ENDIF AGGREGATE;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN TIME a;")
				.toString();
		assertReturns(primaryTimeLost, "NULL");

		String applicabilityPreserved = createEmptyLogicSlotCodeBuilder()
				.addData("a := 0;")
				.addData("APPLICABILITY a := TRUTH VALUE 0.3;")
				.addLogic("IF TRUTH VALUE 0.3 THEN a := 2;")
				.addLogic("ELSE a := 5;")
				.addLogic("ENDIF AGGREGATE;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN (APPLICABILITY a) IS WITHIN TRUTH VALUE 0.29 TO TRUTH VALUE 0.31;")
				.toString();
		assertReturns(applicabilityPreserved, "NULL");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_8)
	public void testSwitchCase() throws Exception {
		String switchCase = createEmptyLogicSlotCodeBuilder()
				.addData("x := 5;")
				.addLogic("SWITCH x")
				  .addLogic("CASE 4 result := FALSE;")
				  .addLogic("CASE 5 result := TRUE;")
				  .addLogic("CASE 6 result := FALSE;")
				  .addLogic("ENDSWITCH;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN result;")
				.toString();
		assertReturns(switchCase, "TRUE");

		String defaultCase = createEmptyLogicSlotCodeBuilder()
				.addData("x := 1;")
				.addLogic("SWITCH x")
				  .addLogic("CASE 4 result := FALSE;")
				  .addLogic("CASE 5 result := FALSE;")
				  .addLogic("CASE 6 result := FALSE;")
				  .addLogic("DEFAULT result := TRUE;")
				.addLogic("ENDSWITCH;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN result;")
				.toString();
		assertReturns(defaultCase, "TRUE");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_9)
	public void testSwitchCaseAggregate() throws Exception {
		String aggregate = createEmptyLogicSlotCodeBuilder()
				.addData("age := 16 YEARS;")
				.addData("young := FUZZY SET (0 YEARS, TRUTH VALUE 1), (15 YEARS, TRUTH VALUE 1), (20 YEARS, TRUTH VALUE 0);")
				.addData("middle_aged := FUZZY SET (15 YEARS, TRUTH VALUE 0), (20 YEARS, TRUTH VALUE 1), (60 YEARS, TRUTH VALUE 1), (70 YEARS, TRUTH VALUE 0);")
				.addData("dose := 0;")
				.addLogic("SWITCH AGE")
				  .addLogic("CASE young dose := 10;")
				  .addLogic("CASE middle_aged dose := 20;")
				  .addLogic("DEFAULT dose := 15;")
				.addLogic("ENDSWITCH AGGREGATE;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN dose IS WITHIN 11.99 TO 12.01;")
				.toString();
		assertReturns(aggregate, "TRUE");
	}

	@Test
	public void testConcludeStatement() throws Exception {
		String conclude = createEmptyLogicSlotCodeBuilder()
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN TRUE;")
				.toString();
		assertReturns(conclude, "TRUE");

		String concludeVariable = createEmptyLogicSlotCodeBuilder()
				.addLogic("a := 5;")
				.addLogic("CONCLUDE TRUE;")
				.addLogic("a := 3;")
				.addLogic("CONCLUDE FALSE;")
				.addAction("RETURN a;")
				.toString();
		assertReturns(concludeVariable, "5");

		String noAction1 = createEmptyLogicSlotCodeBuilder()
				.addLogic("CONCLUDE FALSE;")
				.addAction("RETURN TRUE;")
				.toString();
		assertNoReturn(noAction1);

		String noAction2 = createEmptyLogicSlotCodeBuilder()
				.addAction("RETURN TRUE;")
				.toString();
		assertNoReturn(noAction2);

		String noAction3 = createEmptyLogicSlotCodeBuilder()
				.addLogic("CONCLUDE NULL;")
				.addAction("RETURN TRUE;")
				.toString();
		assertNoReturn(noAction3);

		String noAction4 = createEmptyLogicSlotCodeBuilder()
				.addLogic("CONCLUDE ,TRUE;")
				.addAction("RETURN TRUE;")
				.toString();
		assertNoReturn(noAction4);

		String noAction5 = createEmptyLogicSlotCodeBuilder()
				.addLogic("CONCLUDE FALSE;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN TRUE;")
				.toString();
		assertNoReturn(noAction5);
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_9)
	public void testConcludeApplicability() throws Exception {
		String applicability = createEmptyLogicSlotCodeBuilder()
				.addData("x := 1;")
				.addLogic("CONCLUDE TRUTH VALUE 0.3;")
				.addAction("RETURN (APPLICABILITY OF x) IS WITHIN TRUTH VALUE 0.29 TO TRUTH VALUE 0.31;")
				.toString();
		assertReturns(applicability, "TRUE");

		String concludeWord = createEmptyLogicSlotCodeBuilder()
				.addLogic("CONCLUDE TRUTH VALUE 0.3;")
				.addAction("applicability_of_action_slot := CONCLUDE;")
				.addAction("RETURN applicability_of_action_slot IS WITHIN TRUTH VALUE 0.29 TO TRUTH VALUE 0.31;")
				.toString();
		assertReturns(concludeWord, "TRUE");
	}

	@Test
	public void testMlmCallStatement() throws Exception {
		// other mlm which is called
		String otherMlm = createCodeBuilder()
				.setName("other_mlm")
				.addData("(a, b) := ARGUMENT;")
				.addAction("IF a IS PRESENT THEN RETURN a, b; ENDIF;")
				.toString();
		String data = createEmptyLogicSlotCodeBuilder()
				.addMlm(otherMlm)
				.addData("other_mlm := MLM 'other_mlm';")
				.toString();

		String noReturn = new ArdenCodeBuilder(data)
				.addLogic("a := CALL other_mlm;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN a;")
				.toString();
		assertReturns(noReturn, "NULL");

		String param = new ArdenCodeBuilder(data)
				.addLogic("a := CALL other_mlm WITH 3;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN a;")
				.toString();
		assertReturns(param, "3");

		String nullParam = new ArdenCodeBuilder(data)
				.addLogic("a := CALL other_mlm WITH NULL;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN a;")
				.toString();
		assertReturns(nullParam, "NULL");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2)
	public void testMlmCallMultipleArguments() throws Exception {
		// other mlm which is called
		String otherMlm = createCodeBuilder()
				.setName("other_mlm")
				.addData("(a, b) := ARGUMENT;")
				.addAction("IF a IS PRESENT THEN RETURN a, b; ENDIF;")
				.toString();
		String data = createEmptyLogicSlotCodeBuilder()
				.addMlm(otherMlm)
				.addData("other_mlm := MLM 'other_mlm';")
				.toString();

		String multiParams = new ArdenCodeBuilder(data)
				.addLogic("(a,b,c) := CALL other_mlm WITH 1,2,3;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN (a,b,c);")
				.toString();
		assertReturns(multiParams, "(1,2,NULL)");

		String listParam = new ArdenCodeBuilder(data)
				.addLogic("(a, b) := CALL other_mlm WITH 2, (1,2,3);")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN a*b;")
				.toString();
		assertReturns(listParam, "(2,4,6)");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2) // multi args
	public void testEventCallStatement() throws Exception {
		String eventAssignment = "test_event := EVENT {" + getMappings().createEventMapping() + "};";

		String mlm1 = createCodeBuilder()
				.setName("mlm1")
				.addData(eventAssignment)
				.addData("(a, b) := ARGUMENT;")
				.addEvoke("test_event;")
				.addAction("RETURN a+b;")
				.toString();
		String mlm2 = createCodeBuilder()
				.setName("mlm2")
				.addData(eventAssignment)
				.addData("(a, b) := ARGUMENT;")
				.addEvoke("test_event;")
				.addAction("RETURN a*b;")
				.toString();
		String mlm3 = createCodeBuilder()
				.setName("mlm3")
				.addData(eventAssignment)
				.addEvoke("test_event;")
				.addAction("RETURN NULL;")
				.toString();
		String eventCall = createEmptyLogicSlotCodeBuilder()
				.addMlm(mlm1)
				.addMlm(mlm2)
				.addMlm(mlm3)
				.addData(eventAssignment)
				.addLogic("result_collection := CALL test_event WITH 3,4;") 
				.addLogic("mlm1_success := 7 IS IN result_collection;")
				.addLogic("mlm2_success := 12 IS IN result_collection;")
				.addLogic("mlm3_success := NULL IS NOT IN result_collection;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN mlm1_success AND mlm2_success AND mlm3_success;").toString();
		assertReturns(eventCall, "TRUE");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2)
	public void testInterfaceCallStatement() throws Exception {
		String interfaceCall = createEmptyLogicSlotCodeBuilder()
				.addData("test_interface := INTERFACE {" + getMappings().getInterfaceMapping() + "};")
				.addLogic("(x, y, z) := CALL test_interface WITH 3, 4;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN (x,y,z);")
				.toString();
		assertReturns(interfaceCall, "(7,12,NULL)");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2)
	public void testWhileLoop() throws Exception {
		String whileLoop = createEmptyLogicSlotCodeBuilder()
				.addData("i := 0;")
				.addData("c := 100;")
				.addLogic("WHILE c > 0 DO c := c-7;")
				  .addLogic("i := i+1;")
				.addLogic("ENDDO;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN (c, i);")
				.toString();
		assertReturns(whileLoop, "(-5,15)");
	}

	@Test(timeout = 1000)
	@Compatibility(min = ArdenVersion.V2_8)
	public void testBreakLoop() throws Exception {
		String breakloop = createEmptyLogicSlotCodeBuilder()
				.addData("i := 0;")
				.addLogic("WHILE TRUE DO i := i+1;")
				  .addLogic("IF i >= 5 THEN BREAKLOOP;")
				  .addLogic("ENDIF;")
				.addLogic("ENDDO;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN i;")
				.toString();
		assertReturns(breakloop, "5");

		String nested = createEmptyLogicSlotCodeBuilder()
				.addData("i := 0;")
				.addLogic("WHILE TRUE DO;")
				  .addLogic("WHILE TRUE DO i := i+1;")
				    .addLogic("IF i >= 5 THEN BREAKLOOP;")
				    .addLogic("ENDIF;")
				  .addLogic("ENDDO;")
				  .addLogic("i := 123;")
				  .addLogic("BREAKLOOP;")
				.addLogic("ENDDO;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN i;")
				.toString();
		assertReturns(nested, "123");

		// only allowed inside of loop
		assertInvalidStatement("BREAKLOOP;");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2)
	public void testForLoop() throws Exception {
		String forLoop1 = createEmptyLogicSlotCodeBuilder()
				.addData("fac := 1;")
				.addLogic("FOR i IN 1 SEQTO 5 DO fac := fac*i;")
				.addLogic("ENDDO;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN fac;")
				.toString();
		assertReturns(forLoop1, "120");

		String forLoop2 = createEmptyLogicSlotCodeBuilder()
				.addLogic("FOR i IN () DO CONCLUDE FALSE;")
				.addLogic("ENDDO;")
				.addLogic("CONCLUDE TRUE")
				.addAction("RETURN 1;")
				.toString();
		assertReturns(forLoop2, "1");

		String forLoop3 = createEmptyLogicSlotCodeBuilder()
				.addLogic("FOR i IN NULL DO CONCLUDE FALSE;")
				.addLogic("ENDDO;")
				.addLogic("CONCLUDE TRUE")
				.addAction("RETURN 1;")
				.toString();
		assertReturns(forLoop3, "1");

		String reassign = createEmptyLogicSlotCodeBuilder()
				.addLogic("FOR i IN (1,2,3) DO i := 1;")
				.addLogic("ENDDO;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN FALSE;")
				.toString();
		assertInvalid(reassign);
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_5)
	public void testNewStatement() throws Exception {
		String data = createEmptyLogicSlotCodeBuilder()
				.addData("Patient := OBJECT [Name, DateOfBirth, Id];")
				.toString();

		String new_ = new ArdenCodeBuilder(data)
				.addLogic("blankPatient := NEW Patient;")
				.toString();
		assertValid(new_);

		String newWith = new ArdenCodeBuilder(data)
				.addLogic("john := NEW Patient WITH \"John Doe\", \"1970-01-01T00:00:00\", \"1\", \"X\";")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN john.Name;")
				.toString();
		assertReturns(newWith, "\"John Doe\"");

		String uninitializedAttr = new ArdenCodeBuilder(data)
				.addLogic("john := NEW Patient WITH \"John Doe\", \"1970-01-01T00:00:00\";")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN john.Id;")
				.toString();
		assertReturns(uninitializedAttr, "NULL");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_7)
	public void testNewStatementWithInitializer() throws Exception {
		String data = createEmptyLogicSlotCodeBuilder()
				.addData("Patient := OBJECT [Name, DateOfBirth, Id];")
				.toString();

		String initialized = new ArdenCodeBuilder(data)
				.addLogic("john := NEW Patient WITH [Id:=123, Name:=\"John Doe\"];")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN john.Name;")
				.toString();
		assertReturns(initialized, "\"John Doe\"");

		String uninitializedAttr = new ArdenCodeBuilder(data)
				.addLogic("noname := NEW Patient WITH [Id:=123];")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN noname.Name;")
				.toString();
		assertReturns(uninitializedAttr, "NULL");

		String twoWiths = new ArdenCodeBuilder(data)
				.addLogic("john := NEW Patient WITH \"John Doe\" WITH [Id:=123];")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN john.DateOfBirth;")
				.toString();
		assertReturns(twoWiths, "NULL");

	}

}

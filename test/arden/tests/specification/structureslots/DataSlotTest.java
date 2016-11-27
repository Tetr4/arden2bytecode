package arden.tests.specification.structureslots;

import org.junit.Test;

import arden.tests.specification.testcompiler.ArdenCodeBuilder;
import arden.tests.specification.testcompiler.ArdenVersion;
import arden.tests.specification.testcompiler.CompatibilityRule.Compatibility;
import arden.tests.specification.testcompiler.SpecificationTest;

public class DataSlotTest extends SpecificationTest {
	// TODO error on illegal statements

	@Test
	public void testReadStatement() throws Exception {
		assertEvaluatesTo("READ {" + getMappings().getReadMapping() + "}", "(5,3,2,4,1)");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2)
	public void testReadSortedByTime() throws Exception {
		String readMapping = getMappings().getReadMapping();

		String sortedByTime1 = createCodeBuilder()
				.addData("first_ := READ FIRST {" + readMapping + "};")
				.addData("earliest_  := READ EARLIEST {" + readMapping + "};")
				.addAction("RETURN first_ = earliest_;")
				.toString();
		assertReturns(sortedByTime1, "TRUE");

		String sortedByTime2 = createCodeBuilder()
				.addData("last_ := READ LAST {" + readMapping + "};")
				.addData("latest_  := READ LATEST {" + readMapping + "};")
				.addAction("RETURN last_ = latest_;")
				.toString();
		assertReturns(sortedByTime2, "TRUE");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2)
	public void testReadAggregation() throws Exception {
		String readMapping = getMappings().getReadMapping();
		String readMultiMapping = getMappings().getReadMultipleMapping();

		assertEvaluatesTo("READ THE FIRST 2 FROM ({" + readMapping + "} WHERE THEY OCCURRED WITHIN THE 10 DAYS SURROUNDING 1990-01-01T00:00:00)", "(3,2)");
		assertEvaluatesTo("READ SUM ({" + readMapping + "} WHERE IT OCCURRED BEFORE 1995-01-01T00:00:00)", "14");

		String multipleResults = createCodeBuilder()
				.addData("(id, val):= READ LAST 2 FROM {" + readMultiMapping + "};")
				.addAction("RETURN (id, val);")
				.toString();
		assertReturns(multipleResults, "(4,1,\"d\",\"a\")");

		/*
		 * TODO check (not) permitted aggregations
		 */
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_5)
	public void testReadAs() throws Exception {
		String readMultiMapping = getMappings().getReadMultipleMapping();
		String readAs = createCodeBuilder()
				.addData("DbResult := OBJECT [Id, Val];")
				.addData("entries := READ AS DbResult {" + readMultiMapping + "};")
				.addAction("RETURN entries.val;")
				.toString();
		assertReturns(readAs, "(\"e\",\"c\",\"b\",\"d\",\"a\")");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2)
	public void testInterfaceStatement() throws Exception {
		assertValidStatement("i := INTERFACE {" + getMappings().getInterfaceMapping() + "};");
	}

	@Test
	public void testEventStatement() throws Exception {
		// usable as boolean
		String event = getMappings().createEventMapping();
		String other_mlm = createCodeBuilder()
				.setName("other_mlm")
				.addData("test_event := EVENT {" + event + "};")
				.addEvoke("test_event;")
				.addAction("IF test_event THEN RETURN TRUE;")
				.addAction("ENDIF;")
				.addAction("RETURN FALSE;")
				.toString();
		String eventAsBoolean = createEmptyLogicSlotCodeBuilder()
				.addMlm(other_mlm)
				.addData("test_event := EVENT {" + event + "};")
				.addLogic("x := CALL test_event;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN x;")
				.toString();
		assertReturns(eventAsBoolean, "(,TRUE)");
	}

	@Test
	public void testMlmStatement() throws Exception {
		String mlm1_instself = createCodeBuilder()
				.setName("mlm1")
				.addExpression("\"mlm1_instself\"")
				.toString();
		String mlm1_instother = createCodeBuilder()
				.setName("mlm1")
				.replaceSlotContent("institution:", "other_institute")
				.addExpression("\"mlm1_instother\"")
				.toString();
		String mlm2_testing = createCodeBuilder()
				.setName("mlm2")
				.replaceSlotContent("validation:", "testing")
				.addExpression("\"mlm2_testing\"")
				.toString();
		String mlm2_production = createCodeBuilder()
				.setName("mlm2")
				.replaceSlotContent("validation:", "production")
				.addExpression("\"mlm2_production\"")
				.toString();
		String data = createEmptyLogicSlotCodeBuilder()
				.addMlm(mlm1_instother)
				.addMlm(mlm1_instself)
				.addMlm(mlm2_testing)
				.addMlm(mlm2_production)
				.addLogic("found_name := CALL found_mlm;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN found_name;")
				.toString();

		/*
		 * Search algorithm:
		 * 1. institution (from mlm_self if missing)
		 * 2. mlmname
		 * 3. validation (only with institution missing)
		 * 4. version (implementation dependent)
		 */

		String noInstitution = new ArdenCodeBuilder(data).addData("found_mlm := MLM 'mlm1';").toString();
		assertReturns(noInstitution, "\"mlm1_instself\"");

		String otherInstitution = new ArdenCodeBuilder(data)
				.addData("found_mlm := MLM 'mlm1' FROM INSTITUTION \"other_institute\";").toString();
		assertReturns(otherInstitution, "\"mlm1_instother\"");

		String validation = new ArdenCodeBuilder(data).addData("found_mlm := MLM 'mlm2';").toString();
		assertReturns(validation, "\"mlm2_production\"");
	}

	@Test
	public void testMlmSelf() throws Exception {
		// recursive call
		String mlmSelf = createEmptyLogicSlotCodeBuilder()
				.addData("this := MLM MLM_SELF;")
				.addData("arg := ARGUMENT;")
				.addLogic("IF arg IS NOT PRESENT THEN arg := 10;")
				.addLogic("res := CALL this WITH arg-1;")
				.addLogic("ELSEIF arg > 1 THEN res := CALL this WITH (arg-1);")
				.addLogic("ELSE res := 1;")
				.addLogic("ENDIF;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN res * arg;")
				.toString();
		assertReturns(mlmSelf, "3628800");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2)
	public void testMultipleArgumentStatement() throws Exception {
		String othermlm = createCodeBuilder()
				.setName("other_mlm")
				.addData("(x,y) := ARGUMENT;")
				.addAction("RETURN (x,y);")
				.toString();
		String call = createCodeBuilder()
				.addMlm(othermlm)
				.addData("other_mlm := MLM 'other_mlm';")
				.clearSlotContent("logic:")
				.addLogic("res := CALL other_mlm WITH \"a\", (1,2);")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN res;")
				.toString();
		assertReturns(call, "(\"a\",1,2)");
	}

	@Test
	public void testMessageStatement() throws Exception {
		String message = createCodeBuilder()
				.addData("m := MESSAGE {" + getMappings().getMessageMapping() + "};")
				.toString();
		assertValid(message);

		// TODO too implementation specific?
		// String messageAs = createCodeBuilder()
		// 	.addData("Email := OBJECT [subject, text];")
		// 	.addData("mail := MESSAGE AS Email {"+getMappings().getMessageMapping()+"};")
		// 	.toString();
		// assertValid(messageAs);

		// String messageAsDefault = createCodeBuilder()
		// 	.addData("Email := OBJECT [subject, text];")
		// 	.addData("mail := MESSAGE AS Email;")
		// 	.toString();
		// assertValid(messageAsDefault);
	}

	@Test
	public void testDestinationStatement() throws Exception {
		String destination = createCodeBuilder()
				.addData("d := DESTINATION {" + getMappings().getDestinationMapping() + "};").toString();
		assertValid(destination);

		// TODO too implementation specific?
		// String destinationAs = createCodeBuilder()
		// 	.addData("File := OBJECT [name];")
		// 	.addData("d := DESTINATION AS File {"+getMappings().getDestinationMapping()+"};")
		// 	.toString();
		// assertValid(destinationAs);

		// String destinationAsDefault = createCodeBuilder()
		// 	.addData("File := OBJECT [name];")
		// 	.addData("d := DESTINATION AS File;")
		// 	.toString();
		// assertValid(destinationAsDefault);
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_5)
	public void testObjectStatement() throws Exception {
		String object = createCodeBuilder()
				.addData("Patient := OBJECT [Name, DateOfBirth, Id];")
				.addData("p := NEW patient;")
				.toString();
		assertValid(object);
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_9)
	public void testLinguisticVariableStatement() throws Exception {
		String data = createCodeBuilder().addData("RangeOfAge := LINGUISTIC VARIABLE [young, middleAge, old];")
				.addData("Age := new RangeOfAge;")
				.addData("Age.young := FUZZY SET (0 YEARS, TRUE), (25 YEARS, TRUE),(35 YEARS, FALSE);")
				.addData("Age.middleAge := FUZZY SET (25 YEARS, FALSE), (35 YEARS, TRUE), (55 YEARS, TRUE), (65 YEARS, FALSE);")
				.addData("Age.old := FUZZY SET (55 YEARS, FALSE), (65 YEARS, TRUE);")
				.toString();
		assertEvaluatesToWithData(data, "25 YEARS IS Age.young", "TRUE");
		assertEvaluatesToWithData(data, "45 YEARS IS Age.middleAge", "TRUE");
		assertEvaluatesToWithData(data, "45 YEARS IS Age.young", "FALSE");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_5)
	public void testIncludeStatement() throws Exception {
		String mlm1 = createCodeBuilder()
				.setName("mlm1")
				.addData("Patient := OBJECT [Name, DateOfBirth, Id];")
				.toString();
		String mlm2 = createCodeBuilder()
				.setName("mlm2")
				.addData("Patient := OBJECT [Id, Name, DateOfBirth, Gender, Phone];")
				.toString();

		String include = createCodeBuilder()
				.addMlm(mlm1)
				.addData("othermlm := MLM 'mlm1';")
				.addData("INCLUDE othermlm;")
				.addData("p := NEW Patient;")
				.addData("p.Id := 5;")
				.addAction("RETURN p.Id;")
				.toString();
		assertReturns(include, "5");

		String localPrecedence = createCodeBuilder()
				.addMlm(mlm1)
				.addData("Patient := OBJECT [Id, Name, Phone];")
				.addData("othermlm := MLM 'mlm1';")
				.addData("INCLUDE othermlm;")
				.addData("p := NEW Patient;")
				.addData("p.Phone := 12345;")
				.addAction("RETURN p.Phone;")
				.toString();
		assertReturns(localPrecedence, "12345");

		String latestPrecedence = createCodeBuilder()
				.addMlm(mlm1)
				.addMlm(mlm2)
				.addData("mlm1 := MLM 'mlm1';")
				.addData("INCLUDE mlm1;")
				.addData("mlm2 := MLM 'mlm2';")
				.addData("INCLUDE mlm2;")
				.addData("p := NEW Patient;")
				.addData("p.Gender := \"f\";")
				.addAction("RETURN p.Gender;")
				.toString();
		assertReturns(latestPrecedence, "\"f\"");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_6)
	public void testIncludeResources() throws Exception {
		String resourceDefinitionMlm = createCodeBuilder()
				.setName("resource_mlm")
				.addTextConstant("en", "msg", "a message")
				.toString();
		String localized = createCodeBuilder()
				.addMlm(resourceDefinitionMlm)
				.addData("resource_mlm := MLM 'resource_mlm';")
				.addData("INCLUDE resource_mlm;")
				.addData("msg := LOCALIZED 'msg';")
				.addAction("RETURN msg;")
				.toString();
		assertReturns(localized, "\"a message\"");
	}

}

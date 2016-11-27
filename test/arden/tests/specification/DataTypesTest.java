package arden.tests.specification;

import org.junit.Test;

import arden.tests.specification.testcompiler.ArdenVersion;
import arden.tests.specification.testcompiler.CompatibilityRule.Compatibility;
import arden.tests.specification.testcompiler.SpecificationTest;

public class DataTypesTest extends SpecificationTest {

	private static final String DELAY = "FOR i IN 1 SEQTO 100000 DO x := 0; ENDDO;";

	@Test
	public void testNull() throws Exception {
		assertEvaluatesTo("NULL || 1/0 || TRUE +1", "\"nullnullnull\"");
	}

	@Test
	public void testNumber() throws Exception {
		// floating point arithmetic
		assertEvaluatesTo("1/2", "0.5");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2) // for loop
	public void testNow() throws Exception {
		String constantNow = createCodeBuilder()
				.addData("n := NOW;")
				.clearSlotContent("logic:")
				.addLogic(DELAY)
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN n = NOW;")
				.toString();
		assertReturns(constantNow, "TRUE");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2)
	public void testTriggerTime() throws Exception {
		// trigger delay
		String eventMapping = getMappings().createEventMapping();
		String eventDelay = createCodeBuilder()
				.addData("test_event := EVENT {" + eventMapping + "};")
				.addEvoke(".5 SECONDS AFTER TIME OF test_event")
				.addAction("WRITE TRIGGERTIME = EVENTTIME + .5 SECONDS;")
				.addAction("WRITE TRIGGERTIME <= NOW;")
				.toString();
		assertWritesAfterEvent(eventDelay, eventMapping, "TRUE", "TRUE");

		// call delay
		String startEvent = getMappings().createEventMapping();
		String othermlm = createCodeBuilder()
				.setName("other_mlm")
				.addAction("WRITE TRIGGERTIME = EVENTTIME + .5 SECOND;")
				.toString();
		String combinedDelay = createCodeBuilder()
				.addMlm(othermlm)
				.addData("start_event := EVENT {" + startEvent + "};")
				.addData("other_mlm := MLM 'other_mlm';")
				.addEvoke("start_event")
				.addAction("CALL other_mlm DELAY 0.5 SECONDS;") // 1. delay
				.toString();
		assertWritesAfterEvent(combinedDelay, startEvent, "TRUE");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_1)
	public void testCurrentTime() throws Exception {
		String currentTime = createCodeBuilder()
				.addData("c := CURRENTTIME;")
				.clearSlotContent("logic:")
				.addLogic(DELAY)
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN c < CURRENTTIME;")
				.toString();
		assertReturns(currentTime, "TRUE");

		assertEvaluatesTo("NOW <= CURRENTTIME", "TRUE");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2)
	public void testDuration() throws Exception {
		// time - time
		assertEvaluatesTo("1990-03-01T00:00:00 - 1990-02-01T00:00:00", "2419200 seconds");

		// time + seconds
		assertEvaluatesTo("1990-02-01T00:00:00 + 2419201 seconds", "1990-03-01T00:00:01");

		// time + months
		assertEvaluatesTo("1991-01-31T00:00:00 + 1 month", "1991-02-28T00:00:00");
		assertEvaluatesTo("1991-01-31T00:00:00 + 1.1 months", "1991-03-03T01:02:54.6");
		assertEvaluatesTo("1991-04-30T00:00:00 - 0.1 months", "1991-04-26T22:57:05.4");

		// month/seconds
		assertEvaluatesTo("1 month / 1 second", "2629746");
	}

	@Test
	public void testList() throws Exception {
		assertValidStatement("x := \"Milk\", 5, TRUE, NULL, 1 second");

		// empty List
		assertValidStatement("x := ();");
		assertValidStatement("x := (         );");

		// single element list
		assertValidStatement("x := ,5;");
		assertValidStatement("x := ,NULL;");

		// conversion of single element to list
		assertEvaluatesTo("COUNT 5", "1");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_6)
	public void testDayOfWeek() throws Exception {
		assertEvaluatesTo("MONDAY = 1", "TRUE");
		assertEvaluatesTo("TUESDAY = 2", "TRUE");
		assertEvaluatesTo("WEDNESDAY = 3", "TRUE");
		assertEvaluatesTo("THURSDAY = 4", "TRUE");
		assertEvaluatesTo("FRIDAY = 5", "TRUE");
		assertEvaluatesTo("SATURDAY = 6", "TRUE");
		assertEvaluatesTo("SUNDAY = 7", "TRUE");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_9)
	public void testTruthValue() throws Exception {
		assertEvaluatesTo("TRUTH VALUE 0 = FALSE", "TRUE");
		assertEvaluatesTo("TRUTH VALUE 0.5 = FALSE", "FALSE");
		assertEvaluatesTo("TRUTH VALUE 0.5 = TRUE", "FALSE");
		assertEvaluatesTo("TRUTH VALUE 1 = TRUE", "TRUE");
		assertEvaluatesTo("TRUTH VALUE TRUE", "TRUE");
		assertEvaluatesTo("TRUTH VALUE FALSE", "FALSE");

		// truth value type is a generalizations of boolean type
		assertEvaluatesTo("TRUTH VALUE 1 IS BOOLEAN", "TRUE");
		assertEvaluatesTo("TRUTH VALUE 0.5 IS BOOLEAN", "FALSE");
		assertEvaluatesTo("TRUTH VALUE 0 IS BOOLEAN", "TRUE");

		assertEvaluatesTo("TRUE IS TRUTH VALUE", "TRUE");
		assertEvaluatesTo("FALSE IS TRUTH VALUE", "TRUE");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_9)
	public void testFuzzyNumber() throws Exception {
		String trapezoid = createCodeBuilder()
				.addData("one_to_four := FUZZY SET (1, TRUTH VALUE 0), (2, TRUTH VALUE 1), (2, TRUTH VALUE 1), (3, TRUTH VALUE 1), (4, TRUTH VALUE 0);")
				.toString();
		assertEvaluatesToWithData(trapezoid, "1 IS IN one_to_four", "FALSE");
		assertEvaluatesToWithData(trapezoid, "(1.5 IS IN one_to_four) IS WITHIN TRUTH VALUE 0.49 TO TRUTH VALUE 0.51", "TRUE");
		assertEvaluatesToWithData(trapezoid, "2 IS IN one_to_four", "TRUE");
		assertEvaluatesToWithData(trapezoid, "3 IS IN one_to_four", "TRUE");
		assertEvaluatesToWithData(trapezoid, "(3.5 IS IN one_to_four) IS WITHIN TRUTH VALUE 0.49 TO TRUTH VALUE 0.51", "TRUE");
		assertEvaluatesToWithData(trapezoid, "4 IS IN one_to_four", "FALSE");

		String plateau = createCodeBuilder()
				.addData("two_to_three := FUZZY SET (2, TRUTH VALUE 0), (2, TRUTH VALUE 1), (3, TRUTH VALUE 1), (3, TRUTH VALUE 0);")
				.toString();
		assertEvaluatesToWithData(plateau, "1.999 IS IN two_to_three", "FALSE");
		assertEvaluatesToWithData(plateau, "2 IS IN two_to_three", "FALSE");
		assertEvaluatesToWithData(plateau, "2.001 IS IN two_to_three", "TRUE");
		assertEvaluatesToWithData(plateau, "3 IS IN two_to_three", "TRUE");
		assertEvaluatesToWithData(plateau, "3.001 IS IN two_to_three", "FALSE");

		String plateauLeftMembership = createCodeBuilder()
				.addData("two_to_three := FUZZY SET (2, TRUTH VALUE 0), (2, TRUTH VALUE 1), (2, TRUTH VALUE 1), (3, TRUTH VALUE 1), (3, TRUTH VALUE 0);")
				.toString();
		assertEvaluatesToWithData(plateauLeftMembership, "1.999 IS IN two_to_three", "FALSE");
		assertEvaluatesToWithData(plateauLeftMembership, "2 IS IN two_to_three", "TRUE");
		assertEvaluatesToWithData(plateauLeftMembership, "2.001 IS IN two_to_three", "TRUE");
		assertEvaluatesToWithData(plateauLeftMembership, "3 IS IN two_to_three", "TRUE");
		assertEvaluatesToWithData(plateauLeftMembership, "3.001 IS IN two_to_three", "FALSE");

		String triangular = createCodeBuilder()
				.addData("two := 2 FUZZIFIED BY 1;")
				.toString();
		assertEvaluatesToWithData(triangular, "1 IS IN two", "FALSE");
		assertEvaluatesToWithData(triangular, "(1.5 IS IN two) IS WITHIN TRUTH VALUE 0.49 TO TRUTH VALUE 0.51", "TRUE");
		assertEvaluatesToWithData(triangular, "2 IS IN two", "TRUE");
		assertEvaluatesToWithData(triangular, "(2.5 IS IN two) IS WITHIN TRUTH VALUE 0.49 TO TRUTH VALUE 0.51", "TRUE");
		assertEvaluatesToWithData(triangular, "3 IS IN two", "FALSE");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_9)
	public void testFuzzyTime() throws Exception {
		String trapezoid = createCodeBuilder()
				.addData("important_time := FUZZY SET (2000-01-01T00:00:00, TRUTH VALUE 0), (2000-06-01, TRUTH VALUE 1), (2001-01-01, TRUTH VALUE 1), (2001-06-01, TRUTH VALUE 0);")
				.toString();
		assertEvaluatesToWithData(trapezoid, "2000-09-01 IS IN important_time", "TRUE");
		assertEvaluatesToWithData(trapezoid, "(2000-02-01 IS IN important_time) IS WITHIN TRUTH VALUE 0.2 TO TRUTH VALUE 0.21", "TRUE");

		String triangular = createCodeBuilder()
				.addData("year_2k := 2000-01-01T00:00:00 FUZZIFIED BY 1 DAY;")
				.toString();
		assertEvaluatesToWithData(triangular, "2000-01-01 IS IN year_2k", "TRUE");
		assertEvaluatesToWithData(triangular, "(2000-01-01T12:00:00 IS IN year_2k) IS WITHIN TRUTH VALUE 0.49 TO TRUTH VALUE 0.51", "TRUE");
		assertEvaluatesToWithData(triangular, "1990-01-01 IS IN year_2k", "FALSE");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_9)
	public void testFuzzyDuration() throws Exception {
		String trapezoid = createCodeBuilder()
				.addData("time_for_a_walk := FUZZY SET (5 MINUTES, TRUTH VALUE 0), (15, TRUTH VALUE 1), (1 HOUR, TRUTH VALUE 1), (2 HOURS, TRUTH VALUE 0);")
				.toString();
		assertEvaluatesToWithData(trapezoid, "30 MINUTES IS IN time_for_a_walk", "TRUE");
		assertEvaluatesToWithData(trapezoid, "1 MONTH IS IN time_for_a_walk", "FALSE");

		String triangular = createCodeBuilder()
				.addData("length_of_a_month := 1 MONTH FUZZIFIED BY 5 DAYS;")
				.toString();
		assertEvaluatesToWithData(triangular, "1 MONTH IS IN length_of_a_month", "TRUE");
		assertEvaluatesToWithData(triangular, "1 WEEK IS IN length_of_a_month", "FALSE");
	}

}

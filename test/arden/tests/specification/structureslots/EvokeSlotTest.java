package arden.tests.specification.structureslots;

import org.junit.Test;

import arden.tests.specification.testcompiler.ArdenCodeBuilder;
import arden.tests.specification.testcompiler.ArdenVersion;
import arden.tests.specification.testcompiler.CompatibilityRule.Compatibility;
import arden.tests.specification.testcompiler.SpecificationTest;
import arden.tests.specification.testcompiler.TestCompilerException;

public class EvokeSlotTest extends SpecificationTest {

	@Test
	@Compatibility(min = ArdenVersion.V2)
	public void testTimeOfEvent() throws Exception {
		String event = getMappings().createEventMapping();
		String othermlm = createCodeBuilder()
				.replaceSlotContent("mlmname:", "other_mlm")
				.addData("test_event := EVENT {" + event + "};")
				.addEvoke("test_event;")
				.addAction("RETURN (TIME OF test_event = EVENTTIME);")
				.toString();
		String eventtime = createEmptyLogicSlotCodeBuilder()
				.addMlm(othermlm)
				.addData("test_event := EVENT {" + event + "};")
				.addLogic("result := CALL test_event;")
				.addLogic("CONCLUDE TRUE;")
				.addAction("RETURN result;")
				.toString();
		assertReturns(eventtime, "(,TRUE)");
	}

	@Test(timeout = 5000)
	public void testSimpleTrigger() throws Exception {
		assertDelayedBy("test_event", 0);
		assertDelayedBy("TIME OF test_event", 0);

		String eventMapping = getMappings().createEventMapping();
		String any = createCodeBuilder()
				.addData("test_event1 := EVENT {" + getMappings().createEventMapping() + "};")
				.addData("test_event2 := EVENT {" + eventMapping + "};")
				.addData("test_event3 := EVENT {" + getMappings().createEventMapping() + "};")
				.addEvoke("test_event1 OR ANY OF(test_event2, test_event3)")
				.addAction("WRITE \"success\";")
				.toString();
		assertDelayedBy(any, eventMapping, 0);

		String invalidEvent = createCodeBuilder()
				.addData("a := 5;")
				.addEvoke("a;")
				.toString();
		assertInvalid(invalidEvent);
	}

	@Test(timeout = 5000)
	public void testDelayedEventTrigger() throws Exception {
		// duration
		assertDelayedBy(".5 SECONDS AFTER TIME OF test_event", 500);
		assertDelayedBy(".5 SECONDS AFTER .5 SECOND AFTER TIME OF test_event", 1000);
		assertInvalidEvokeStatement("10 SECONDS BEFORE TIME OF test_event");
	}

	@Test(timeout = 5000)
	@Compatibility(min = ArdenVersion.V2_7)
	public void testDelayedEventTriggerRelativeConstant() throws Exception {
		// time constant (in the past, so execute immediately)
		assertDelayedBy("1800-01-01 AFTER TIME OF test_event", 0);

		// day of week attime time of day
		assertValidEvokeStatement("MONDAY ATTIME 12:00 AFTER TIME OF test_event");
		assertValidEvokeStatement("TUESDAY ATTIME 12:00:00 AFTER TIME OF test_event");
		assertValidEvokeStatement("WEDNESDAY ATTIME 12:00:30Z OR FRIDAY AT 20:00 AFTER TIME OF test_event");

		// reserved words (in the past, so executed immediately)
		assertDelayedBy("TODAY ATTIME 00:00 AFTER TIME OF test_event", 0);
		assertDelayedBy("TODAY ATTIME 00:00:00 AFTER TIME OF test_event", 0);
		assertValidEvokeStatement("TOMORROW ATTIME 12:00 AFTER TIME OF test_event");
	}

	@Test(timeout = 5000)
	@Compatibility(min = ArdenVersion.V2_6, max = ArdenVersion.V2_6, pedantic = true)
	public void testDelayedEventTriggerRelativeConstantV2_6() throws Exception {
		// time constant
		assertDelayedBy("1800-01-01 AFTER TIME OF test_event", 0);

		// day of week at time of day
		assertValidEvokeStatement("MONDAY AT 12:00 AFTER TIME OF test_event");
		assertValidEvokeStatement("TUESDAY AT 12:00:00 AFTER TIME OF test_event");
		assertValidEvokeStatement("WEDNESDAY AT 12:00:30Z OR FRIDAY AT 20:00 AFTER TIME OF test_event");

		// reserved word
		assertDelayedBy("TODAY AT 00:00 AFTER TIME OF test_event", 0);
		assertDelayedBy("TODAY AT 00:00:00 AFTER TIME OF test_event", 0);
		assertValidEvokeStatement("TOMORROW AT 12:00 AFTER TIME OF test_event");
	}

	@Test
	public void testConstantTimeTrigger() throws Exception {
		String datetime = createCodeBuilder()
				.addEvoke("2016-01-01T12:00:00;")
				.addEvoke("2016-01-02T12:00:00;")
				.toString();
		assertValid(datetime);

		assertDelayedBy("1800-02-05", 0);
		assertDelayedBy("1 WEEK AFTER 1800-01-01", 0);
	}

	@Test(timeout = 5000)
	@Compatibility(min = ArdenVersion.V2_7)
	public void testConstantTimeTriggerTimeExpression() throws Exception {
		assertDelayedBy("1 SECOND", 1000);
		assertDelayedBy("TODAY ATTIME 00:00:00", 0);
		assertValidEvokeStatement("TOMORROW ATTIME 02:30");
		assertValidEvokeStatement("MONDAY ATTIME 02:30");
	}

	@Test(timeout = 5000)
	@Compatibility(min = ArdenVersion.V2_6, max = ArdenVersion.V2_6, pedantic = true)
	public void testConstantTimeTriggerTimeExpressionV2_6() throws Exception {
		assertDelayedBy("1 SECOND", 1000);
		assertDelayedBy("TODAY AT 00:00:00", 0);
		assertValidEvokeStatement("TOMORROW AT 02:30");
		assertValidEvokeStatement("MONDAY AT 02:30");
	}

	@Test(timeout = 10000)
	public void testPeriodicEventTrigger() throws Exception {
		String eventMapping = getMappings().createEventMapping();
		String data = createCodeBuilder()
				.addData("test_event := EVENT {" + eventMapping + "};")
				.addAction("WRITE \"success\";")
				.toString();

		String simple = new ArdenCodeBuilder(data)
				.addEvoke("EVERY 1 SECOND FOR 2 SECONDS STARTING TIME OF test_event")
				.toString();
		assertDelayedBy(simple, eventMapping, 0, 1000, 2000);

		String complex = new ArdenCodeBuilder(data)
				.addEvoke("EVERY 1 SECONDS FOR 2 SECONDS STARTING .7 SECONDS AFTER THE TIME OF test_event")
				.toString();
		assertDelayedBy(complex, eventMapping, 700, 1700, 2700);

		String condition = new ArdenCodeBuilder(data)
				.addData("a := 0;")
				.addEvoke("EVERY 1 WEEK FOR 6 MONTHS STARTING 1990-01-01T12:00:00 UNTIL a > 5")
				.toString();
		assertValid(condition);
	}

	@Test(timeout = 10000)
	@Compatibility(min = ArdenVersion.V2_7)
	public void testPeriodicEventTriggerRelativeConstant() throws Exception {
		assertValidEvokeStatement("EVERY 1 YEAR FOR 99 YEARS STARTING 2010-06-03T12:33:00Z AFTER TIME OF test_event");
		assertValidEvokeStatement("EVERY 1 HOUR FOR 2 WEEKS STARTING TODAY ATTIME 12:33:54Z AFTER TIME OF test_event");
	}

	@Test(timeout = 10000)
	@Compatibility(min = ArdenVersion.V2_6, max = ArdenVersion.V2_6, pedantic = true)
	public void testPeriodicEventTriggerRelativeConstantV2_6() throws Exception {
		assertValidEvokeStatement("EVERY 1 YEAR FOR 99 YEARS STARTING 2010-06-03T12:33:00Z AFTER TIME OF test_event");
		assertValidEvokeStatement("EVERY 1 HOUR FOR 2 WEEKS STARTING TODAY AT 12:33:54Z AFTER TIME OF test_event");
	}

	@Test
	public void testConstantPeriodicTimeTrigger() throws Exception {
		assertValidEvokeStatement("EVERY 1 DAY FOR 14 DAYS STARTING 1992-01-01T00:00:00");
		assertValidEvokeStatement("EVERY 1 WEEK FOR 1 MONTH STARTING 3 DAYS AFTER 1992-01-01T00:00:00 UNTIL TRUE");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_7)
	public void testConstantPeriodicTimeTriggerRelativeConstant() throws Exception {
		assertValidEvokeStatement("EVERY 2 HOURS FOR 1 DAY STARTING TODAY ATTIME 12:00");
	}

	@Test
	@Compatibility(min = ArdenVersion.V2_6, max = ArdenVersion.V2_6, pedantic = true)
	public void testConstantPeriodicTimeTriggerRelativeConstantV2_6() throws Exception {
		assertValidEvokeStatement("EVERY 2 HOURS FOR 1 DAY STARTING TODAY AT 12:00");
	}

	@Test
	@Compatibility(max = ArdenVersion.V2, pedantic = true)
	public void testWhereTriggerStatement() throws Exception {
		assertValidEvokeStatement("test_event WHERE TRUE");

		String complex = createCodeBuilder()
				.addData("penicillin_storage := EVENT {" + getMappings().createEventMapping() + "};")
				.addData("cephalosporin_storage := EVENT {" + getMappings().createEventMapping() + "};")
				.addData("penicillin_dose := 600;")
				.addData("cephalosporin_dose := 400;")
				.addEvoke("(penicillin_storage WHERE penicillin_dose > 500) OR (cephalosporin_storage WHERE cephalosporin_dose > 500)")
				.addAction("WRITE \"success\";")
				.toString();
		assertValid(complex);
	}

	@Test
	@Compatibility(max = ArdenVersion.V1)
	public void testCallStatement() throws Exception {
		assertValidEvokeStatement("CALL");
	}

	@Test(timeout = 5000)
	@Compatibility(min = ArdenVersion.V2)
	public void testCombinedDelay() throws Exception {
		String startEvent = getMappings().createEventMapping();
		String callEvent = getMappings().createEventMapping();
		String othermlm = createCodeBuilder()
				.replaceSlotContent("mlmname:", "other_mlm")
				.addData("call_event := EVENT {" + callEvent + "};")
				.addEvoke(".5 SECONDS AFTER TIME OF call_event") // 2. delay
				.addAction("WRITE \"success\";")
				.toString();
		String combinedDelay = createCodeBuilder().addMlm(othermlm)
				.addData("start_event := EVENT {" + startEvent + "};")
				.addData("call_event := EVENT {" + callEvent + "};")
				.addEvoke("start_event")
				.addAction("CALL call_event DELAY 0.5 SECONDS;") // 1. delay
				.toString();
		assertDelayedBy(combinedDelay, startEvent, 1000);
	}

	private void assertDelayedBy(String evokeStatement, int delay) throws TestCompilerException {
		String eventMapping = getMappings().createEventMapping();
		String code = createCodeBuilder().addData("test_event := EVENT {" + eventMapping + "};")
				.addEvoke(evokeStatement).addAction("WRITE \"success\";").toString();
		assertDelayedBy(code, eventMapping, delay);
	}

	private void assertInvalidEvokeStatement(String evokeStatement) throws TestCompilerException {
		String code = createCodeBuilder().addData("test_event := EVENT {" + getMappings().createEventMapping() + "};")
				.addEvoke(evokeStatement).addAction("WRITE \"success\";").toString();
		assertInvalid(code);
	}

	private void assertValidEvokeStatement(String evokeStatement) throws TestCompilerException {
		String code = createCodeBuilder().addData("test_event := EVENT {" + getMappings().createEventMapping() + "};")
				.addEvoke(evokeStatement).addAction("WRITE \"success\";").toString();
		assertValid(code);
	}

}

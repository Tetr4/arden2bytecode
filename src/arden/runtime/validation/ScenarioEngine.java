package arden.runtime.validation;

import arden.runtime.ArdenNumber;
import arden.runtime.ArdenTime;
import arden.runtime.ArdenValue;
import arden.runtime.MedicalLogicModule;

public class ScenarioEngine {

	private ScenarioExecutionContext context;

	public ScenarioEngine(ScenarioExecutionContext context) {
		this.context = context;
	}

	public void callMlm(MedicalLogicModule mlm, ArdenValue[] args) {
		System.out.println("callMlm");
	}

	public void callEvent(String mapping, ArdenTime primaryTime, ArdenTime eventTime) {
		System.out.println("callEvent");
	}

	public void setTime(ArdenTime time) {
		System.out.println("setTime");
	}

	public void assertIsTriggered(boolean not, MedicalLogicModule mlm) {
		System.out.println("assertIsTriggered");
	}

	public void assertConclude(boolean not, MedicalLogicModule mlm, ArdenValue value) {
		System.out.println("assertConclude");
	}

	public void assertNothingReturned(boolean not, MedicalLogicModule mlm) {
		System.out.println("assertNothingReturned");
	}

	public ArdenValue[] getReturnedValues(MedicalLogicModule mlm) {
		System.out.println("getReturnedValues");
		return new ArdenValue[] { new ArdenNumber(5) };
	}

}

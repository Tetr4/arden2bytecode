package arden.runtime.validation;

import arden.runtime.ArdenValue;
import arden.runtime.MedicalLogicModule;

public class MlmCall extends Call {
	public final MedicalLogicModule mlm;

	public MlmCall(MedicalLogicModule mlm, ArdenValue[] args, ArdenValue delay) {
		super(args, delay);
		this.mlm = mlm;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MlmCall) {
			MlmCall other = (MlmCall) obj;
			return this.mlm.getName().equalsIgnoreCase(other.mlm.getName()) && super.equals(other);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + mlm.getName().toLowerCase().hashCode();
	}
}
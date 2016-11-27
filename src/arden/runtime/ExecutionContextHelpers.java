package arden.runtime;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import arden.runtime.MaintenanceMetadata.Validation;
import arden.runtime.evoke.CallTrigger;
import arden.runtime.evoke.Trigger;

public class ExecutionContextHelpers {

	/**
	 * The algorithm for the MLM statement.
	 * 
	 * @param name
	 *            Name of the MLM to find.
	 * @param institution
	 *            Institution of the MLM to find. May be <code>null</code> to
	 *            signify that no institution is specified.
	 * @param institutionSelf
	 *            The calling MLMs institution. Only used if institution is
	 *            <code>null</code>.
	 * @param mlms
	 *            All MLMs which should be search for a match.
	 * @param versionComparator
	 *            A {@link Comparator} to compare versions in the MLMs
	 *            "version:" slot. May be <code>null</code>.
	 * @return The best matching MLM or <code>null</code> if none is found.
	 */
	public static MedicalLogicModule findModule(String name, String institution, MedicalLogicModule[] mlms,
			final Comparator<String> versionComparator) {
		if (!name.matches("[a-zA-Z0-9\\-_]+")) {
			throw new IllegalArgumentException("Malformed module name: " + name);
		}

		institution = institution.toLowerCase().trim();

		name = name.toLowerCase().trim();

		// sort results by validation and version
		Comparator<MedicalLogicModule> mlmComparator = new Comparator<MedicalLogicModule>() {
			@Override
			public int compare(MedicalLogicModule mlm1, MedicalLogicModule mlm2) {
				Validation validation1 = mlm1.getMaintenance().getValidation();
				Validation validation2 = mlm2.getMaintenance().getValidation();
				// highest validation first
				int result = validation2.compareTo(validation1);
				if (result != 0) {
					return result;
				} else if (versionComparator != null) {
					String version1 = mlm1.getMaintenance().getVersion().trim();
					String version2 = mlm2.getMaintenance().getVersion().trim();
					// highest version first
					return versionComparator.compare(version2, version1);
				} else {
					return 0;
				}
			}
		};
		Queue<MedicalLogicModule> matches = new PriorityQueue<MedicalLogicModule>(11, mlmComparator);

		// find mlms with matching institution name
		for (MedicalLogicModule mlm : mlms) {
			MaintenanceMetadata maintenance = mlm.getMaintenance();
			String curMlmname = maintenance.getMlmName().toLowerCase().trim();
			String curInstitution = maintenance.getInstitution().toLowerCase().trim();
			if (curInstitution.equals(institution) && curMlmname.equals(name)) {
				matches.add(mlm);
			}
		}

		if (!matches.isEmpty()) {
			return matches.peek();
		}
		return null;
	}

	public static Trigger combine(Trigger callingMlmsTrigger, long delay) {
		ArdenEvent event = callingMlmsTrigger.getTriggeringEvent();
		long combinedDelay = callingMlmsTrigger.getDelay() + delay;
		return new CallTrigger(event, combinedDelay);
	}

	public static ArdenEvent combine(ArdenEvent event, long delay) {
		if (event.eventTime == ArdenValue.NOPRIMARYTIME) {
			throw new IllegalArgumentException("Event must have a valid EVENTTIME");
		}
		return new ArdenEvent(event.name, event.primaryTime, event.eventTime + delay);
	}

	public static long delayToMillis(ArdenValue delay) {
		if (!(delay instanceof ArdenDuration)) {
			throw new IllegalArgumentException("Delay must be a duration");
		}
		ArdenDuration delayDuration = (ArdenDuration) delay;
		return Math.round(delayDuration.toSeconds() * 1000);
	}

}

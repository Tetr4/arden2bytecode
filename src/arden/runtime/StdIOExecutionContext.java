package arden.runtime;

import java.util.Scanner;

import arden.CommandLineOptions;
import arden.constants.ConstantParser;
import arden.constants.ConstantParserException;

/** Reads and prints queries from/to StdIO. */
public class StdIOExecutionContext extends BaseExecutionContext {
	private Scanner sc = new Scanner(System.in);
	private static final String PROMPT_SIGN = "> ";

	public StdIOExecutionContext(CommandLineOptions options) {
		super(options);
	}
	
	@Override
	public DatabaseQuery createQuery(String mapping) {
		System.out.println(
				"Query mapping: \"" + mapping + "\". Enter result as " + "Arden Syntax constant (Strings in quotes)");
		System.out.print(PROMPT_SIGN);

		ArdenValue[] val = null;
		while (val == null) {
			if (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (line.isEmpty()) {
					System.out.print(PROMPT_SIGN);
					continue; // retry
				}

				try {
					val = ConstantParser.parseMultiple(line);
				} catch (ConstantParserException e) {
					System.out.println("Syntax error: ");
					System.out.println(e.getMessage());
					System.out.println("Please enter again:");
					System.out.print(PROMPT_SIGN);
					continue; // retry
				}
			} else {
				val = new ArdenValue[] { ArdenNull.create(System.currentTimeMillis()) };
			}
		}

		return new MemoryQuery(val);
	}

	@Override
	public ArdenValue getMessage(String mapping) {
		System.out.println("Message, mapping: " + mapping);
		return new ArdenString(mapping);
	}
	
	@Override
	public ArdenObject getMessageAs(String mapping, ObjectType type) {
		System.out.println("Message, mapping: " + mapping + ", type: " + type.name);
		ArdenObject object = new ArdenObject(type);
		if(object.fields.length > 0) {
			object.fields[0] = new ArdenString(mapping);
		}
		return object;
	}
	
	@Override
	public ArdenValue getDestination(String mapping) {
		System.out.println("Destination, mapping: " + mapping);
		return new ArdenString(mapping);
	}
	
	@Override
	public ArdenObject getDestinationAs(String mapping, ObjectType type) {
		System.out.println("Destination, mapping: " + mapping + ", type: " + type.name);
		ArdenObject object = new ArdenObject(type);
		if(object.fields.length > 0) {
			object.fields[0] = new ArdenString(mapping);
		}
		return object;
	}

	@Override
	public void write(ArdenValue message, ArdenValue destination, double urgency) {
		String destString = ArdenString.getStringFromValue(destination);
		if (destString != null  && "stdout".equalsIgnoreCase(destString)) {
			// just print string
			if (message instanceof ArdenString) {
				System.out.println(ArdenString.getStringFromValue(message));
			} else {
				System.out.println(message);
			}
		} else {
			// prepend destination to printed string
			System.out.print("Destination: ");
			System.out.print(destination);
			System.out.print(" Message: ");
			if (message instanceof ArdenString) {
				System.out.println(ArdenString.getStringFromValue(message));
			} else {
				System.out.println(message);
			}
		}
	}

}

package arden.runtime.validation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation for scenarios. It's value contains the scenario description.
 * This allows JUnit to print the scenario description instead of the method
 * name.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Scenario {
	String value();
}

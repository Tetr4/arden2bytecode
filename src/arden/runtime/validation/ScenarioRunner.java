package arden.runtime.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

/**
 * A JUnit runner for scenarios in an MLM. Test name are taken from
 * a @{@link Scenario} annotation instead of the test method name. <br>
 * Tests in MLMs can not be run by the default junit runner, as it only accepts
 * test classes with a single empty constructor.
 */
public class ScenarioRunner extends BlockJUnit4ClassRunner {

	public ScenarioRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected TestClass createTestClass(Class<?> testClass) {
		return new MultiConstructorTestClass(testClass);
	}

	@Override
	protected String testName(FrameworkMethod method) {
		// Show full scenario name instead of method name
		Scenario scenario = method.getAnnotation(Scenario.class);
		if (scenario != null) {
			return scenario.value();
		}
		return method.getName();
	}

	@Override
	protected void validateOnlyOneConstructor(List<Throwable> arg0) {
	}

	@Override
	protected void validateZeroArgConstructor(List<Throwable> arg0) {
	}

	/**
	 * This is a dirty hack to get JUnit to run classes with multiple
	 * constructors. <br>
	 * The {@link TestClass} throws an exception in it's constructor for test
	 * classes with multiple constructors. Therefore <code>null</code> is passed
	 * in, which does not produce an exception, and the private fields are set
	 * via reflection. <br>
	 * The only difference to the original constructor is, that the test class
	 * constructor count is not checked.
	 */
	public static class MultiConstructorTestClass extends TestClass {

		public MultiConstructorTestClass(Class<?> clazz) {
			super(null);

			try {
				// set private clazz field
				Field clazzField = TestClass.class.getDeclaredField("clazz");
				clazzField.setAccessible(true);
				clazzField.set(this, clazz);

				// make static private method accessible
				Method makeDeeplyUnmodifiableMethod = TestClass.class.getDeclaredMethod("makeDeeplyUnmodifiable",
						Map.class);
				makeDeeplyUnmodifiableMethod.setAccessible(true);

				Map<Class<? extends Annotation>, List<FrameworkMethod>> methodsForAnnotations = new LinkedHashMap<Class<? extends Annotation>, List<FrameworkMethod>>();
				Map<Class<? extends Annotation>, List<FrameworkField>> fieldsForAnnotations = new LinkedHashMap<Class<? extends Annotation>, List<FrameworkField>>();

				scanAnnotatedMembers(methodsForAnnotations, fieldsForAnnotations);

				// set private field
				Field methodsForAnnotationsField = TestClass.class.getDeclaredField("methodsForAnnotations");
				methodsForAnnotationsField.setAccessible(true);
				methodsForAnnotationsField.set(this, makeDeeplyUnmodifiableMethod.invoke(null, methodsForAnnotations));

				// set private field
				Field fieldsForAnnotationsField = TestClass.class.getDeclaredField("fieldsForAnnotations");
				fieldsForAnnotationsField.setAccessible(true);
				fieldsForAnnotationsField.set(this, makeDeeplyUnmodifiableMethod.invoke(null, fieldsForAnnotations));
			} catch (Exception e) {
				System.err.println(e);
				throw new RuntimeException(e);
			}
		}

		/**
		 * Return the empty constructor.
		 */
		public Constructor<?> getOnlyConstructor() {
			Constructor<?>[] constructors = getJavaClass().getConstructors();
			for (Constructor<?> constructor : constructors) {
				if (constructor.getParameterTypes().length == 0) {
					return constructor;
				}
			}
			throw new RuntimeException("Empty constructor is required.");
		}

	}

}
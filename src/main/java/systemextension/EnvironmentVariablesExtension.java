package systemextension;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.lang.Class.forName;
import static java.lang.System.getProperties;
import static java.lang.System.getenv;
import static java.lang.System.setProperties;

public class EnvironmentVariablesExtension implements BeforeEachCallback, AfterEachCallback {

  private Map<String, String> originalVariables;
  private Map<String, String> setVariables;

  public EnvironmentVariablesExtension() {
    this.setVariables = new HashMap<>();
  }

  @Override
  public void beforeEach(final ExtensionContext extensionContext) {
    originalVariables = new HashMap<>(getenv());
  }

  @Override
  public void afterEach(final ExtensionContext extensionContext) {
    restoreOriginalVariables(originalVariables);
  }

  public void set(String name, String value) {
    validateNotSet(name, value);
    setVariables.put(name, value);
    setEnvironmentVariables();
  }

  private void validateNotSet(String name, String value) {
    if (setVariables.containsKey(name)) {
      String currentValue = setVariables.get(name);
      throw new IllegalArgumentException(
        String.format("The environment variable '%s' cannot be set to %s because it was already set to %s.",
                      name, format(value), format(currentValue)
        ));
    }
  }

  private String format(String text) {
    return text == null ? "null" : "'" + text + "'";
  }

  private void setEnvironmentVariables() {
    overrideVariables(getEditableMapOfVariables());
    overrideVariables(getTheCaseInsensitiveEnvironment());
  }

  private void overrideVariables(Map<String, String> existingVariables) {
    // will be null when running on non-Windows machine and theCaseInsensitiveEnvironment variables are passed
    if (existingVariables != null) {
      setVariables.forEach((name, value) -> set(existingVariables, name, value));
    }
  }

  private void set(Map<String, String> variables, String name, String value) {
    if (value == null) {
      variables.remove(name);
    } else {
      variables.put(name, value);
    }
  }

  private void restoreOriginalVariables(Map<String, String> originalVariables) {
    restoreVariables(getEditableMapOfVariables(), originalVariables);
    restoreVariables(getTheCaseInsensitiveEnvironment(), originalVariables);
  }

  private void restoreVariables(Map<String, String> variables, Map<String, String> originalVariables) {
    // theCaseInsensitiveEnvironment may be null
    if (variables != null) {
      variables.clear();
      variables.putAll(originalVariables);
    }
  }

  private static Map<String, String> getEditableMapOfVariables() {
    Class<?> classOfMap = getenv().getClass();
    try {
      return getFieldValue(classOfMap, getenv(), "m");
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Cannot access the field 'm' of the map System.getenv().", e);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException("Expecting System.getenv() to have a field 'm' but it has not.", e);
    }
  }

  /*
   * The names of environment variables are case-insensitive in Windows.
   * Therefore it stores the variables in a TreeMap named
   * theCaseInsensitiveEnvironment.
   */
  private static Map<String, String> getTheCaseInsensitiveEnvironment() {
    final String caseInsensitiveEnv = "theCaseInsensitiveEnvironment";
    final String processEnv = "java.lang.ProcessEnvironment";
    try {
      Class<?> processEnvironment = forName(processEnv);
      return getFieldValue(processEnvironment, null, caseInsensitiveEnv);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(
        String.format("Expecting the existence of the class %s but it does not exist.", processEnv), e
      );
    } catch (IllegalAccessException e) {
      throw new RuntimeException(
        String.format("Cannot access the static field %s of the class %s.", caseInsensitiveEnv, processEnv), e
      );
    } catch (NoSuchFieldException e) {
      // this field is only available for Windows
      return null;
    }
  }

  private static Map<String, String> getFieldValue(Class<?> klass, Object object, String name)
    throws NoSuchFieldException, IllegalAccessException {
    Field field = klass.getDeclaredField(name);
    field.setAccessible(true);
    return (Map<String, String>) field.get(object);
  }

}

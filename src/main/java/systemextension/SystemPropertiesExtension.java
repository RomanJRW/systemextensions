package systemextension;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Properties;

import static java.lang.System.getProperties;
import static java.lang.System.setProperties;

public class SystemPropertiesExtension implements BeforeEachCallback, AfterEachCallback {

  private Properties originalProperties;

  public void beforeEach(final ExtensionContext extensionContext) throws Exception {
    originalProperties = getProperties();
    setProperties(copyOf(originalProperties));
  }

  public void afterEach(final ExtensionContext extensionContext) throws Exception {
    setProperties(originalProperties);
  }

  private Properties copyOf(Properties source) {
    Properties copy = new Properties();
    copy.putAll(source);
    return copy;
  }

}

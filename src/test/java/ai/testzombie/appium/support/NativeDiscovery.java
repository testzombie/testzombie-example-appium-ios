package ai.testzombie.appium.support;

import com.testzombie.driver.TestZombieDriver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;

/**
 * Enables the *SDK's own* Discovery, corresponding to Playwright's
 * TestZombie.configure({ discoveryEnabled: true, ... }).
 *
 * As in the Android example, discovery setters are located at runtime
 * to keep SDK integration consistent across demo projects. No application-side recorder or fake
 * backend upload is used. If native enablement is unavailable, fail before
 * browser startup with a helpful error.
 */
public final class NativeDiscovery {
    public static final String DEFAULT_FILE_PATH =
            "test-results/testzombie-discovery-{testRunId}.json";

    private NativeDiscovery() {}

    public static void configure(boolean enabled) {
        if (!enabled) {
            // All non-Discovery scenarios actively switch SDK Discovery off, if possible.
            invokeOptionalBoolean("setDiscoveryEnabled", false);
            invokeOptionalBoolean("setDiscoveryFileEnabled", false);
            return;
        }

        if (!invokeOptionalBoolean("setDiscoveryEnabled", true)) {
            throw new IllegalStateException("Native TestZombie Discovery cannot be activated: "
                    + "com.testzombie.driver.TestZombieDriver does not expose public static "
                    + "setDiscoveryEnabled(boolean). The local DiscoveryRecorder has been "
                    + "removed intentionally: a JSON-only trace would not be equivalent "
                    + "to Playwright Discovery. Check testzombie-lib-java 1.3.0 for the "
                    + "actual Discovery API and connect it in NativeDiscovery.java.");
        }

        boolean fileEnabled = invokeOptionalBoolean("setDiscoveryFileEnabled", true);
        String filePath = AppiumDriverFactory.setting("testzombie.discovery.file", "TESTZOMBIE_DISCOVERY_FILE",
                DEFAULT_FILE_PATH);
        boolean pathConfigured = invokeOptionalPath("setDiscoveryFilePath", filePath);
        System.out.println("Native TestZombie SDK Discovery enabled");
        if (fileEnabled && pathConfigured) {
            System.out.println("SDK JSON output requested: " + filePath);
        } else {
            System.out.println("SDK does not expose both file setters: "
                    + "backend Discovery may work, but JSON file output is not guaranteed");
        }
    }

    private static boolean invokeOptionalBoolean(String name, boolean value) {
        Method method = publicStaticMethod(name, boolean.class);
        if (method == null) method = publicStaticMethod(name, Boolean.class);
        if (method == null) return false;
        invoke(method, value);
        return true;
    }

    private static boolean invokeOptionalPath(String name, String value) {
        Method method = publicStaticMethod(name, String.class);
        if (method != null) {
            invoke(method, value);
            return true;
        }
        method = publicStaticMethod(name, Path.class);
        if (method == null) return false;
        invoke(method, Path.of(value));
        return true;
    }

    private static Method publicStaticMethod(String name, Class<?> argumentType) {
        try {
            Method method = TestZombieDriver.class.getMethod(name, argumentType);
            return Modifier.isStatic(method.getModifiers()) ? method : null;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static void invoke(Method method, Object value) {
        try {
            method.invoke(null, value);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalStateException("TestZombie SDK rejected Discovery configuration: "
                    + method.getName(), ex);
        }
    }
}

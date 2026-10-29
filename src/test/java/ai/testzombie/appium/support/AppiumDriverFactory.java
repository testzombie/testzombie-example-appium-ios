package ai.testzombie.appium.support;

import com.testzombie.driver.SourceUpdateMode;
import com.testzombie.driver.TestZombieDriver;
import com.testzombie.driver.TestZombieDriverMobile;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/** Creates an iOS XCUITest session, with SDK configuration per feature demo. */
public final class AppiumDriverFactory {
    public static final String DEFAULT_BUNDLE_ID = "ai.testzombie.hybridios";

    private AppiumDriverFactory() {}

    public static IOSDriver create(SourceUpdateMode sourceUpdateMode,
                                   boolean discoveryEnabled,
                                   boolean testZombieEnabled) throws Exception {
        String app = setting("app.ios", "APP_IOS", "./TestZombieHybridIOS.app");
        Path appPath = Path.of(app).toAbsolutePath().normalize();
        if (!Files.exists(appPath)) {
            throw new IllegalArgumentException("iOS .app/.ipa nicht gefunden: " + appPath);
        }

        XCUITestOptions options = new XCUITestOptions();
        options.setCapability("platformName", "iOS");
        options.setCapability("appium:automationName", "XCUITest");
        options.setCapability("appium:deviceName", setting("device.name", "DEVICE_NAME", "iPhone 16e"));
        options.setCapability("appium:app", appPath.toString());
        options.setCapability("appium:bundleId", setting("app.bundleId", "APP_BUNDLE_ID", DEFAULT_BUNDLE_ID));
        options.setCapability("appium:noReset", false);
        options.setCapability("appium:fullReset", false);
        options.setCapability("appium:enforceAppInstall", true);
        options.setCapability("appium:newCommandTimeout", 180);
        options.setCapability("appium:wdaLaunchTimeout", 120000);
        options.setCapability("appium:webviewConnectTimeout", 35000);
        String platformVersion = setting("platform.version", "PLATFORM_VERSION", null);
        String udid = setting("device.udid", "DEVICE_UDID", null);
        if (platformVersion != null) options.setCapability("appium:platformVersion", platformVersion);
        if (udid != null) options.setCapability("appium:udid", udid);

        URL serverUrl = URI.create(setting("appium.server", "APPIUM_SERVER", "http://127.0.0.1:4723")).toURL();
        if (!testZombieEnabled) {
            // The matrix is a *real* application control, with no healing interception.
            TestZombieDriver.setSourceUpdateMode(SourceUpdateMode.OFF);
            NativeDiscovery.configure(false);
            return new IOSDriver(serverUrl, options);
        }

        String apiKey = setting("testzombie.apikey", "TESTZOMBIE_API_KEY", null);
        String email = setting("testzombie.email", "TESTZOMBIE_EMAIL", null);
        if (apiKey == null || email == null) {
            throw new IllegalStateException("TestZombie credentials missing. Set TESTZOMBIE_API_KEY "
                    + "and TESTZOMBIE_EMAIL, or pass -Dtestzombie.apikey=... "
                    + "-Dtestzombie.email=... .");
        }

        // The TestZombie SDK is configured *before* Appium driver creation.
        TestZombieDriver.setCredentials(apiKey, email);
        TestZombieDriver.setSourceUpdateMode(sourceUpdateMode);
        NativeDiscovery.configure(discoveryEnabled);

        // Preserve compatibility with the original iOS example: different SDK builds
        // may name their IOS factory createIOS or createIos.
        Method factory = findIosFactory(serverUrl, options);
        if (factory == null) {
            throw new IllegalStateException("No public static TestZombieDriverMobile.createIOS/createIos"
                    + "(URL, Capabilities) factory found. Check testzombie-lib-java version.");
        }
        Object result;
        try {
            result = factory.invoke(null, serverUrl, options);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof Exception error) throw error;
            if (cause instanceof Error fatal) throw fatal;
            throw exception;
        }
        if (!(result instanceof IOSDriver driver)) {
            throw new IllegalStateException("TestZombie iOS factory did not return an IOSDriver: " + result);
        }
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        return driver;
    }

    private static Method findIosFactory(URL serverUrl, XCUITestOptions options) {
        for (Method method : TestZombieDriverMobile.class.getMethods()) {
            if (!method.getName().equals("createIOS") && !method.getName().equals("createIos")) continue;
            if (!Modifier.isStatic(method.getModifiers())) continue;
            Class<?>[] args = method.getParameterTypes();
            if (args.length == 2 && args[0].isInstance(serverUrl) && args[1].isInstance(options)) {
                return method;
            }
        }
        return null;
    }

    public static String setting(String systemProperty, String environment, String fallback) {
        String value = System.getProperty(systemProperty);
        if (value != null && !value.isBlank() && !value.startsWith("${")) return value;
        value = System.getenv(environment);
        return value == null || value.isBlank() ? fallback : value;
    }
}

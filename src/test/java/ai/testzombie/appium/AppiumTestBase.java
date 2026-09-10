package ai.testzombie.appium;

import com.testzombie.driver.TestZombieDriver;
import com.testzombie.driver.TestZombieDriverMobile;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.junit.jupiter.api.AfterEach;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

abstract class AppiumTestBase {
    protected static final String DEFAULT_BUNDLE_ID = "ai.testzombie.hybridios";

    protected IOSDriver driver;
    protected WebDriverWait wait;

    protected void startApp() throws Exception {
        String server = property("appium.server", "APPIUM_SERVER", "http://127.0.0.1:4723");
        String device = property("device.name", "DEVICE_NAME", "iPhone 16e");
        String platformVersion = property("platform.version", "PLATFORM_VERSION", null);
        String udid = property("device.udid", "DEVICE_UDID", null);
        String bundleId = property("app.bundleId", "APP_BUNDLE_ID", DEFAULT_BUNDLE_ID);
        String appValue = property("app.ios", "APP_IOS", "./TestZombieHybridIOS.app");

        Path appPath = Path.of(appValue).toAbsolutePath().normalize();
        if (!Files.exists(appPath)) {
            throw new IllegalArgumentException("iOS-App nicht gefunden: " + appPath);
        }

        XCUITestOptions options = new XCUITestOptions();
        options.setCapability("platformName", "iOS");
        options.setCapability("appium:automationName", "XCUITest");
        options.setCapability("appium:deviceName", device);
        options.setCapability("appium:app", appPath.toString());
        options.setCapability("appium:bundleId", bundleId);
        options.setCapability("appium:noReset", false);
        options.setCapability("appium:fullReset", false);
        options.setCapability("appium:enforceAppInstall", true);
        options.setCapability("appium:newCommandTimeout", 180);
        options.setCapability("appium:wdaLaunchTimeout", 120000);
        options.setCapability("appium:webviewConnectTimeout", 35000);

        if (platformVersion != null && !platformVersion.isBlank()) {
            options.setCapability("appium:platformVersion", platformVersion);
        }
        if (udid != null && !udid.isBlank()) {
            options.setCapability("appium:udid", udid);
        }

        URL serverUrl = URI.create(server).toURL();
        boolean healing = Boolean.parseBoolean(
                property("testzombie.healing", "TESTZOMBIE_HEALING", "true")
        );

        driver = healing ? createTestZombieIOSDriver(serverUrl, options) : new IOSDriver(serverUrl, options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    private IOSDriver createTestZombieIOSDriver(URL serverUrl, XCUITestOptions options) throws Exception {
        // TestZombie credentials:
        // - local quickstart: -Dtestzombie.apikey=... -Dtestzombie.email=...
        // - CI/CD: TESTZOMBIE_API_KEY and TESTZOMBIE_EMAIL environment variables
        String apiKey = firstNonBlank(
                System.getProperty("testzombie.apikey"),
                System.getenv("TESTZOMBIE_API_KEY")
        );
        String email = firstNonBlank(
                System.getProperty("testzombie.email"),
                System.getenv("TESTZOMBIE_EMAIL")
        );

        if (apiKey == null || email == null) {
            System.out.println(
                    "Missing TestZombie credentials. Set testzombie.apikey/testzombie.email "
                            + "or TESTZOMBIE_API_KEY/TESTZOMBIE_EMAIL."
            );
        }

        //Or copy your credentials directly from testzombie.ai Onboarding screen
        TestZombieDriver.setCredentials(apiKey, email);

        Method factory = findIosFactory(serverUrl, options);
        if (factory == null) {
            throw new IllegalStateException(
                    "In com.testzombie.driver.TestZombieDriverMobile wurde keine öffentliche "
                            + "createIOS/createIos(URL, Capabilities)-Factory gefunden. "
                            + "Prüfe die installierte TestZombie-Version oder starte zum Gegencheck "
                            + "mit -Dtestzombie.healing=false."
            );
        }

        try {
            Object created = factory.invoke(null, serverUrl, options);
            if (!(created instanceof IOSDriver iosDriver)) {
                throw new IllegalStateException(
                        "Die TestZombie-iOS-Factory lieferte keinen IOSDriver, sondern: "
                                + (created == null ? "null" : created.getClass().getName())
                );
            }
            return iosDriver;
        } catch (InvocationTargetException error) {
            Throwable cause = error.getCause();
            if (cause instanceof Exception exception) throw exception;
            if (cause instanceof Error fatal) throw fatal;
            throw error;
        }
    }

    private Method findIosFactory(URL serverUrl, XCUITestOptions options) {
        for (Method method : TestZombieDriverMobile.class.getMethods()) {
            String name = method.getName();
            if (!(name.equals("createIOS") || name.equals("createIos"))) continue;

            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length != 2) continue;
            if (!parameters[0].isInstance(serverUrl)) continue;
            if (!parameters[1].isInstance(options)) continue;
            return method;
        }
        return null;
    }

    @AfterEach
    void stopApp() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void saveScreenshot(String name) {
        if (driver == null) return;
        try {
            Path dir = Path.of("target", "screenshots");
            Files.createDirectories(dir);
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(dir.resolve(name + ".png"), png);
        } catch (IOException | RuntimeException ignored) {
            // Ein Screenshot-Fehler soll den eigentlichen Testfehler nicht verdecken.
        }
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) return first;
        if (second != null && !second.isBlank()) return second;
        return null;
    }

    private static String property(String property, String environment, String fallback) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) value = System.getenv(environment);
        return value == null || value.isBlank() ? fallback : value;
    }
}

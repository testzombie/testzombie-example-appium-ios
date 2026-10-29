package ai.testzombie.appium.testbase;

import ai.testzombie.appium.support.AppiumDriverFactory;
import ai.testzombie.appium.support.NativeDiscovery;
import com.testzombie.driver.SourceUpdateMode;
import com.testzombie.driver.TestZombieDriver;
import io.appium.java_client.ios.IOSDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/** Shared JUnit lifecycle. Mirrors Android's BaseAndroidTest, using XCUITest. */
public abstract class BaseIosTest {
    protected IOSDriver driver;
    protected WebDriverWait wait;

    protected SourceUpdateMode sourceUpdateMode() {
        return SourceUpdateMode.OFF;
    }

    protected boolean discoveryEnabled() {
        return false;
    }

    protected boolean useTestZombie() {
        return true;
    }

    @BeforeEach
    void startApp(TestInfo info) throws Exception {
        System.out.printf("%n=== %s | sourceUpdate=%s | discovery=%s | sdk=%s ===%n",
                info.getDisplayName(), sourceUpdateMode(), discoveryEnabled(), useTestZombie());
        driver = AppiumDriverFactory.create(sourceUpdateMode(), discoveryEnabled(), useTestZombie());
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    /** Restart the iOS application but keep the SAME Appium/TestZombie session. */
    protected void resetDemoState() {
        String bundleId = AppiumDriverFactory.setting("app.bundleId", "APP_BUNDLE_ID",
                AppiumDriverFactory.DEFAULT_BUNDLE_ID);
        driver.context("NATIVE_APP");
        driver.terminateApp(bundleId);
        driver.activateApp(bundleId);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mutation-level-0")));
    }

    @AfterEach
    void stopApp() {
        try {
            if (driver != null) driver.quit();
        } finally {
            driver = null;
            wait = null;
            NativeDiscovery.configure(false);
            TestZombieDriver.setSourceUpdateMode(SourceUpdateMode.OFF);
        }
    }

    protected void saveScreenshot(String name) {
        if (driver == null) return;
        try {
            Path directory = Path.of("target", "screenshots");
            Files.createDirectories(directory);
            Files.write(directory.resolve(name + ".png"),
                    ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
        } catch (IOException | RuntimeException ignored) {
            // Preserve the original test failure if screenshot capture fails.
        }
    }
}

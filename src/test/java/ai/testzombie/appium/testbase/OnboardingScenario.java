package ai.testzombie.appium.testbase;

import ai.testzombie.appium.pages.HybridOnboardingPage;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Shared iOS hybrid business flow for the three feature demos. */
public final class OnboardingScenario {
    private OnboardingScenario() {}

    public static void run(IOSDriver driver, WebDriverWait wait, int level, String scenarioName) {
        System.out.printf("%n=== %s (Mutation Level %d) ===%n", scenarioName, level);
        HybridOnboardingPage app = new HybridOnboardingPage(driver, wait);
        app.chooseMutationLevel(level)
                .enterPassword()
                .enterPerson()
                .chooseEnterprisePlan(level);

        String status = app.completeWebSummary();
        assertTrue(status.toLowerCase(Locale.ROOT).contains("erfolgreich"),
                "Onboarding must succeed at Level " + level + "; status: " + status);
        System.out.printf("=== iOS Mutation Level %d erfolgreich: %s ===%n", level, status);
    }
}
